import AVFoundation
import CallKit
import Foundation
import PushKit
import Shared
import RTKWebRTC

/// Configures AVAudioSession the way WebRTC needs and activates it, in one locked operation —
/// necessary now that RTCAudioSession is in manual mode (see CallKitManager.init): WebRTC no
/// longer applies its own category/mode/activation automatically, so whatever bare-bones session
/// CallKit (or nothing, for outgoing calls) set up is all there is unless we configure it
/// ourselves. `.videoChat` mode + `.defaultToSpeaker` is what actually gets audio routed to the
/// loudspeaker instead of the earpiece — the earpiece route is real audio, just inaudible once
/// the phone isn't held to the caller's ear, which it normally isn't during a video call. This is
/// the fix for "video connects but I can't hear the other person talk."
func configureAndActivateWebRTCAudioSession(isVideo: Bool) {
    NSLog("SRC-AUDIO: configureAndActivateWebRTCAudioSession start, isVideo=\(isVideo)")
    let rtcAudioSession = RTKRTCAudioSession.sharedInstance()
    let configuration = RTKRTCAudioSessionConfiguration.webRTC()
    configuration.category = AVAudioSession.Category.playAndRecord.rawValue
    configuration.mode = (isVideo ? AVAudioSession.Mode.videoChat : AVAudioSession.Mode.voiceChat).rawValue
    configuration.categoryOptions = isVideo ? [.allowBluetooth, .defaultToSpeaker] : [.allowBluetooth]

    rtcAudioSession.lockForConfiguration()
    do {
        try rtcAudioSession.setConfiguration(configuration, active: true)
        NSLog("SRC-AUDIO: setConfiguration succeeded")
    } catch {
        NSLog("SRC-AUDIO: setConfiguration FAILED — \(error.localizedDescription)")
    }
    rtcAudioSession.unlockForConfiguration()
    rtcAudioSession.isAudioEnabled = true
    let session = AVAudioSession.sharedInstance()
    NSLog("SRC-AUDIO: isAudioEnabled set true, sampleRate=\(session.sampleRate), category=\(session.category.rawValue), route=\(session.currentRoute.outputs.map { $0.portName })")
}

func deactivateWebRTCAudioSession() {
    let rtcAudioSession = RTKRTCAudioSession.sharedInstance()
    rtcAudioSession.isAudioEnabled = false
    rtcAudioSession.lockForConfiguration()
    try? rtcAudioSession.setActive(false)
    rtcAudioSession.unlockForConfiguration()
}

/// Handles incoming VoIP pushes via PushKit and reports them to CallKit (CXProvider), which
/// shows the native system incoming-call UI even when the app is backgrounded or killed — the
/// reliable iOS channel for WhatsApp-style ringing (Android's equivalent is a full-screen-intent
/// notification, see IncomingCallHandler.android.kt, since Android has no CallKit concept).
final class CallKitManager: NSObject {
    static let shared = CallKitManager()

    private let pushRegistry = PKPushRegistry(queue: .main)
    private let provider: CXProvider
    private let callController = CXCallController()

    private var uuidByCallId: [String: UUID] = [:]
    private var callIdByUuid: [UUID: String] = [:]
    private var callerInfoByCallId: [String: (callerId: String, callerName: String?)] = [:]
    private var isVideoByCallId: [String: Bool] = [:]
    // Outgoing calls (this device placed them) never went through the ringing-invite maps
    // above — by the time RtkCallSessionImpl reports one, the callee has already answered via
    // signaling (see RtkCallController.ios.kt's start()), so there's no separate "decline"
    // affordance to track, only "end". Tracked independently of activeCallId (incoming).
    private var outgoingCallUUID: UUID?
    // Set from isVideoByCallId when the call is answered — read by didActivate below, which has
    // no other way to know whether this call needs .videoChat (speaker-routed) audio mode.
    private var activeCallIsVideo = true
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

    /// True while a CallKit-reported call (incoming, answered via CXAnswerCallAction, or
    /// outgoing, reported via [reportOutgoingCall]) is ongoing — lets RtkCallSessionImpl
    /// (iOSApp.swift) tell whether CallKit already owns RTCAudioSession activation (it does,
    /// for both directions now) rather than needing to drive it manually.
    var isCallKitCallActive: Bool { activeCallId != nil || outgoingCallUUID != nil }

    private override init() {
        let config = CXProviderConfiguration()
        config.supportsVideo = true
        config.maximumCallGroups = 1
        config.maximumCallsPerCallGroup = 1
        config.supportedHandleTypes = [.generic]
        provider = CXProvider(configuration: config)
        super.init()
        // `queue: nil` would have CallKit invoke every delegate method — including didActivate,
        // where we bring up RTCAudioSession — on a private background queue it manages internally.
        // On a process freshly launched from killed/suspended by a VoIP push, doing that
        // entitlement-sensitive audio activation off the main thread this early in the process's
        // lifecycle can trigger audiomxd's "SecTask is NULL / Missing entitlement" failure on
        // AURemoteIO. Forcing the delegate onto the main queue keeps that activation on the same
        // thread the rest of the app's audio/UI work runs on.
        provider.setDelegate(self, queue: .main)

        // WebRTC's *automatic* audio-session mode (the default) races CallKit for control of
        // AVAudioSession the moment a call is reported: CallKit activates/deactivates the session
        // externally, and WebRTC's own activation attempts against that externally-held session
        // can silently no-op, leaving its audio unit never actually started even though signaling
        // succeeds (matches the "video connects, audio stays silent" symptom exactly — video
        // doesn't touch AVAudioSession at all). Manual mode hands control of *when* WebRTC's audio
        // unit turns on entirely to us: didActivate/didDeactivate below drive it for CallKit-
        // mediated (incoming) calls, RtkCallSessionImpl's init/dispose in iOSApp.swift drives it
        // directly for outgoing calls, which never go through CallKit in this app at all.
        let rtcAudioSession = RTKRTCAudioSession.sharedInstance()
        rtcAudioSession.useManualAudio = true
        rtcAudioSession.isAudioEnabled = false
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
        isVideoByCallId[callId] = isVideo

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

    /// Reports an outgoing call to CallKit — gives it the OS's native "return to call"
    /// background affordance (previously incoming-only) and hands RTCAudioSession activation
    /// to didActivate/didDeactivate below instead of RtkCallSessionImpl driving it manually.
    /// Called from RtkCallSessionImpl.init (iOSApp.swift) right as the RealtimeKit session
    /// itself starts — the callee already answered via signaling by this point, so this is
    /// purely for system call-state/audio-routing integration, not gating the actual connect.
    func reportOutgoingCall(isVideo: Bool, calleeName: String?) {
        let uuid = UUID()
        outgoingCallUUID = uuid
        activeCallIsVideo = isVideo

        let handle = CXHandle(type: .generic, value: calleeName ?? "Doctor")
        let startAction = CXStartCallAction(call: uuid, handle: handle)
        startAction.isVideo = isVideo
        callController.request(CXTransaction(action: startAction)) { [weak self] error in
            if let error = error {
                print("CallKitManager: reportOutgoingCall failed — \(error.localizedDescription)")
                self?.outgoingCallUUID = nil
            }
        }
    }

    /// Tells CallKit an outgoing call's media actually connected — call once RealtimeKit
    /// confirms the room join (RtkCallSessionImpl.onMeetingRoomJoinCompleted).
    func reportOutgoingCallConnected() {
        guard let uuid = outgoingCallUUID else { return }
        provider.reportOutgoingCall(with: uuid, connectedAt: nil)
    }

    private func endOutgoingCall() {
        guard let uuid = outgoingCallUUID else { return }
        provider.reportCall(with: uuid, endedAt: nil, reason: .remoteEnded)
        outgoingCallUUID = nil
    }

    /// Called once the in-app Call screen tears down (hangup tap, remote leaving, connection
    /// ending — see ConsultationViewModel.endCall() / ActiveCallNotifier), for both incoming
    /// and outgoing calls. Without this, CallKit still thinks the call it reported is ongoing
    /// even after our own UI has moved on, so its call state (status bar indicator, Recents
    /// entry, Dynamic Island, "return to call" pill) never clears.
    func endActiveCall() {
        if let callId = activeCallId {
            endCall(callId: callId)
            return
        }
        endOutgoingCall()
    }

    private func cleanup(callId: String, uuid: UUID) {
        uuidByCallId.removeValue(forKey: callId)
        callIdByUuid.removeValue(forKey: uuid)
        callerInfoByCallId.removeValue(forKey: callId)
        isVideoByCallId.removeValue(forKey: callId)
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
        isVideoByCallId.removeAll()
        reportedCallIds.removeAll()
        activeCallId = nil
        outgoingCallUUID = nil
    }

    // The call has already been placed via signaling (WS invite + answer) by the time
    // RtkCallSessionImpl reports it — this just confirms it to CallKit and hands audio-session
    // activation to didActivate below.
    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        NSLog("SRC-AUDIO: CXStartCallAction perform")
        provider.reportOutgoingCall(with: action.callUUID, startedConnectingAt: nil)
        action.fulfill()
        NSLog("SRC-AUDIO: CXStartCallAction fulfilled")
    }

    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        NSLog("SRC-AUDIO: CXAnswerCallAction perform")
        guard let callId = callIdByUuid[action.callUUID], let info = callerInfoByCallId[callId] else {
            action.fail()
            return
        }
        activeCallId = callId
        activeCallIsVideo = isVideoByCallId[callId] ?? true
        // Same deep-link the tap-to-join notification flow already uses — lands the user in
        // ConsultationChat/ConsultationCall once MainRoot picks up the pending event.
        NotificationDeepLink.shared.signal(
            event: NotificationEvent.ToCall(doctorId: info.callerId, doctorName: info.callerName ?? "Doctor", appointmentId: "")
        )
        action.fulfill()
        NSLog("SRC-AUDIO: CXAnswerCallAction fulfilled")
    }

    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        // The system's own End button (Dynamic Island, lock screen, CarPlay) for a call this
        // device placed — end the live RealtimeKit session via the same hook the in-app "End
        // call" button uses, not the ringing-invite decline path below (which doesn't apply —
        // this call was already answered).
        if action.callUUID == outgoingCallUUID {
            ActiveCallSignal.shared.onEndCall?()
            outgoingCallUUID = nil
            action.fulfill()
            return
        }

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

    // CallKit activates AVAudioSession itself once the call is answered, but with whatever bare
    // category/mode it defaults to — not necessarily one that routes audio anywhere audible. We
    // take over here and apply the actual WebRTC-appropriate configuration (see
    // configureAndActivateWebRTCAudioSession above) on top of it, which is also what actually
    // turns RTCAudioSession's audio unit on now that it's in manual mode (see CallKitManager.init).
    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        NSLog("SRC-AUDIO: CXProvider didActivate — sampleRate=\(audioSession.sampleRate), category=\(audioSession.category.rawValue)")
        configureAndActivateWebRTCAudioSession(isVideo: activeCallIsVideo)
    }

    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        NSLog("SRC-AUDIO: CXProvider didDeactivate")
        deactivateWebRTCAudioSession()
    }
}
