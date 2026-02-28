/**
 * Detects if the current device is running iOS
 */
export function isIos(): boolean {
    const userAgent = window.navigator.userAgent.toLowerCase();
    return /iphone|ipad|ipod/.test(userAgent);
}

/**
 * Detects if the app is running as a PWA (installed)
 */
export function isPwa(): boolean {
    return (
        window.matchMedia("(display-mode: standalone)").matches ||
        (window.navigator as any).standalone === true
    );
}

/**
 * Detects if the app is running as a PWA on an iOS device
 */
export function isIosPwa(): boolean {
    return isIos() && isPwa();
}
