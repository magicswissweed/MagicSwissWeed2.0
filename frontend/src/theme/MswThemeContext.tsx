import {createContext, useContext} from 'react';

export type MswTheme = 'light' | 'dark';
export type MswThemePreference = 'light' | 'system' | 'dark';

type ThemeContextType = {
    /** Resolved theme actually applied to the UI (never 'system'). */
    theme: MswTheme;
    /** What the user has chosen: 'light', 'dark', or 'system' (follow OS). */
    preference: MswThemePreference;
    setPreference: (p: MswThemePreference) => void;
};

export const MswThemeContext = createContext<ThemeContextType | null>(null);

export const useTheme = () => {
    const ctx = useContext(MswThemeContext);
    if (!ctx) {
        throw new Error('useTheme must be used inside MswThemeProvider');
    }
    return ctx;
};
