import './MswHeader.scss'
import React, {useEffect, useState} from 'react';
import {Button} from 'react-bootstrap';
import {useUserAuth} from '../user/UserAuthContext';
import {MswAddSpot} from "../spot/add/MswAddSpot";
import {MswLoginModal} from "../user/login/MswLoginModal";
import MswSignUpModal from "../user/signup/MswSignUp";
import {MswForgotPassword} from "../user/forgot-password/MswForgotPassword";
import {useAuthModal} from '../user/AuthModalContext';
import {usePwaInstalled} from "../isPwaInstalled/isPwaInstalled";

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
    const [isIOS, setIsIOS] = useState(false);

    let loginOrLogout: JSX.Element;

    if (user) {
        loginOrLogout = <>
            <MswAddSpot/>
            <Button variant='danger' size='sm' onClick={logOut}>Log Out</Button>
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

    useEffect(() => {
        // Detect iOS
        const userAgent = window.navigator.userAgent.toLowerCase();
        const isIosDevice = /iphone|ipad|ipod/.test(userAgent) && !(window as any).MSStream;
        setIsIOS(isIosDevice);
    }, []);

    let iosInstructions = 'Tap Share → Add to Home Screen';
    let androidInstructions = 'Tap ⋮ menu → Install App';
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
                        <p>{isIOS ? iosInstructions : androidInstructions}</p>
                    </div>
                }
            </div>
        </header>
    </>;
}
