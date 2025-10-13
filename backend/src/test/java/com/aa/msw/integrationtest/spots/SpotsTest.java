package com.aa.msw.integrationtest.spots;

import com.aa.msw.gen.api.AddPrivateSpotRequest;
import com.aa.msw.gen.api.ApiSpot;
import com.aa.msw.integrationtest.IntegrationTest;
import com.aa.msw.integrationtest.TestUser;
import com.google.gson.Gson;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

public class SpotsTest extends IntegrationTest {
    public static final String API_PREFIX = "/api/v1";
    public static final String ALL_SPOTS_URL = API_PREFIX + "/spots";
    public static final String SPOT_URL = API_PREFIX + "/spot";

    @Test
    public void shouldBeAbleToFetchAllSpots() {
        getTemplateRequest(TestUser.THE_ONE)
                .get(ALL_SPOTS_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("spots", hasSize(5));
    }

    @Test
    public void shouldBeAbleToAddPrivateSpot() {
        getTemplateRequest(TestUser.THE_ONE)
                .contentType(ContentType.JSON)
                .when().body(
                        new Gson().toJson(
                                new AddPrivateSpotRequest(
                                        new ApiSpot(
                                                UUID.randomUUID(),
                                                "someSpotName",
                                                2018,
                                                ApiSpot.SpotTypeEnum.RIVER_SURF,
                                                false,
                                                20,
                                                30,
                                                null,
                                                false
                                        ),
                                        0
                                )
                        )
                )
                .post(SPOT_URL)
                .then()
                .statusCode(HttpStatus.OK.value());

        getTemplateRequest(TestUser.THE_ONE)
                .get(ALL_SPOTS_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("spots", hasSize(6));
    }
}
