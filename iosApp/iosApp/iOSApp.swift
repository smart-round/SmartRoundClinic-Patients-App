import SwiftUI
import Shared
import FirebaseCore
import FirebaseMessaging
import UserNotifications
import RealtimeKit
import RealtimeKitUI

/// Retained for the lifetime of an active meeting.
/// rtkUI owns the delegate relationship with RtkSetupViewController; if it
/// is released before the user taps "Join", the meeting room VC is never shown.
private var activeMeeting: RealtimeKitUI?

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        application.registerForRemoteNotifications()
        return true
    }

    // Forward APNS device token to Firebase so it can exchange it for an FCM token.
    // kmpnotifier's internal MessagingDelegate picks this up and fires onNewToken.
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }

    // Required when FirebaseAppDelegateProxyEnabled = NO — pass the raw remote
    // notification to Firebase so it can update FCM state (ack, analytics, etc.)
    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        Messaging.messaging().appDidReceiveMessage(userInfo)
        completionHandler(.newData)
    }

    // Show banner + sound when a notification arrives while the app is in the foreground
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        FirebaseApp.configure()
        NotifierManager.shared.initialize(
            configuration: NotificationPlatformConfigurationIos(
                showPushNotification: true,
                askNotificationPermissionOnStart: true,
                notificationSoundName: nil
            )
        )
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
