import './MswThemeSwitch.scss'
import {useTheme} from "../../theme/MswThemeContext";

export const MswThemeSwitch = () => {
    const {theme, setTheme} = useTheme();

    const toggle = () => {
        setTheme(theme === 'dark' ? 'light' : 'dark');
    };

    return (
        <button className={`theme-toggle ${theme}`} onClick={toggle}>
            <span className="icon sun"/>
            <span className="icon moon"/>
            <span className="thumb"/>
        </button>
    );
};
