package com.aa.msw.auth;

import com.aa.msw.api.WebConfig;
import com.aa.msw.database.repository.dao.UserDao;
import com.aa.msw.database.services.UserDbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The integration tests run with the permissive TestSecurityConfiguration, so the production
 * SecurityConfiguration is only covered here.
 */
@WebMvcTest(
        controllers = SecurityConfigurationTest.DummyController.class,
        properties = "spring.profiles.active=securitytest"
)
@Import({SecurityConfiguration.class, WebConfig.class, SecurityConfigurationTest.DummyController.class})
class SecurityConfigurationTest {

    private static final String ORIGIN = "https://msw.ponteshare.ch";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserDao userDao;
    @MockitoBean
    private UserDbService userDbService;

    @Test
    void preflightIsAnsweredWithoutAuthentication() throws Exception {
        mvc.perform(options("/api/v1/spot")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    void publicEndpointIsReachableWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/spots/public").header("Origin", ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/user").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class DummyController {
        @GetMapping("/api/v1/spots/public")
        String publicSpots() {
            return "ok";
        }

        @GetMapping("/api/v1/user")
        String user() {
            return "ok";
        }
    }
}
