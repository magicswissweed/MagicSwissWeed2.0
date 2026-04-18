import {useEffect, useState} from 'react';
import {MswTheme, MswThemeContext, MswThemePreference} from "./MswThemeContext";

const STORAGE_KEY = 'theme';

const getInitialPreference = (): MswThemePreference => {
    const stored = localStorage.getItem(STORAGE_KEY) as MswThemePreference | null;
    // Treat a legacy stored value of 'light' or 'dark' as-is; anything else → 'system'
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
    return 'system';
};

const resolveTheme = (preference: MswThemePreference, systemDark: boolean): MswTheme => {
    if (preference === 'system') return systemDark ? 'dark' : 'light';
    return preference;
};

export const MswThemeProvider = ({children}: { children: React.ReactNode }) => {
    const [preference, setPreference] = useState<MswThemePreference>(getInitialPreference);
    const [systemDark, setSystemDark] = useState(
        () => window.matchMedia('(prefers-color-scheme: dark)').matches
    );

    const theme = resolveTheme(preference, systemDark);

    // Apply theme to DOM and persist preference
    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(STORAGE_KEY, preference);
        syncAndroidToolbarWithTheme();
    }, [preference, theme]);

    // Track OS-level dark mode — only affects the UI when preference is 'system'
    useEffect(() => {
        const media = window.matchMedia('(prefers-color-scheme: dark)');
        const listener = (e: MediaQueryListEvent) => setSystemDark(e.matches);
        media.addEventListener('change', listener);
        return () => media.removeEventListener('change', listener);
    }, []);

    return (
        <MswThemeContext.Provider value={{theme, preference, setPreference}}>
            {children}
        </MswThemeContext.Provider>
    );

    function syncAndroidToolbarWithTheme() {
        const metaThemeColor = document.querySelector(
            'meta[name="theme-color"]'
        );

        if (metaThemeColor) {
            const bg = getComputedStyle(document.documentElement)
                .getPropertyValue('--bg')
                .trim();

            if (bg) {
                metaThemeColor.setAttribute('content', bg);
            }
        }
    }
};
