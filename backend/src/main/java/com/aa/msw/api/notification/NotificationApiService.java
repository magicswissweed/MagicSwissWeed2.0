package com.aa.msw.api.notification;

import com.aa.msw.database.repository.dao.NotificationDao;
import com.aa.msw.gen.api.PushNotificationSubscription;
import org.springframework.stereotype.Service;

@Service
public class NotificationApiService {

    private final NotificationDao notificationDao;

    NotificationApiService(NotificationDao notificationDao) {
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
}
