package com.aa.msw.api.notification;

import com.aa.msw.gen.api.NotificationsApi;
import com.aa.msw.gen.api.PushNotificationSubscription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class NotificationApiController implements NotificationsApi {

    private final NotificationApiService notificationApiService;

    public NotificationApiController(NotificationApiService notificationApiService) {
        this.notificationApiService = notificationApiService;
    }

    @Override
    public ResponseEntity<Void> registerForPushNotifications(PushNotificationSubscription pushNotificationSubscription) {
        this.notificationApiService.subscribe(pushNotificationSubscription);
        return new ResponseEntity<>(OK);
    }

    // TODO: remove after testing phase
    @Override
    public ResponseEntity<Void> triggerTestNotifications() {
        try {
            this.notificationApiService.triggerTestNotifications();
        } catch (Exception e) {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(OK);
    }
}
