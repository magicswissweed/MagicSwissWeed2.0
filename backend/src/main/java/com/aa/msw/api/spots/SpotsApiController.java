package com.aa.msw.api.spots;

import com.aa.msw.database.exceptions.NoSampleAvailableException;
import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.gen.api.*;
import com.aa.msw.model.Spot;
import com.aa.msw.model.SpotTypeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SpotsApiController implements SpotsApi {
    private final SpotsApiService spotsApiService;

    public SpotsApiController(SpotsApiService spotsApiService) {
        this.spotsApiService = spotsApiService;
    }

    private static SpotTypeEnum getSpotTypeEnum(ApiSpot.SpotTypeEnum spotType) {
        return switch (spotType) {
            case RIVER_SURF -> SpotTypeEnum.RIVER_SURF;
            case BUNGEE_SURF -> SpotTypeEnum.BUNGEE_SURF;
        };
    }

    @Override
    public ResponseEntity<Void> orderSpots(List<UUID> UUID) {
        List<SpotId> orderedSpotIds = UUID.stream().map(SpotId::new).toList();
        spotsApiService.orderSpots(orderedSpotIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ApiSpotInformation>> getSpots() {
        return ResponseEntity.ok(spotsApiService.getSpots());
    }

    @Override
    public ResponseEntity<Void> addPrivateSpot(AddPrivateSpotRequest addPrivateSpotRequest) {
        ApiSpot apiSpot = addPrivateSpotRequest.getSpot();

        Spot spot = new Spot(
                new SpotId(apiSpot.getId(), false),
                false,
                getSpotTypeEnum(apiSpot.getSpotType()),
                apiSpot.getName(),
                apiSpot.getStationId(),
                apiSpot.getMinFlow(),
                apiSpot.getMaxFlow()
        );
        try {
            spotsApiService.addPrivateSpot(
                    spot,
                    addPrivateSpotRequest.getPosition(),
                    addPrivateSpotRequest.getSpot().getWithNotification());
        } catch (NoSampleAvailableException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> editPrivateSpot(UUID spotId, EditPrivateSpotRequest editPrivateSpotRequest) {
        ApiSpot apiSpot = editPrivateSpotRequest.getSpot();

        Spot updatedSpot = new Spot(
                new SpotId(apiSpot.getId(), false),
                false,
                getSpotTypeEnum(apiSpot.getSpotType()),
                apiSpot.getName(),
                apiSpot.getStationId(),
                apiSpot.getMinFlow(),
                apiSpot.getMaxFlow()
        );
        try {
            spotsApiService.editSpot(updatedSpot, apiSpot.getWithNotification());
        } catch (NoSampleAvailableException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deletePrivateSpot(UUID spotId) {
        spotsApiService.deletePrivateSpot(new SpotId(spotId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
