import {MswTheme} from "./MswThemeContext";

export type ThemeDependingColors = {
    invertedRgb: string;
}

export function getThemeDependingColors(theme: MswTheme): ThemeDependingColors {
    if (theme === 'light') {
        return {
            invertedRgb: '0, 0, 0',
        };
    } else {
        return {
            invertedRgb: '255, 255, 255',
        };
    }
}
