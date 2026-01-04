package com.aa.msw.source.swiss.hydrodaten.model.station;

import java.util.List;

public record HydroStation(String key, List<String> names, String label) {
}
