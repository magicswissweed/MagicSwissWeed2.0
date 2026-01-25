import './MswHeader.scss'
import React, {useEffect, useState} from 'react';
import {Button} from 'react-bootstrap';
import {useUserAuth} from '../user/UserAuthContext';
import {MswAddSpot} from "../spot/add/MswAddSpot";
import {MswLoginModal} from "../user/login/MswLoginModal";
import MswSignUpModal from "../user/signup/MswSignUp";
import {MswForgotPassword} from "../user/forgot-password/MswForgotPassword";
import {MswProfileModal} from "../user/profile/MswProfileModal";
import {useAuthModal} from '../user/AuthModalContext';
import '@khmyznikov/pwa-install';
import {usePwaInstalled} from "../isPwaInstalled/isPwaInstalled";
import {subscribeToPushNotifications} from "../subscribeToPushNotifications";

// Declare the custom element for TypeScript
declare global {
    namespace React.JSX {
        interface IntrinsicElements {
            'pwa-install': any;
        }
    }
}

export const MswHeader = () => {
    // @ts-ignore
    const {user, token, logOut} = useUserAuth();
    const {
        showLoginModal,
        showSignupModal,
        showForgotPasswordModal,
        setShowLoginModal,
        setShowSignupModal,
        setShowForgotPasswordModal
    } = useAuthModal();
    const isPwaInstalled = usePwaInstalled();
    const [shouldShowPwaInstallBrowserPopup, setShouldShowPwaInstallBrowserPopup] = useState<boolean>(false);

    let loginOrLogout: React.JSX.Element;

    useEffect(() => {
        if (token && isPwaInstalled) {
            subscribeToPushNotifications(token) // no .then, because we don't want to be blocking
        }
    }, [token])

    if (user) {
        loginOrLogout = <>
            <MswAddSpot/>
            {/* <Button variant='danger' size='sm' onClick={logOut}>Log Out</Button> */}

            {/* <div className='icon' onClick={() => setShowProfileModal(true)}>
              <img className="button" alt="Show user information." title="Show user information." src={user_icon}/>
            </div> */}
            <MswProfileModal/>
        </>
    } else {
        loginOrLogout = <>
            <Button variant='msw me-2' size='sm' onClick={() => setShowLoginModal(true)}>Log In</Button>
            <Button variant='msw-outline' size='sm' onClick={() => setShowSignupModal(true)}>Sign Up</Button>

            <MswLoginModal isOpen={showLoginModal}
                           closeModal={() => setShowLoginModal(false)}
                           openSignupModal={() => setShowSignupModal(true)}
                           openForgotPasswordModal={() => setShowForgotPasswordModal(true)}/>
            <MswSignUpModal isOpen={showSignupModal}
                            closeModal={() => setShowSignupModal(false)}
                            openLoginModal={() => setShowLoginModal(true)}/>
            <MswForgotPassword isOpen={showForgotPasswordModal}
                               closeModal={() => setShowForgotPasswordModal(false)}
                               openLoginModal={() => setShowLoginModal(true)}/>
        </>
    }

    return <>
        <header className="App-header">
            <div className="loginOrLogoutContainer m-2">
                {loginOrLogout}
            </div>
            <div className="title">
                <h1>MagicSwissWeed</h1>
                <p>Know when the rivers flow</p>
                {!isPwaInstalled && token &&
                    <div className='info-box'>
                        <p>
                            ! Install this site as an app to get notifications on your mobile device !
                        </p>
                        <div className='install-pwa-button'
                             onClick={() => setShouldShowPwaInstallBrowserPopup(true)}>
                            Install
                        </div>
                    </div>
                }
                {shouldShowPwaInstallBrowserPopup && (
                    <div>
                        {/* PWA Install Element - automatically shows install button when available */}
                        <pwa-install
                            manifestpath="/manifest.json"
                            name="MagicSwissWeed"
                            description="Know when the rivers flow."
                            install-description="Install as an app to enable notifications."
                            icon="/logo512.png"
                        />
                    </div>
                )}
            </div>
        </header>
    </>;
}
