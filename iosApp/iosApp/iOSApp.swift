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

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Do NOT set UNUserNotificationCenter.current().delegate here.
        // NotifierManager.initialize() sets itself as the UNUserNotificationCenterDelegate
        // so it can fire onNotificationClicked and show foreground banners.
        // Overriding that delegate here would break kmpnotifier's handling.
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
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // Firebase and NotifierManager must be initialised before doInitKoin() because
        // initKoin() calls setupNotificationListener(), which immediately launches a
        // coroutine that calls NotifierManager.getPushNotifier(). If the factory is not
        // ready by then the app crashes with IllegalStateException.
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
