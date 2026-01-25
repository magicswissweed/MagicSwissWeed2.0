import React, {useState} from "react";
import {Button} from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import {useUserAuth} from "../UserAuthContext";
import user_icon from '../../assets/user_icon.svg';

export const MswProfileModal = () => {

    const [showProfileModal, setShowProfileModal] = useState(false);
    const handleShowProfileModal = () => setShowProfileModal(true);
    const handleCloseProfileModal = () => setShowProfileModal(false);

    // @ts-ignore
    const {user, logOut} = useUserAuth();

    return (
        <>
            <Button
                variant="link"
                className='icon'
                onClick={() => handleShowProfileModal()}
                aria-label="Show user information"
            >
                <img className="button" alt="" title="Show user information." src={user_icon}/>
            </Button>
            <Modal show={showProfileModal} onHide={handleCloseProfileModal}>
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
                    <Button variant="outline-dark" onClick={handleCloseProfileModal}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};
