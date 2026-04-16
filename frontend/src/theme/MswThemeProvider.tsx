import {useEffect, useState} from 'react';
import {MswTheme, MswThemeContext} from "./MswThemeContext";

const THEME_COOKIE = 'theme';
const COOKIE_MAX_AGE_SECONDS = 365 * 24 * 60 * 60;

const getThemeCookie = (): MswTheme | null => {
    const match = document.cookie.split('; ').find(row => row.startsWith(THEME_COOKIE + '='));
    if (!match) return null;
    const value = match.split('=')[1] as MswTheme;
    return value === 'dark' || value === 'light' ? value : null;
};

const setThemeCookie = (theme: MswTheme) => {
    const secure = location.protocol === 'https:' ? '; Secure' : '';
    document.cookie = `${THEME_COOKIE}=${theme}; max-age=${COOKIE_MAX_AGE_SECONDS}; path=/; SameSite=Lax${secure}`;
};

const getInitialTheme = (): MswTheme => {
    const stored = getThemeCookie();
    if (stored) return stored;

    return window.matchMedia('(prefers-color-scheme: dark)').matches
        ? 'dark'
        : 'light';
};

export const MswThemeProvider = ({children}: { children: React.ReactNode }) => {
    const [theme, setTheme] = useState<MswTheme>(getInitialTheme);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        setThemeCookie(theme);

        syncAndroidToolbarWithTheme();
    }, [theme]);

    useEffect(() => {
        const media = window.matchMedia('(prefers-color-scheme: dark)');
        const listener = () => setTheme(media.matches ? 'dark' : 'light');

        media.addEventListener('change', listener);
        return () => media.removeEventListener('change', listener);
    }, []);

    return (
        <MswThemeContext.Provider value={{theme, setTheme}}>
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
