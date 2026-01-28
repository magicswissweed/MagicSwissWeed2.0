import React, {useState} from 'react';
import {GoogleMap, InfoWindow, Marker, MarkerClusterer, useLoadScript} from '@react-google-maps/api';
import {ApiStation} from "../../../gen/msw-api-ts";
import {mapCenter} from "../spot-map/per-category/MswSpotMapPerCategory";

export const MswStationMap = (props: { stations: ApiStation[] }) => {
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.REACT_APP_GOOGLE_MAPS_API_KEY!,
    });
    const [selectedStation, setSelectedStation] = useState<ApiStation | null>(null);

    if (!isLoaded) return <p>Loading maps...</p>;

    return (
        <GoogleMap
            mapContainerStyle={{
                width: "100%",
                height: "100%",
            }}
            zoom={8}
            center={mapCenter}>

            <MarkerClusterer>
                {(clusterer) => (
                    <>
                        {props.stations.map((station, index) => (
                            <Marker
                                key={index}
                                position={{lat: station.latitude, lng: station.longitude}}
                                clusterer={clusterer}
                                onClick={() => setSelectedStation(station)}
                            />
                        ))}
                    </>
                )}
            </MarkerClusterer>

            {selectedStation && (
                <InfoWindow
                    position={{lat: selectedStation.latitude, lng: selectedStation.longitude}}
                    onCloseClick={() => setSelectedStation(null)}
                >
                    <div>
                        <p>{selectedStation.id.externalId} - {selectedStation.label}</p>
                    </div>
                </InfoWindow>
            )}
        </GoogleMap>
    );
};
