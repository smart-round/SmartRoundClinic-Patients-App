import SwiftUI
import Shared
import RealtimeKit
import RealtimeKitUI

/// Retained for the lifetime of an active meeting.
/// rtkUI owns the delegate relationship with RtkSetupViewController; if it
/// is released before the user taps "Join", the meeting room VC is never shown.
private var activeMeeting: RealtimeKitUI?

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitKoin()
        wireRealtimeMeetingBridge()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func wireRealtimeMeetingBridge() {
        RealtimeMeetingBridge.shared.factory = { authToken, enableVideo, onLeave in
            let rtkUI = RealtimeKitUI(
                meetingInfo: RtkMeetingInfo(
                    authToken: authToken,
                    enableAudio: true,
                    enableVideo: enableVideo.boolValue
                )
            )
            activeMeeting = rtkUI   // keep alive so the delegate link stays valid

            // MeetingViewController never calls self.dismiss — it only calls the completion.
            // We must dismiss the entire RTK modal chain here before notifying Kotlin.
            // Weak ref avoids a retain cycle; UIKit retains vc while it's presented.
            weak var setupVC: UIViewController?
            let vc = rtkUI.startMeeting {
                activeMeeting = nil
                if let presenter = setupVC?.presentingViewController {
                    presenter.dismiss(animated: true) { onLeave() }
                } else {
                    // Presenter is gone — Kotlin onDispose will clean up the Compose side.
                    onLeave()
                }
            }
            setupVC = vc
            vc.modalPresentationStyle = .fullScreen
            vc.overrideUserInterfaceStyle = .dark
            return vc
        }
    }
}
