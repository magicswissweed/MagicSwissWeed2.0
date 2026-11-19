import './MswThemeSwitch.scss'
import {CSSProperties} from "react";
import {MswThemePreference, useTheme} from "../../theme/MswThemeContext";

// Inline styles (not a CSS class) so the live region stays invisible even if the
// stylesheet is missing, stale (e.g. cached PWA assets) or overridden.
const VISUALLY_HIDDEN: CSSProperties = {
    position: 'absolute',
    width: 1,
    height: 1,
    padding: 0,
    margin: -1,
    overflow: 'hidden',
    clip: 'rect(0, 0, 0, 0)',
    clipPath: 'inset(50%)',
    whiteSpace: 'nowrap',
    border: 0,
    fontSize: 0,
    lineHeight: 0,
};

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
        <div className="theme-toggle-wrapper" style={{position: 'relative'}}>
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
            <span className="visually-hidden" style={VISUALLY_HIDDEN} aria-live="polite" aria-atomic="true">{label}</span>
        </div>
    );
};
