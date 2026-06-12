import React, {useState} from 'react';
import {GoogleMap, InfoWindow, Marker, MarkerClusterer} from '@react-google-maps/api';
import {ApiStation} from "../../../gen/msw-api-ts";
import {mapCenter} from "../spot-map/per-category/MswSpotMapPerCategory";
import {useGoogleMaps} from "../../../map-provider/GoogleMapsProvider";

export const MswStationMap = (props: { stations: ApiStation[] }) => {
    const {isLoaded} = useGoogleMaps();
    const [selectedStation, setSelectedStation] = useState<ApiStation | null>(null);

    if (!isLoaded) {
        return <p>Loading maps...</p>;
    }

    // sometimes the externalId is already in the label (e.g. switzerland does that)
    let label = selectedStation?.label.includes(selectedStation?.id.externalId) ?
        selectedStation.label :
        selectedStation?.id.externalId + " - " + selectedStation?.label;
    return (
        <GoogleMap
            mapContainerStyle={{
                width: "100%",
                height: "100%",
            }}
            zoom={8}
            center={mapCenter}
            onClick={() => setSelectedStation(null)}
        >
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
                    options={{headerDisabled: true}}
                >
                    <div>
                        <p>{label}</p>
                    </div>
                </InfoWindow>
            )}
        </GoogleMap>
    );
};
