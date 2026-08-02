import AVFoundation
import CallKit
import Foundation
import PushKit
import Shared

/// Handles incoming VoIP pushes via PushKit and reports them to CallKit (CXProvider), which
/// shows the native system incoming-call UI even when the app is backgrounded or killed — the
/// reliable iOS channel for WhatsApp-style ringing (Android's equivalent is a full-screen-intent
/// notification, see IncomingCallHandler.android.kt, since Android has no CallKit concept).
final class CallKitManager: NSObject {
    static let shared = CallKitManager()

    private let pushRegistry = PKPushRegistry(queue: .main)
    private let provider: CXProvider

    private var uuidByCallId: [String: UUID] = [:]
    private var callIdByUuid: [UUID: String] = [:]
    private var callerInfoByCallId: [String: (callerId: String, callerName: String?)] = [:]
    // The call this device has actually answered and is now in, if any — lets
    // endActiveCall() (called from Kotlin once the in-app Call screen ends, see
    // ActiveCallNotifier/CallKitBridge.onEndActiveCall) tell CallKit the call is over
    // without the Kotlin side needing to plumb callId through the whole call-screen stack.
    private var activeCallId: String?
    // Only calls CallKit actually confirmed presenting to the user land here. iOS can silently
    // filter/reject a reported call — Focus Mode, Do Not Disturb, "Silence Unknown Callers"
    // screening our generic (non-phone-number) CXHandle — with no UI ever shown, yet still tear
    // it down internally via the same CXEndCallAction delegate method a real user Decline tap
    // uses. Without this guard, that silent system rejection was being forwarded to the backend
    // as if the callee had explicitly declined.
    private var reportedCallIds: Set<String> = []

    private override init() {
        let config = CXProviderConfiguration()
        config.supportsVideo = true
        config.maximumCallGroups = 1
        config.maximumCallsPerCallGroup = 1
        config.supportedHandleTypes = [.generic]
        provider = CXProvider(configuration: config)
        super.init()
        provider.setDelegate(self, queue: nil)
    }

    /// Call once at app launch (see iOSApp.swift's AppDelegate).
    func start() {
        pushRegistry.delegate = self
        pushRegistry.desiredPushTypes = [.voIP]
    }

    // MARK: - Called from Kotlin's IncomingCallHandler via CallKitBridge (see wireCallKitBridge())

    func reportIncomingCall(callId: String, callerName: String?, isVideo: Bool) {
        let uuid = UUID()
        uuidByCallId[callId] = uuid
        callIdByUuid[uuid] = callId

        let update = CXCallUpdate()
        update.remoteHandle = CXHandle(type: .generic, value: callerName ?? "Doctor")
        update.localizedCallerName = callerName ?? "Doctor"
        update.hasVideo = isVideo
        update.supportsHolding = false
        update.supportsGrouping = false
        update.supportsUngrouping = false
        update.supportsDTMF = false

        provider.reportNewIncomingCall(with: uuid, update: update) { [weak self] error in
            guard let self = self else { return }
            if let error = error {
                print("CallKitManager: failed to report incoming call — \(error.localizedDescription)")
                // Never actually shown to the user — clear it now so a later CXEndCallAction for
                // this UUID (iOS tearing down the call it silently rejected) can't be mistaken
                // for a real decline.
                self.cleanup(callId: callId, uuid: uuid)
            } else {
                self.reportedCallIds.insert(callId)
            }
        }
    }

    func endCall(callId: String) {
        guard let uuid = uuidByCallId[callId] else { return }
        provider.reportCall(with: uuid, endedAt: nil, reason: .remoteEnded)
        cleanup(callId: callId, uuid: uuid)
    }

    /// Called once the in-app Call screen tears down (hangup tap, remote leaving, connection
    /// ending — see ConsultationViewModel.endCall() / ActiveCallNotifier). Without this, CallKit
    /// still thinks the call it reported is ongoing even after our own UI has moved on, so its
    /// call state (status bar indicator, Recents entry, Dynamic Island) never clears.
    func endActiveCall() {
        guard let callId = activeCallId else { return }
        endCall(callId: callId)
    }

    private func cleanup(callId: String, uuid: UUID) {
        uuidByCallId.removeValue(forKey: callId)
        callIdByUuid.removeValue(forKey: uuid)
        callerInfoByCallId.removeValue(forKey: callId)
        reportedCallIds.remove(callId)
        if activeCallId == callId { activeCallId = nil }
    }
}

// MARK: - PKPushRegistryDelegate

extension CallKitManager: PKPushRegistryDelegate {
    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        guard type == .voIP else { return }
        let token = pushCredentials.token.map { String(format: "%02x", $0) }.joined()
        VoipTokenRegistrar.shared.register(token: token)
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {}

    // Apple requires CallKit to be told about every VoIP push received in this callback (or the
    // app risks losing its VoIP push entitlement) — for a new invite that means
    // reportNewIncomingCall (via onCallInvite -> reportIncomingCall below); for the other three
    // events it means updating the already-reported call via provider.reportCall(endedAt:) (see
    // endCall(callId:), reached through onCallCancelled). This handler used to only understand
    // the invite shape, so a caller hanging up before the callee answered — the exact case VoIP
    // push exists to handle reliably — silently left the ringing screen stuck on-screen.
    func pushRegistry(
        _ registry: PKPushRegistry,
        didReceiveIncomingPushWith payload: PKPushPayload,
        for type: PKPushType,
        completion: @escaping () -> Void
    ) {
        defer { completion() }
        guard type == .voIP else { return }
        let data = payload.dictionaryPayload

        guard let callId = data["callId"] as? String, let event = data["event"] as? String else { return }

        switch event {
        case "Incoming Video Call":
            guard
                let callerId = data["callerId"] as? String,
                let doctorId = data["doctorId"] as? String,
                let patientId = data["patientId"] as? String
            else { return }

            let callerName = data["callerName"] as? String
            let isVideo = (data["isVideo"] as? String) == "true"
            let ringTimeoutSeconds = Int64((data["ringTimeoutSeconds"] as? String) ?? "") ?? 45

            callerInfoByCallId[callId] = (callerId: callerId, callerName: callerName)

            IncomingCallHandler.shared.onCallInvite(
                callId: callId,
                callerId: callerId,
                callerName: callerName,
                doctorId: doctorId,
                patientId: patientId,
                isVideo: isVideo,
                ringTimeoutSeconds: ringTimeoutSeconds
            )
        // Sent to the caller once the callee answers/declines elsewhere — this device never had
        // a CallKit session for its own outgoing call, so there's nothing to report here beyond
        // updating the in-app "Calling…" state.
        case "Call Answered":
            IncomingCallHandler.shared.onCallAnswered(callId: callId)
        case "Call Declined":
            IncomingCallHandler.shared.onCallDeclined(callId: callId)
        // Sent to the callee when the caller hangs up before answering — dismisses the CallKit
        // ringing screen via IncomingCallHandler.onCallCancelled -> CallKitBridge.onEndCall -> endCall(callId:).
        case "Call Cancelled":
            IncomingCallHandler.shared.onCallCancelled(callId: callId)
        default:
            print("CallKitManager: unrecognized VoIP push event '\(event)' — no handler for it")
        }
    }
}

// MARK: - CXProviderDelegate

extension CallKitManager: CXProviderDelegate {
    func providerDidReset(_ provider: CXProvider) {
        uuidByCallId.removeAll()
        callIdByUuid.removeAll()
        callerInfoByCallId.removeAll()
        reportedCallIds.removeAll()
        activeCallId = nil
    }

    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        guard let callId = callIdByUuid[action.callUUID], let info = callerInfoByCallId[callId] else {
            action.fail()
            return
        }
        activeCallId = callId
        // Same deep-link the tap-to-join notification flow already uses — lands the user in
        // ConsultationChat/ConsultationCall once MainRoot picks up the pending event.
        NotificationDeepLink.shared.signal(
            event: NotificationEvent.ToCall(doctorId: info.callerId, doctorName: info.callerName ?? "Doctor", appointmentId: "")
        )
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        guard let callId = callIdByUuid[action.callUUID] else {
            action.fulfill()
            return
        }
        // Only forward as a real decline if CallKit actually confirmed presenting this call to
        // the user — otherwise this is the system tearing down a call it silently filtered
        // (see reportedCallIds), and the backend/caller should never hear about it as a decline.
        if reportedCallIds.contains(callId), let info = callerInfoByCallId[callId] {
            CallActionDispatcher.shared.decline(otherUserId: info.callerId, callId: callId)
        }
        cleanup(callId: callId, uuid: action.callUUID)
        action.fulfill()
    }

    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {}
    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {}
}
