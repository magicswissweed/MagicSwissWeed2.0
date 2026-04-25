import './MswThemeSwitch.scss'
import {MswThemePreference, useTheme} from "../../theme/MswThemeContext";

const CYCLE: MswThemePreference[] = ['light', 'system', 'dark'];

const LABELS: Record<MswThemePreference, string> = {
    light: 'Light mode',
    system: 'System default',
    dark: 'Dark mode',
};

export const MswThemeSwitch = () => {
    const {preference, setPreference} = useTheme();

    const cycle = () => {
        const next = CYCLE[(CYCLE.indexOf(preference) + 1) % CYCLE.length];
        setPreference(next);
    };

    const label = LABELS[preference];

    return (
        <>
            <button
                type="button"
                className={`theme-toggle pref-${preference}`}
                onClick={cycle}
                aria-label={`Theme: ${label}`}
                title={label}
            >
                <span className="icon sun"/>
                <span className="icon system"/>
                <span className="icon moon"/>
                <span className="thumb"/>
            </button>
            <span className="sr-only" aria-live="polite" aria-atomic="true">{label}</span>
        </>
    );
};
