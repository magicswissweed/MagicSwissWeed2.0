import './MswSignUp.scss';
import React, {useEffect, useRef, useState} from "react";
import {Button, Form} from "react-bootstrap";
import {GoogleSignInButton} from "@firebase-oss/ui-react";
import {useUserAuth} from "../UserAuthContext";
import Modal from "react-bootstrap/Modal";

interface MswSignUpProps {
    isOpen: boolean,
    closeModal: () => void,
    openLoginModal: () => void
}

const MswSignup = (props: MswSignUpProps) => {
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [password, setPassword] = useState("");
    // @ts-ignore
    const {signUp} = useUserAuth();

    const [showSignupModal, setShowSignupModal] = useState(props.isOpen);

    useEffect(() => {
        setShowSignupModal(props.isOpen);
    }, [props.isOpen]);

    const formRef = useRef<HTMLFormElement | null>(null);

    const handleSubmit = async (e: { preventDefault: any; }) => {
        e.preventDefault()
        setError("");
        try {
            await signUp(email, password);
            props.closeModal();
        } catch (err: any) {
            if (err.message.includes('auth/email-already-in-use')) {
                setError('<p>This email is already registered. Please try to log in.</p>');
            } else if (err.message.includes('auth/weak-password')) {
                setError('Password should have at least 6 Characters.')
            } else {
                setError(err.message);
            }
        }
    };

    function onOpenLoginModal() {
        props.closeModal();
        props.openLoginModal();
    }

    return (
        <>
            <Modal show={showSignupModal} onHide={props.closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Sign Up</Modal.Title>
                </Modal.Header>
                <Modal.Body className="auth-modal">
                    <div className="form">
                        {error && <p className="error-message" dangerouslySetInnerHTML={{__html: error}}></p>}

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
                        </Form>
                    </div>

                    <div className="auth-separator mt-2 mb-3">
                        <span>or</span>
                    </div>
                    <div className="d-flex justify-content-center mb-2 google-signin-button-container">
                        <GoogleSignInButton themed onSignIn={() => props.closeModal()}/>
                    </div>

                    <div className="p-4 box mt-3 text-center">
                        Already have an account?
                        <Button variant="link" onClick={onOpenLoginModal}>Log In</Button>
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-dark" onClick={props.closeModal}>
                        Cancel
                    </Button>
                    <Button variant="msw"
                            onClick={() => formRef.current && formRef.current.requestSubmit()}>
                        Sign Up
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default MswSignup;