import React, {useEffect, useMemo} from "react";
import {ApiSpotSpotTypeEnum, ApiStation, ApiStationId, CountryEnum} from "../gen/msw-api-ts";
import {Button, Col, Form, Row} from "react-bootstrap";
import {Typeahead} from "react-bootstrap-typeahead";
import Modal from "react-bootstrap/Modal";
import {MswStationMap} from "../overview/map/station-map/MswStationMap";
import './MswAddOrEditUtil.scss';

export function MswAddOrEditSpotModal(showModal: boolean | undefined, handleCancelModal: (() => void) | undefined, formRef: React.MutableRefObject<HTMLFormElement | null>, handleSaveAndCloseModal: (e: {
    preventDefault: any
}) => void, spotName: string, setSpotName: (value: (((prevState: string) => string) | string)) => void, type: ApiSpotSpotTypeEnum, setType: (value: (((prevState: ApiSpotSpotTypeEnum) => ApiSpotSpotTypeEnum) | ApiSpotSpotTypeEnum)) => void, setStationId: (value: (((prevState: (ApiStationId | undefined)) => (ApiStationId | undefined)) | ApiStationId | undefined)) => void, setStationSelectionError: (value: (((prevState: string) => string) | string)) => void, stations: ApiStation[], stationId: ApiStationId | undefined, stationSelectionError: string, minFlow: number | undefined, setMinFlow: (value: (((prevState: (number | undefined)) => (number | undefined)) | number | undefined)) => void, maxFlow: number | undefined, setMaxFlow: (value: (((prevState: (number | undefined)) => (number | undefined)) | number | undefined)) => void, withNotification: boolean, setWithNotification: (value: (((prevState: boolean) => boolean) | boolean)) => void, isSubmitButtonDisabled: boolean | undefined, setIsSubmitButtonDisabled: (value: (((prevState: boolean) => boolean) | boolean)) => void, isEditMode: boolean) {
    // Validation effect for enabling/disabling submit button
    useEffect(() => {
        const flowsValid =
            minFlow !== undefined && minFlow >= 0 &&
            maxFlow !== undefined && maxFlow >= 0 &&
            maxFlow > minFlow;
        const nameValid = spotName.trim() !== "";
        const stationValid = stations.some(station => station.id.country === stationId?.country && station.id.externalId === stationId.externalId);

        if (flowsValid && nameValid && stationValid) {
            setIsSubmitButtonDisabled(false);
        } else {
            setIsSubmitButtonDisabled(true);
        }
    }, [minFlow, maxFlow, spotName, stationId, stations]);

    const countryEmoji = (country: CountryEnum) => {
        switch (country) {
            case CountryEnum.Ch:
                return '🇨🇭';
            case CountryEnum.Fr:
                return '🇫🇷';
            default:
                return '🌍';
        }
    };

    const sortedStations = useMemo(() => {
        return [...stations].sort((a, b) => a.label.localeCompare(b.label));
    }, [stations]);

    return <>
        <Modal dialogClassName="add-or-edit-modal" show={showModal} onHide={handleCancelModal} scrollable={true}>
            <Modal.Header closeButton>

                <Modal.Title>{isEditMode ? "Edit private Spot" : "Add new private Spot"}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <div className="form modal-body-container">
                    <div className="container-left">
                        <p className="info">This spot will only be visible to you. Other surfers will have to find this
                            spot
                            themselves.</p>
                        <Form ref={formRef} onSubmit={handleSaveAndCloseModal}>
                            <Form.Label htmlFor="formBasicSpotName">Name of the Spot</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicSpotName">
                                <Form.Control
                                    required
                                    type="string"
                                    placeholder="Name of the Spot"
                                    value={spotName}
                                    onChange={(e) => setSpotName(e.target.value)}
                                />
                            </Form.Group>

                            <Form.Label htmlFor="formBasicSpotType">Type of the Spot</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicSpotType">
                                <Form>
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="Riversurf"
                                        name="radioTypeGroup"
                                        id="riversurf"
                                        checked={type === ApiSpotSpotTypeEnum.RiverSurf}
                                        onChange={() => setType(ApiSpotSpotTypeEnum.RiverSurf)}
                                    />
                                    <Form.Check
                                        inline
                                        type="radio"
                                        label="Bungeesurf"
                                        name="radioTypeGroup"
                                        id="bungeesurf"
                                        checked={type === ApiSpotSpotTypeEnum.BungeeSurf}
                                        onChange={() => setType(ApiSpotSpotTypeEnum.BungeeSurf)}
                                    />
                                </Form>
                            </Form.Group>

                            <Form.Label htmlFor="formBasicStationId">The measuring station</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicStationId">
                                <Typeahead
                                    allowNew={false}
                                    inputProps={{required: true}}
                                    id="station-autocomplete"
                                    labelKey="label"
                                    onChange={(selected) => {
                                        // distinguish between a valid selection and no selection
                                        if (selected && selected.length > 0) {
                                            const station = selected[0] as ApiStation; // Safely access the first selected item
                                            setStationId(station.id); // Update the stationId state
                                            setStationSelectionError(''); // Clear any selection error
                                            setIsSubmitButtonDisabled(false);

                                        } else {
                                            setStationId(undefined); // Clear the stationId if no selection
                                            setStationSelectionError('Please select a valid option.'); // set an error
                                            setIsSubmitButtonDisabled(true);
                                        }
                                    }}
                                    onBlur={() => {
                                        let matchingStation = undefined;
                                        if (stationId) {
                                            matchingStation = stations.find(s => s.id.country === stationId.country && s.id.externalId === stationId.externalId);
                                        }
                                        if (!matchingStation) {
                                            setStationId(undefined);
                                            setStationSelectionError("Please select a valid option.");
                                            setIsSubmitButtonDisabled(true);
                                        }
                                    }}
                                    renderMenuItemChildren={(option: unknown) => {
                                        const station = option as ApiStation;
                                        return (
                                            <div>
                                                <span style={{marginRight: 8}}>
                                                    {countryEmoji(station.id.country)}
                                                </span>
                                                <span>{station.label}</span>
                                            </div>
                                        );
                                    }}
                                    options={sortedStations}
                                    placeholder="Station"
                                    selected={stations.filter(s => {
                                        if (stationId === undefined) {
                                            return false;
                                        }
                                        return s.id.country === stationId.country && s.id.externalId === stationId.externalId;
                                    })}
                                />
                                {stationSelectionError && <div style={{color: 'red'}}>{stationSelectionError}</div>}
                            </Form.Group>

                            <Form.Label htmlFor="formBasicMinFlow">Minimum Flow for Spot to Work</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicMinFlow">
                                <Form.Control
                                    required
                                    type="number"
                                    placeholder="Minimum Flow"
                                    value={minFlow}
                                    onChange={(e) => setMinFlow(isNaN(parseFloat(e.target.value)) ? undefined : parseFloat(e.target.value))}
                                />
                            </Form.Group>
                            {minFlow !== undefined && minFlow < 0 && (
                                <div style={{color: 'red'}}>
                                    Minimum flow must be a positive number.
                                </div>
                            )}

                            <Form.Label htmlFor="formBasicMaxFlow">Maximum Flow for Spot to Work</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicMaxFlow">
                                <Form.Control
                                    required
                                    type="number"
                                    placeholder="Maximum Flow"
                                    value={maxFlow}
                                    onChange={(e) => setMaxFlow(isNaN(parseFloat(e.target.value)) ? undefined : parseFloat(e.target.value))}
                                />
                            </Form.Group>
                            {maxFlow !== undefined && maxFlow < 0 && (
                                <div style={{color: 'red'}}>
                                    Maximum flow must be a positive number.
                                </div>
                            )}
                            {maxFlow !== undefined && minFlow !== undefined && maxFlow <= minFlow && (
                                <div style={{color: 'red'}}>
                                    Maximum flow must be greater than minimum flow.
                                </div>
                            )}
                        </Form>

                        <Form>
                            <Row className="align-items-center">
                                <Col className="text-start">Enable Notifications</Col>
                                <Col xs="auto">
                                    <Form.Check
                                        type="switch"
                                        id="enable-notification-toggle"
                                        checked={withNotification}
                                        onChange={() => {
                                            setWithNotification(!withNotification)
                                        }}
                                    />
                                </Col>
                            </Row>
                        </Form>
                    </div>
                    <div className="container-right">
                        <MswStationMap stations={stations}></MswStationMap>
                    </div>
                </div>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="outline-dark" onClick={handleCancelModal}>
                    Cancel
                </Button>
                <Button disabled={isSubmitButtonDisabled} variant="msw" type="submit"
                        onClick={() => formRef.current && formRef.current.requestSubmit()}>
                    {isEditMode ? "Save Changes" : "Add Spot"}
                </Button>
            </Modal.Footer>
        </Modal>
    </>;
}
