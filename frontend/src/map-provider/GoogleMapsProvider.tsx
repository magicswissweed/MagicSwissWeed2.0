import {createContext, useContext} from "react";
import {useLoadScript} from "@react-google-maps/api";

type GoogleMapsContextType = {
    isLoaded: boolean;
    loadError: Error | undefined;
};

const GoogleMapsContext = createContext<GoogleMapsContextType | undefined>(
    undefined
);

export const GoogleMapsProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const {isLoaded, loadError} = useLoadScript({
        googleMapsApiKey: process.env.REACT_APP_GOOGLE_MAPS_API_KEY!,
    });

    return (
        <GoogleMapsContext.Provider value={{isLoaded, loadError}}>
            {children}
        </GoogleMapsContext.Provider>
    );
};

export const useGoogleMaps = () => {
    const ctx = useContext(GoogleMapsContext);
    if (!ctx) {
        throw new Error("useGoogleMaps must be used inside GoogleMapsProvider");
    }
    return ctx;
};
