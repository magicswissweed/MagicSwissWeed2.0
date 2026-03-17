import './MswLogin.scss';
import React, {useEffect, useRef, useState} from "react";
import {Alert, Button, Form} from "react-bootstrap";
import {GoogleSignInButton} from "@firebase-oss/ui-react";
import Modal from "react-bootstrap/Modal";
import {useUserAuth} from "../UserAuthContext";

interface MswLoginModalProps {
    isOpen: boolean,
    closeModal: () => void,
    openSignupModal: () => void,
    openForgotPasswordModal: () => void
}

export const MswLoginModal = (props: MswLoginModalProps) => {
    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [error, setError] = useState<string>("");
    // @ts-ignore
    const {logIn} = useUserAuth();

    const [showLoginModal, setShowLoginModal] = useState(props.isOpen);

    useEffect(() => {
        setShowLoginModal(props.isOpen);
    }, [props.isOpen]);

    const formRef = useRef<HTMLFormElement | null>(null);

    async function handleSubmit(e: { preventDefault: any; }) {
        e.preventDefault();
        setError("");
        try {
            await logIn(email, password);
            props.closeModal();
        } catch (err: any) {
            if (err.message.includes('auth/invalid-credential')) {
                setError('Wrong email or password.');
            } else if (err.message.includes('auth/invalid-email')) {
                setError('Wrong email.');
            } else if (err.message.includes('auth/missing-passwordl')) {
                setError('Please provide a password.');
            } else {
                setError(err.message);
            }
        }
    }

    function onOpenSignupModal() {
        props.closeModal();
        props.openSignupModal();
    }

    function onOpenForgotPasswordModal() {
        props.closeModal();
        props.openForgotPasswordModal();
    }

    // const handleGoogleSignIn = async (e) => {
    //     e.preventDefault();
    //     try {
    //         await googleSignIn();
    //         navigate("/spots");
    //     } catch (error) {
    //         console.log(error.message);
    //     }
    // };

    return (
        <>
            <Modal show={showLoginModal} onHide={props.closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Log In</Modal.Title>
                </Modal.Header>
                <Modal.Body className="auth-modal">
                    <div className="form">
                        {error && <Alert variant="danger">{error}</Alert>}

                        <Form ref={formRef} onSubmit={handleSubmit}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Control
                                    type="email"
                                    placeholder="Email address"
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Control
                                    type="password"
                                    placeholder="Password"
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </Form.Group>
                            <div className='text-center'>
                                <Button variant="link" onClick={onOpenForgotPasswordModal}>Forgot Password</Button>
                            </div>
                        </Form>
                    </div>

                    <div className="auth-separator mt-2 mb-3">
                        <span>or</span>
                    </div>

                    <div className="d-flex flex-column align-items-center gap-2 mb-2 google-signin-button-container">
                        <GoogleSignInButton themed onSignIn={() => props.closeModal()}/>
                    </div>

                    <div className="p-4 box mt-3 text-center">
                        Don't have an account?
                        <Button variant="link" onClick={onOpenSignupModal}>Sign Up</Button>
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-dark" onClick={props.closeModal}>
                        Cancel
                    </Button>
                    <Button variant="msw"
                            onClick={() => formRef.current && formRef.current.requestSubmit()}>
                        Log In
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};
