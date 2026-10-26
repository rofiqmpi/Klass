import SwiftUI
import FirebaseCore
import GoogleSignIn
import Shared

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()

        let bridge = IosSignInBridge.shared

        bridge.onSignInRequested = {
            guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = scene.windows.first?.rootViewController else {
                bridge.completeSignIn(email: nil, name: nil, photoUrl: nil, errorMsg: "সাইন-ইন UI চালু করা যায়নি")
                return
            }

            GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
                if let error {
                    let isCanceled = (error as NSError).code == GIDSignInErrorCode.canceled.rawValue
                    bridge.completeSignIn(
                        email: nil,
                        name: nil,
                        photoUrl: nil,
                        errorMsg: isCanceled ? nil : error.localizedDescription,
                    )
                    return
                }

                let user = result?.user
                bridge.completeSignIn(
                    email: user?.profile?.email,
                    name: user?.profile?.name,
                    photoUrl: user?.profile?.imageURL(withDimension: 256)?.absoluteString,
                    errorMsg: nil,
                )
            }
        }

        bridge.onSignOutRequested = {
            GIDSignIn.sharedInstance.signOut()
        }

        // The CLIENT_ID key is added to GoogleService-Info.plist once the iOS
        // OAuth client is created in the Firebase console.
        if let plistPath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
           let plist = NSDictionary(contentsOfFile: plistPath),
           let clientID = plist["CLIENT_ID"] as? String {
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
