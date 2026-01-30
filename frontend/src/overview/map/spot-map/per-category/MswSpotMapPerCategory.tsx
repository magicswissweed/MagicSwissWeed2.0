import React, {useCallback, useEffect, useRef, useState} from "react";
import {GoogleMap, InfoWindow} from "@react-google-maps/api";
import {MarkerClusterer} from "@googlemaps/markerclusterer";
import './MswSpotMapPerCategory.scss';
import {FlowColorEnum, SpotModel} from "../../../../model/SpotModel";
import {useGoogleMaps} from "../../../../map-provider/GoogleMapsProvider";

export const mapCenter = {lat: 47.05, lng: 8.30}; // Luzern / ca. Mitte der Schweiz

const getOffsetPosition = (latitude: number, longitude: number, index: number) => {
    const offset = 0.0005 * (index + 1);
    return {lat: latitude + offset, lng: longitude + offset};
};

interface MswSpotMapPropsPerCategory {
    spots: SpotModel[];
}

export const MswSpotMapPerCategory = ({spots}: MswSpotMapPropsPerCategory) => {
    const {isLoaded, loadError} = useGoogleMaps();
    const [selectedSpot, setSelectedSpot] = useState<SpotModel | null>(null);

    const mapRef = useRef<google.maps.Map | null>(null);
    const clustererRef = useRef<MarkerClusterer | null>(null);

    const clearMap = () => {
        clustererRef.current?.clearMarkers();
        clustererRef.current = null;
    };

    const renderClusterIcon = ({markers, count, position}: any) => {
        const colors = markers.map((m: any) => m.customColor);
        const uniqueColors = new Set<string>(colors);

        let color = FlowColorEnum.RED;
        if (uniqueColors.has(FlowColorEnum.GREEN)) {
            color = FlowColorEnum.GREEN;
        } else if (uniqueColors.has(FlowColorEnum.ORANGE)) {
            color = FlowColorEnum.ORANGE;
        }

        return new google.maps.Marker({
            position,
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 14,
                fillColor: color,
                fillOpacity: 0.9,
                strokeWeight: 1,
                strokeColor: "white",
                labelOrigin: new google.maps.Point(0, 0),
            },
            label: {
                text: String(count),
                color: "white",
                fontWeight: "bold",
            },
        });
    };

    const createMarkers = (spots: SpotModel[]) => {
        return spots.map((spot, index) => {
            const position = getOffsetPosition(spot.station.latitude, spot.station.longitude, index);

            const marker = new google.maps.Marker({
                position,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 10,
                    fillColor: spot.flowStatus.toString(),
                    fillOpacity: 1,
                    strokeWeight: 1,
                    strokeColor: "white",
                },
            });

            marker.addListener("click", () => setSelectedSpot(spot));

            (marker as any).customColor = spot.flowStatus.toString();
            return marker;
        });
    };

    const updateMap = useCallback(() => {
        if (!mapRef.current || !isLoaded) return;

        clearMap();

        const markers = createMarkers(spots);
        clustererRef.current = new MarkerClusterer({
            markers,
            map: mapRef.current,
            renderer: {render: renderClusterIcon}
        });
    }, [spots, isLoaded]);

    useEffect(() => {
        updateMap();
    }, [updateMap]);

    const handleMapLoad = (map: google.maps.Map) => {
        mapRef.current = map;
        updateMap();
    };

    if (loadError) return <p>Error loading maps</p>;
    if (!isLoaded) return <p>Loading maps...</p>;

    return (
        <div className="map-container">
            <GoogleMap
                mapContainerStyle={{width: "100%", height: "100%"}}
                zoom={8}
                center={mapCenter}
                onLoad={handleMapLoad}
            >
                {selectedSpot && (
                    <InfoWindow
                        position={{lat: selectedSpot.station.latitude, lng: selectedSpot.station.longitude}}
                        onCloseClick={() => setSelectedSpot(null)}
                    >
                        <p style={{textTransform: "none"}}>{selectedSpot.name}: {selectedSpot.currentSample.flow} m³/s</p>
                    </InfoWindow>
                )}
            </GoogleMap>
        </div>
    );
};
