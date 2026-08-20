import AVKit
import UIKit

/// WhatsApp/FaceTime-style video-call Picture-in-Picture — shows the remote participant's
/// live video in a small floating system window when the app backgrounds during an active
/// video call. Built on Apple's dedicated calling-app API (`AVPictureInPictureController` +
/// `AVPictureInPictureVideoCallViewController`), not the generic `AVPlayer`-based PiP.
///
/// Re-parents the *same* `remoteVideoView()` instance `RtkCallSessionImpl` already exposes
/// into the PiP content view controller for the duration of PiP, then moves it straight back
/// to its original spot in the Compose-managed call screen the moment PiP stops (dismissed or
/// expanded back to full screen) — never duplicates/clones the view, which both
/// `PlatformVideoView.ios.kt`/`.android.kt` document at length is unsafe for this SDK's video
/// surfaces (they hand back the same underlying native view every time; re-adding a *second*
/// copy crashes on detach).
final class CallPictureInPictureManager: NSObject {
    static let shared = CallPictureInPictureManager()

    private var pipController: AVPictureInPictureController?
    private let contentViewController = AVPictureInPictureVideoCallViewController()
    private var possibleObservation: NSKeyValueObservation?

    // Where the video view lived before PiP borrowed it, so restoreVideoView() can put it
    // back exactly rather than guessing at a layout.
    private weak var originalSuperview: UIView?
    private var originalFrame: CGRect = .zero
    private weak var videoView: UIView?

    private override init() {
        super.init()
        contentViewController.preferredContentSize = CGSize(width: 9, height: 16)
    }

    /// Arms PiP for the current call so it's ready to auto-start the moment the app
    /// backgrounds — call as soon as the remote participant's video view exists (RealtimeKit
    /// hands it back late — often not yet at room-join — so this may be called more than once
    /// per call; a no-op once already armed).
    ///
    /// [remoteVideoView] doubles as both the PiP transition's animation source rect
    /// (`activeVideoCallSourceView`) and the view re-parented into the PiP window — but
    /// RealtimeKit hands back this raw UIView the instant the participant's video track
    /// exists, which can be *before* Compose's UIKitView interop has actually mounted it into
    /// the on-screen window hierarchy (view.window == nil). AVPictureInPictureController never
    /// becomes eligible for a source view that isn't part of a window, so if it's not attached
    /// yet, retry shortly instead of silently giving up forever.
    func start(remoteVideoView: UIView) {
        guard pipController == nil else { return }
        guard AVPictureInPictureController.isPictureInPictureSupported() else {
            NSLog("SRC-PIP: start() — isPictureInPictureSupported() is false, not arming")
            return
        }
        guard remoteVideoView.window != nil else {
            NSLog("SRC-PIP: start() — remoteVideoView not yet in a window, retrying in 0.3s")
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
                self?.start(remoteVideoView: remoteVideoView)
            }
            return
        }
        self.videoView = remoteVideoView

        let source = AVPictureInPictureController.ContentSource(
            activeVideoCallSourceView: remoteVideoView,
            contentViewController: contentViewController
        )
        let controller = AVPictureInPictureController(contentSource: source)
        controller.canStartPictureInPictureAutomaticallyFromInline = true
        controller.delegate = self
        pipController = controller
        NSLog("SRC-PIP: armed — sourceView frame=\(remoteVideoView.frame), inWindow=\(remoteVideoView.window != nil)")

        possibleObservation = controller.observe(\.isPictureInPicturePossible, options: [.new]) { _, change in
            NSLog("SRC-PIP: isPictureInPicturePossible=\(change.newValue ?? false)")
        }
    }

    /// Call once the call ends (`RtkCallSessionImpl.dispose()`) — tears down PiP eligibility
    /// entirely so a stale controller can't fire for the next call, and restores the video
    /// view to its Compose-managed parent if PiP happened to still be active.
    func stop() {
        if pipController?.isPictureInPictureActive == true {
            pipController?.stopPictureInPicture()
        }
        possibleObservation = nil
        restoreVideoView()
        pipController = nil
        videoView = nil
    }

    private func restoreVideoView() {
        guard let videoView = videoView, let superview = originalSuperview, videoView.superview !== superview else { return }
        videoView.removeFromSuperview()
        videoView.frame = originalFrame
        superview.addSubview(videoView)
    }
}

extension CallPictureInPictureManager: AVPictureInPictureControllerDelegate {
    func pictureInPictureControllerWillStartPictureInPicture(_ controller: AVPictureInPictureController) {
        NSLog("SRC-PIP: willStartPictureInPicture")
        guard let videoView = videoView else { return }
        originalSuperview = videoView.superview
        originalFrame = videoView.frame
        videoView.removeFromSuperview()
        videoView.frame = contentViewController.view.bounds
        videoView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        contentViewController.view.addSubview(videoView)
    }

    func pictureInPictureController(_ controller: AVPictureInPictureController, failedToStartPictureInPictureWithError error: Error) {
        NSLog("SRC-PIP: failedToStartPictureInPictureWithError — \(error.localizedDescription)")
    }

    func pictureInPictureControllerDidStopPictureInPicture(_ controller: AVPictureInPictureController) {
        NSLog("SRC-PIP: didStopPictureInPicture")
        restoreVideoView()
    }

    func pictureInPictureController(
        _ controller: AVPictureInPictureController,
        restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void
    ) {
        restoreVideoView()
        completionHandler(true)
    }
}
