import React, {useEffect, useMemo} from "react";
import {ApiMeasurementType, ApiSpotSpotTypeEnum, ApiStation, ApiStationId, CountryEnum} from "../gen/msw-api-ts";
import {Button, Col, Form, Row} from "react-bootstrap";
import {Typeahead} from "react-bootstrap-typeahead";
import Modal from "react-bootstrap/Modal";
import {MswStationMap} from "../overview/map/station-map/MswStationMap";
import './MswAddOrEditUtil.scss';
import {measurementLabel} from "../helper/ApiMeasurementTypeHelper";

export function MswAddOrEditSpotModal(showModal: boolean | undefined, handleCancelModal: (() => void) | undefined, formRef: React.MutableRefObject<HTMLFormElement | null>, handleSaveAndCloseModal: (e: {
    preventDefault: any
}) => void, spotName: string, setSpotName: (value: (((prevState: string) => string) | string)) => void, type: ApiSpotSpotTypeEnum, setType: (value: (((prevState: ApiSpotSpotTypeEnum) => ApiSpotSpotTypeEnum) | ApiSpotSpotTypeEnum)) => void, setStationId: (value: (((prevState: (ApiStationId | undefined)) => (ApiStationId | undefined)) | ApiStationId | undefined)) => void, setStationSelectionError: (value: (((prevState: string) => string) | string)) => void, stations: ApiStation[], stationId: ApiStationId | undefined, stationSelectionError: string, measurementType: ApiMeasurementType, setMeasurementType: (value: (((prevState: ApiMeasurementType) => ApiMeasurementType) | ApiMeasurementType)) => void, minValue: number | undefined, setMinValue: (value: (((prevState: (number | undefined)) => (number | undefined)) | number | undefined)) => void, maxValue: number | undefined, setMaxValue: (value: (((prevState: (number | undefined)) => (number | undefined)) | number | undefined)) => void, withNotification: boolean, setWithNotification: (value: (((prevState: boolean) => boolean) | boolean)) => void, isSubmitButtonDisabled: boolean | undefined, setIsSubmitButtonDisabled: (value: (((prevState: boolean) => boolean) | boolean)) => void, isEditMode: boolean) {
    const selectedStation = useMemo(
        () => stations.find(s => stationId && s.id.country === stationId.country && s.id.externalId === stationId.externalId),
        [stations, stationId]
    );
    const supportedMeasurements = useMemo(() => {
        const types = (selectedStation?.supportedMeasurements ?? []).filter(
            t => t === ApiMeasurementType.Flow || t === ApiMeasurementType.Height
        );
        return types.length > 0 ? types : [ApiMeasurementType.Flow];
    }, [selectedStation]);

    // If the selected measurement type isn't supported by the chosen station, snap to a supported one.
    useEffect(() => {
        if (!supportedMeasurements.includes(measurementType)) {
            setMeasurementType(measurementType ?? supportedMeasurements[0]);
        }
    }, [supportedMeasurements, measurementType, setMeasurementType]);

    // Validation effect for enabling/disabling submit button
    useEffect(() => {
        const flowsValid =
            minValue !== undefined && minValue >= 0 &&
            maxValue !== undefined && maxValue >= 0 &&
            maxValue > minValue;
        const nameValid = spotName.trim() !== "";
        const stationValid = stations.some(station => station.id.country === stationId?.country && station.id.externalId === stationId.externalId);

        if (flowsValid && nameValid && stationValid) {
            setIsSubmitButtonDisabled(false);
        } else {
            setIsSubmitButtonDisabled(true);
        }
    }, [minValue, maxValue, spotName, stationId, stations]);

    const unitLabel = measurementLabel(measurementType);

    const countryEmoji = (country: CountryEnum) => {
        switch (country) {
            case CountryEnum.Ch:
                return '🇨🇭';
            case CountryEnum.Fr:
                return '🇫🇷';
            case CountryEnum.DeBw:
                return '🇩🇪';
            default:
                return '🌍';
        }
    };

    const sortedStations = useMemo(() => {
        return [...stations].sort((a, b) => a.label.localeCompare(b.label));
    }, [stations]);

    function setActiveStation(stationId: ApiStationId | undefined, stationSelectionError: string, isSubmitButtonDisabled: boolean) {
        setStationId(stationId);
        setStationSelectionError(stationSelectionError);
        setIsSubmitButtonDisabled(isSubmitButtonDisabled);
    }

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
                                            setActiveStation(station.id, '', false);

                                        } else {
                                            setActiveStation(undefined, 'Please select a valid option.', true);
                                        }
                                    }}
                                    onBlur={() => {
                                        let matchingStation = undefined;
                                        if (stationId) {
                                            matchingStation = stations.find(s => s.id.country === stationId.country && s.id.externalId === stationId.externalId);
                                        }
                                        if (!matchingStation) {
                                            setActiveStation(undefined, 'Please select a valid option.', true);
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

                            {supportedMeasurements.length > 1 && (
                                <>
                                    <Form.Label htmlFor="formBasicMeasurementType">Measurement</Form.Label>
                                    <Form.Group className="mb-3" controlId="formBasicMeasurementType">
                                        {supportedMeasurements.map(m => (
                                            <Form.Check
                                                key={m}
                                                inline
                                                type="radio"
                                                label={measurementLabel(m)}
                                                name="radioMeasurementGroup"
                                                id={`measurement-${m}`}
                                                checked={measurementType === m}
                                                onChange={() => setMeasurementType(m)}
                                            />
                                        ))}
                                    </Form.Group>
                                </>
                            )}

                            <Form.Label htmlFor="formBasicMinValue">Minimum {unitLabel} for Spot to Work</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicMinValue">
                                <Form.Control
                                    required
                                    type="number"
                                    placeholder={`Minimum ${unitLabel}`}
                                    value={minValue}
                                    onChange={(e) => setMinValue(isNaN(parseFloat(e.target.value)) ? undefined : parseFloat(e.target.value))}
                                />
                            </Form.Group>
                            {minValue !== undefined && minValue < 0 && (
                                <div style={{color: 'red'}}>
                                    Minimum {unitLabel.toLowerCase()} must be a positive number.
                                </div>
                            )}

                            <Form.Label htmlFor="formBasicMaxValue">Maximum {unitLabel} for Spot to Work</Form.Label>
                            <Form.Group className="mb-3" controlId="formBasicMaxValue">
                                <Form.Control
                                    required
                                    type="number"
                                    placeholder={`Maximum ${unitLabel}`}
                                    value={maxValue}
                                    onChange={(e) => setMaxValue(isNaN(parseFloat(e.target.value)) ? undefined : parseFloat(e.target.value))}
                                />
                            </Form.Group>
                            {maxValue !== undefined && maxValue < 0 && (
                                <div style={{color: 'red'}}>
                                    Maximum {unitLabel.toLowerCase()} must be a positive number.
                                </div>
                            )}
                            {maxValue !== undefined && minValue !== undefined && maxValue <= minValue && (
                                <div style={{color: 'red'}}>
                                    Maximum {unitLabel.toLowerCase()} must be greater than
                                    minimum {unitLabel.toLowerCase()}.
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
                        <MswStationMap
                            stations={stations}
                            onStationSelect={(station) => {
                                setActiveStation(station.id, '', false);
                            }}
                        ></MswStationMap>
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
