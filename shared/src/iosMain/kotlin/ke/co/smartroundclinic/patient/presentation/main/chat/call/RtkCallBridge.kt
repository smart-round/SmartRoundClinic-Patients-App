package ke.co.smartroundclinic.patient.presentation.main.chat.call

import platform.UIKit.UIView

/**
 * Kotlin↔Swift bridge for RealtimeKit's Core SDK on iOS.
 *
 * The Kotlin framework has no direct visibility into RealtimeKit (it's linked into the
 * iosApp Xcode target via Swift Package Manager, not cinterop'd into this framework), so
 * [RtkCallController] drives calls entirely through this callback-based indirection —
 * same shape as the old `RealtimeMeetingBridge` factory, just with the richer set of
 * hooks a custom call UI needs instead of "here's a UIViewController, good luck".
 *
 * Swift sets [factory] once at app startup — see `iOSApp.swift`'s `wireRtkCallBridge()`.
 */
object RtkCallBridge {
    var factory: ((
        authToken: String,
        enableAudio: Boolean,
        enableVideo: Boolean,
        listener: IosCallSessionListener,
    ) -> IosCallSession)? = null
}

/**
 * Implemented in Swift — a thin handle over one live RealtimeKit `RealtimeKitClient`.
 * Swift performs `doInit` immediately on creation and auto-joins the room on init success
 * (mirroring the Android actual's behavior), reporting back through [IosCallSessionListener].
 */
interface IosCallSession {
    fun leaveRoom()
    fun toggleAudio()
    fun toggleVideo()
    fun switchCamera()

    // Named `dispose`, not `release` — the latter collides with NSObject's reserved
    // Objective-C selector and Swift can't disambiguate an override from it.
    fun dispose()

    fun localVideoView(): UIView?
    fun remoteVideoView(): UIView?
}

/** Implemented in Kotlin (by [RtkCallController]) — Swift forwards SDK events into this. */
interface IosCallSessionListener {
    fun onConnected()
    fun onEnded()
    fun onFailed(message: String)
    fun onAudioUpdate(enabled: Boolean)
    fun onVideoUpdate(enabled: Boolean)
    fun onRemoteParticipantUpdate(name: String?, audioEnabled: Boolean, videoEnabled: Boolean)
}
