package com.aa.msw;

import com.aa.msw.database.helpers.UserToSpot;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.NotificationDao;
import com.aa.msw.database.repository.dao.UserToSpotDao;
import com.aa.msw.model.NotificationSubscription;
import com.aa.msw.model.Spot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationService {
    private final NotificationDao notificationDao;
    private final FirebaseMessaging fcm;
    private final UserToSpotDao userToSpotDao;

    NotificationService(
            FirebaseMessaging fcm,
            NotificationDao notificationDao, UserToSpotDao userToSpotDao) {
        this.fcm = fcm;
        this.notificationDao = notificationDao;
        this.userToSpotDao = userToSpotDao;
    }

    public void sendNotificationsForSpots(Set<Spot> updatedSpots) {
        Set<UserId> userIdsToNotify = new HashSet<>();
        updatedSpots.forEach(spot -> {
            if (!spot.isPublic()) {
                UserToSpot userToSpot = userToSpotDao.get(spot.spotId());
                userIdsToNotify.add(userToSpot.userId());
            }
        });
        userIdsToNotify.forEach(this::sendNotifications);
    }

    public void sendNotifications(UserId userId) {
        // TODO?: for the moment we just send a static message. Maybe in the future we want to have a body with infos about the updates
        Map<String, String> messageData = Map.of(
                "title", "Updates for your spots",
                "body", "Check out what changed on magicswissweed.ch"
        );

        Set<NotificationSubscription> subscriptions = notificationDao.getSubscriptionsForUser(userId);
        subscriptions.forEach(subscription ->
                sendNotification(subscription.subscriptionToken(), messageData)
        );

    }

    public void sendNotification(String subscriptionToken, Map<String, String> messageData) {
        Message msg = Message.builder()
                .setToken(subscriptionToken)
                .putAllData(messageData)
                .build();

        try {
            fcm.send(msg);
        } catch (FirebaseMessagingException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }
}
