import {createContext, useContext} from 'react';

export type MswTheme = 'light' | 'dark';

type ThemeContextType = {
    theme: MswTheme;
    setTheme: (t: MswTheme) => void;
};

export const MswThemeContext = createContext<ThemeContextType | null>(null);

export const useTheme = () => {
    const ctx = useContext(MswThemeContext);
    if (!ctx) {
        throw new Error('useTheme must be used inside MswThemeProvider');
    }
    return ctx;
};
