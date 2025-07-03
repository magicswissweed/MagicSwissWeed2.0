import React, {useCallback, useEffect, useRef} from "react";
import {GoogleMap, useLoadScript} from "@react-google-maps/api";
import {MarkerClusterer} from "@googlemaps/markerclusterer";
import './MswSpotMapPerCategory.scss';
import {SpotModel} from "../../../../model/SpotModel";

export const mapCenter = {lat: 47.05, lng: 8.30}; // Luzern / ca. Mitte der Schweiz

const getOffsetPosition = (latitude: number, longitude: number, index: number) => {
    const offset = 0.0005 * (index + 1);
    return {lat: latitude + offset, lng: longitude + offset};
};

interface MswSpotMapPropsPerCategory {
    spots: SpotModel[];
}

export const MswSpotMapPerCategory = ({spots}: MswSpotMapPropsPerCategory) => {
    const {isLoaded, loadError} = useLoadScript({
        googleMapsApiKey: process.env.REACT_APP_GOOGLE_MAPS_API_KEY!,
    });

    const mapRef = useRef<google.maps.Map | null>(null);
    const clustererRef = useRef<MarkerClusterer | null>(null);

    const clearMap = () => {
        clustererRef.current?.clearMarkers();
        clustererRef.current = null;
    };

    const renderClusterIcon = ({markers, count, position}: any) => {
        const colors = markers.map((m: any) => m.customColor);
        const uniqueColors = new Set<string>(colors);

        let color = "blue"; // default color for clustered spots if not all spots have the same color
        if (uniqueColors.size === 1) {
            color = [...uniqueColors][0];
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
            />
        </div>
    );
};
