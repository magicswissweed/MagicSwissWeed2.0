package com.aa.msw.auth;

import com.google.firebase.messaging.FirebaseMessaging;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Service
public class FirebaseInitializeMock {
    @Bean
    @Profile("test")
    FirebaseMessaging firebaseMessaging() {
        // return a mock or dummy instance
        return Mockito.mock(FirebaseMessaging.class);
    }
}
