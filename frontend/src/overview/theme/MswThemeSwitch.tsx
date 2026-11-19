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

    // The current theme is exposed to assistive tech through the button's
    // aria-label only. A separate hidden aria-live text element was rendered
    // visibly on some mobile browsers after toggling and overlapped the
    // Log Out button in the profile modal, so there is deliberately no extra
    // text node here.
    return (
        <button
            type="button"
            className={`theme-toggle pref-${preference}`}
            onClick={cycle}
            aria-label={`Theme: ${label}`}
            title={label}
        >
            <span className="icon sun" aria-hidden="true"/>
            <span className="icon system" aria-hidden="true"/>
            <span className="icon moon" aria-hidden="true"/>
            <span className="thumb" aria-hidden="true"/>
        </button>
    );
};
