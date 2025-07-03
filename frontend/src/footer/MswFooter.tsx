import React, {useState} from 'react';
import './MswFooter.scss';
import Modal from "react-bootstrap/Modal";
import Button from "react-bootstrap/Button";
import { BuyMeACoffeeButton } from './BuyMeACoffeeButton';

export const MswFooter = () => {
    const [showAboutModal, setShowAboutModal] = useState(false);

    return (
        <>
            <footer>
                <div className="Footer">
                    <ul className='Footer_list'>
                        <li>Source:{" "}
                            <a href="https://www.hydrodaten.admin.ch">
                                BAFU
                            </a>
                        </li>
                        <li>Created by Nicola K&#252;ng and Aaron Studer</li>
                        <li>
                            <Button variant="link" onClick={() => setShowAboutModal(true)}>
                                About
                            </Button>
                        </li>
                    </ul>
                </div>
            </footer>

            <Modal show={showAboutModal} onHide={() => setShowAboutModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>About</Modal.Title>
                </Modal.Header>
                <Modal.Body style={{textAlign: "center"}}>
                    <p>
                        This website is a passion project by Aaron Studer and Nicola Küng.
                    </p>
                    <p>
                        Nicola built a <a href={"https://nkueng.github.io/MagicSwissWeed1.0/"}>first version</a> of
                        MagicSwissWeed to learn JavaScript and bring public data to life.
                    </p>
                    <p>
                        When he met Aaron over a river surf session, they dreamed up grand ideas for an improved second
                        version.
                    </p>
                    <p>
                        Thanks to Aaron's programming skills, these dreams actually took shape in what you have before
                        you.
                    </p>
                    <p>
                        If you find this site useful, we would appreciate your support and feedback.
                    </p>

                </Modal.Body>
                <Modal.Footer style={{justifyContent: "center"}}>
                    <div style={{display: "flex", gap: "1rem"}}>
                        <BuyMeACoffeeButton />
                        <Button variant="msw"
                                style={{width: "135px"}}
                                onClick={() => window.location.href = "https://github.com/magicswissweed/msw/issues"}>
                            Feedback
                        </Button>
                    </div>
                </Modal.Footer>
            </Modal>
        </>
    );
};
