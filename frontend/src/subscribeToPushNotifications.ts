import {getMessaging, getToken} from 'firebase/messaging';
import {getAuth, signInAnonymously} from 'firebase/auth';
import {authConfiguration} from "./api/config/AuthConfiguration";
import {NotificationsApi} from "./gen/msw-api-ts";

export const subscribeToPushNotifications = async (userToken: string) => {
    try {
        await Notification.requestPermission()
        // Step 1: Ensure Firebase auth is initialized
        const auth = getAuth();
        if (!auth.currentUser) {
            await signInAnonymously(auth);
        }

        // Step 2: Register the service worker
        const swRegistration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');

        // Step 3: Get the FCM token
        const messaging = getMessaging();
        const fcmToken = await getToken(messaging, {serviceWorkerRegistration: swRegistration});
        if (!fcmToken) {
            console.log('No FCM registration token available. Request permission to generate one.');
            return;
        }
        
        // Step 4: register token with backend
        const config = await authConfiguration(userToken);
        new NotificationsApi(config).registerForPushNotifications({token: fcmToken});
    } catch (err: any) {
        console.error('An error occurred while registering for push notifications:', err);
    }
};
