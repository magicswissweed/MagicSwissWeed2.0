import './MswHeader.scss'
import React, {useState} from 'react';
import {Button} from 'react-bootstrap';
import {useUserAuth} from '../user/UserAuthContext';
import {MswAddSpot} from "../spot/add/MswAddSpot";
import {MswLoginModal} from "../user/login/MswLoginModal";
import MswSignUpModal from "../user/signup/MswSignUp";
import {MswForgotPassword} from "../user/forgot-password/MswForgotPassword";

export const MswHeader = () => {
    // @ts-ignore
    const {user, logOut} = useUserAuth();

    const [showLoginModal, setShowLoginModal] = useState(false);
    const [showSignupModal, setShowSignupModal] = useState(false);
    const [showForgotPasswordModal, setShowForgotPasswordModal] = useState(false);

    let loginOrLogout: JSX.Element;

    if (user) {
        loginOrLogout = <>
            <MswAddSpot/>
            <Button variant='msw-outline' size='sm' onClick={logOut}>Logout</Button>
        </>
    } else {
        loginOrLogout = <>
            <Button variant='msw-outline me-2' size='sm' onClick={() => setShowLoginModal(true)}>Login</Button>
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
            </div>
        </header>
    </>;
}
