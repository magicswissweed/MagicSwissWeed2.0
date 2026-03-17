import './MswForgotPassword.scss';
import React, {useEffect, useRef, useState} from "react";
import {Button, Form} from "react-bootstrap";
import {useUserAuth} from "../UserAuthContext";
import Modal from "react-bootstrap/Modal";

interface MswForgotPasswordProps {
    isOpen: boolean,
    closeModal: () => void,
    openLoginModal: () => void
}

export function MswForgotPassword(props: MswForgotPasswordProps) {
    const [email, setEmail] = useState("");
    const [showSuccessMsg, setShowSuccessMsg] = useState(false);

    // @ts-ignore
    const {sendForgotPasswordEmail} = useUserAuth();

    const [showForgotPasswordModal, setShowForgotPasswordModal] = useState(props.isOpen);

    useEffect(() => {
        setShowForgotPasswordModal(props.isOpen);
    }, [props.isOpen]);

    const formRef = useRef<HTMLFormElement | null>(null);

    const handleSubmit = async (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        sendForgotPasswordEmail(email);
        setShowSuccessMsg(true);
    };

    function onOpenLoginModal() {
        props.closeModal();
        props.openLoginModal();
    }

    return <>
        <Modal show={showForgotPasswordModal} onHide={props.closeModal}>
            <Modal.Header closeButton>
                <Modal.Title>Reset Password</Modal.Title>
            </Modal.Header>
            <Modal.Body className="auth-modal">
                <div className="form">
                    <Form ref={formRef} onSubmit={handleSubmit}>

                        <Form.Group className="mb-3" controlId="formBasicEmail">
                            <Form.Control
                                type="email"
                                placeholder="Email address"
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </Form.Group>
                    </Form>

                    {showSuccessMsg &&
                        <p className="success-msg text-center">We sent you an email. Please check your inbox.</p>}

                    <div className='text-center'>
                        <Button variant="link" onClick={onOpenLoginModal}>Back to Login</Button>
                    </div>
                </div>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="outline-dark" onClick={props.closeModal}>
                    Cancel
                </Button>
                <Button variant="msw"
                        onClick={() => formRef.current && formRef.current.requestSubmit()}>
                    Reset Password
                </Button>
            </Modal.Footer>
        </Modal>
    </>;
}