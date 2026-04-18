import './MswThemeSwitch.scss'
import {MswThemePreference, useTheme} from "../../theme/MswThemeContext";

const CYCLE: MswThemePreference[] = ['light', 'system', 'dark'];

export const MswThemeSwitch = () => {
    const {preference, setPreference} = useTheme();

    const cycle = () => {
        const next = CYCLE[(CYCLE.indexOf(preference) + 1) % CYCLE.length];
        setPreference(next);
    };

    return (
        <button
            className={`theme-toggle pref-${preference}`}
            onClick={cycle}
            title={preference === 'system' ? 'Following system' : preference === 'dark' ? 'Dark mode' : 'Light mode'}
        >
            <span className="icon sun"/>
            <span className="icon system"/>
            <span className="icon moon"/>
            <span className="thumb"/>
        </button>
    );
};
