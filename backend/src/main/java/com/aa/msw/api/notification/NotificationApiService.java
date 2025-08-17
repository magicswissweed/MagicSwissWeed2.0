package com.aa.msw.api.notification;

import com.aa.msw.NotificationService;
import com.aa.msw.database.repository.dao.NotificationDao;
import com.aa.msw.gen.api.PushNotificationSubscription;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationApiService {

    private final NotificationDao notificationDao;
    private final NotificationService notificationService;

    NotificationApiService(
            NotificationService notificationService,
            NotificationDao notificationDao) {
        this.notificationService = notificationService;
        this.notificationDao = notificationDao;
    }

    public void subscribe(PushNotificationSubscription apiSubscription) {
        this.notificationDao.persistSubscriptionIfNotExists(apiSubscription.getToken());
    }

    // TODO: implement unsubscribe (maybe simply if there is no subscribed spot anymore??)
//    public void unsubscribe(String endpoint) {
//        subscriptions = subscriptions.stream()
//                .filter(s -> !endpoint.equals(s.getEndpoint()))
//                .collect(Collectors.toList());
//    }

    // TODO: remove if messaging works as expected (after testing phase)
    public void triggerTestNotifications() {
        Map<String, String> messageData = Map.of(
                "title", "Server says hello!",
                "body", "This is a test notification sent by the server"
        );
        notificationDao.getAll().forEach(subscription ->
                notificationService.sendNotification(subscription.subscriptionToken(), messageData));
    }
}
