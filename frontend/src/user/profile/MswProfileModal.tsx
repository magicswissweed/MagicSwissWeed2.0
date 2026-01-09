import React, {useEffect, useState} from "react";
import {Button} from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import {useUserAuth} from "../UserAuthContext";

interface MswProfileModalProps {
    isOpen: boolean,
    closeModal: () => void
}

export const MswProfileModal = (props: MswProfileModalProps) => {

    // @ts-ignore
    const {user, logOut} = useUserAuth();

    const [showProfileModal, setShowProfileModal] = useState(props.isOpen);

    useEffect(() => {
        setShowProfileModal(props.isOpen);
    }, [props.isOpen]);

    return (
        <>
            <Modal show={showProfileModal} onHide={props.closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>User Profile</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                  <div className='text-center'>
                    {user?.email && (
                        <div style={{ marginBottom: '16px' }}>
                            <p style={{ margin: '0 0 8px 0', color: '#666' }}>Email</p>
                            <p style={{ margin: '0', fontWeight: 'bold' }}>{user.email}</p>
                        </div>
                    )}
                    <Button variant='danger' onClick={logOut}>Log Out</Button>
                  </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-dark" onClick={props.closeModal}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};
