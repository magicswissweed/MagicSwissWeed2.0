import {useEffect, useState} from 'react';
import {MswTheme, MswThemeContext} from "./MswThemeContext";

const getInitialTheme = (): MswTheme => {
    const stored = localStorage.getItem('theme') as MswTheme | null;
    if (stored) return stored;

    return window.matchMedia('(prefers-color-scheme: dark)').matches
        ? 'dark'
        : 'light';
};

export const MswThemeProvider = ({children}: { children: React.ReactNode }) => {
    const [theme, setTheme] = useState<MswTheme>(getInitialTheme);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
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
};
