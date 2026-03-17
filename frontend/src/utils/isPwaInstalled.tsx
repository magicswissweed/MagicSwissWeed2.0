import {useEffect, useState} from "react";

export function usePwaInstalled() {
    const [isInstalled, setIsInstalled] = useState(false);

    useEffect(() => {
        const checkInstalled = () => {
            const standalone =
                window.matchMedia("(display-mode: standalone)").matches ||
                (window.navigator as any).standalone === true;
            setIsInstalled(standalone);
        };

        checkInstalled();

        // listen for changes in display-mode
        const handler = (e: any) => checkInstalled();
        window.matchMedia("(display-mode: standalone)").addEventListener("change", handler);

        return () => {
            window.matchMedia("(display-mode: standalone)").removeEventListener("change", handler);
        };
    }, []);

    return isInstalled;
}
