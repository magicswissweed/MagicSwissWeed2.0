package com.aa.msw.notifications;

import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.NotificationDao;
import com.aa.msw.model.FlowStatusEnum;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationDao notificationDao;
    private final FirebaseMessaging fcm;

    NotificationService(
            FirebaseMessaging fcm,
            NotificationDao notificationDao) {
        this.fcm = fcm;
        this.notificationDao = notificationDao;
    }

    public void sendNotificationsForSpots(Set<NotificationSpotInfo> updatedSpots) {
        Map<UserId, Set<NotificationSpotInfo>> userToUpdatedSpots = updatedSpots.stream()
                .filter(i -> !i.spot().isPublic())
                .filter(i -> i.userToSpot().withNotification())
                .map(i -> new AbstractMap.SimpleImmutableEntry<>(i.userToSpot().userId(), i))
                .collect(
                        Collectors.groupingBy(
                                AbstractMap.SimpleImmutableEntry::getKey,
                                Collectors.mapping(AbstractMap.SimpleImmutableEntry::getValue, Collectors.toSet())
                        )
                );

        userToUpdatedSpots.forEach(this::sendNotificationToAllSubscribedClientsOfUser);
    }

    private void sendNotificationToAllSubscribedClientsOfUser(UserId userId, Set<NotificationSpotInfo> spotInfos) {
        boolean hasNewlySurfableSpots = spotInfos.stream().anyMatch(i -> i.flowStatus().equals(FlowStatusEnum.GOOD));
        boolean hasImprovedForecasts = spotInfos.stream().anyMatch(i -> i.flowStatus().equals(FlowStatusEnum.TENDENCY_TO_BECOME_GOOD));

        Set<String> newlySurfableSpotNames = spotInfos.stream()
                .filter(i -> i.flowStatus().equals(FlowStatusEnum.GOOD))
                .map(i -> i.spot().name())
                .collect(Collectors.toSet());
        String newlySurfableSpotsMessage = hasNewlySurfableSpots ?
                "You can now surf at: " + String.join(",", newlySurfableSpotNames) : "";

        Set<String> improvedForecastsSpotNames = spotInfos.stream()
                .filter(i -> i.flowStatus().equals(FlowStatusEnum.TENDENCY_TO_BECOME_GOOD))
                .map(i -> i.spot().name())
                .collect(Collectors.toSet());
        String improvedForecastsSpotsMessage = hasImprovedForecasts ?
                "Improved forecasts at: " + String.join(",", improvedForecastsSpotNames) : "";

        Set<String> messageBodyLines = Set.of(newlySurfableSpotsMessage, improvedForecastsSpotsMessage);

        String title = hasNewlySurfableSpots ? "Surf's up" : "Improved forecasts";
        String body = String.join("\n", messageBodyLines);

        Map<String, String> messageData = Map.of(
                "title", title,
                "body", body
        );

        sendNotificationToAllSubscribedClientsOfUser(userId, messageData);
    }

    private void sendNotificationToAllSubscribedClientsOfUser(UserId userId, Map<String, String> messageData) {
        notificationDao
                .getSubscriptionsForUser(userId)
                .forEach(subscription -> sendNotificationToSubscribedClient(subscription.subscriptionToken(), messageData));
    }

    public void sendNotificationToSubscribedClient(String subscriptionToken, Map<String, String> messageData) {
        Message msg = Message.builder()
                .setToken(subscriptionToken)
                .putAllData(messageData)
                .build();

        try {
            fcm.send(msg);
        } catch (FirebaseMessagingException e) {
            if (e.getMessage().contains("Requested entity was not found")) {
                notificationDao.deleteSubscriptionToken(subscriptionToken);
            } else {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }
}
