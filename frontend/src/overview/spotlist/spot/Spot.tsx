import './Spot.scss'
import React, {useState} from 'react';
import {SpotsApi} from '../../../gen/msw-api-ts';
import {MswEditSpot} from "../../../spot/edit/MswEditSpot";
import {MswMeasurement} from './measurement/MswMeasurement';
import arrow_down_icon from '../../../assets/arrow_down.svg';
import delete_icon from '../../../assets/trash.svg';
import link_icon from '../../../assets/link.svg';
import drag_drop_icon from '../../../assets/drag_drop_icon.svg';
import {authConfiguration} from '../../../api/config/AuthConfiguration';
import {useUserAuth} from '../../../user/UserAuthContext';
import Modal from 'react-bootstrap/Modal';
import {Button} from "react-bootstrap";
import {spotsService} from "../../../service/SpotsService";
import {MswHistoricalYearsGraph} from "./graph/historical/MswHistoricalYearsGraph";
import {MswForecastGraph} from "./graph/forecast/MswForecastGraph";
import {MswLastMeasurementsGraph} from "./graph/historical/MswLastMeasurementsGraph";
import {GraphTypeEnum} from "../../MswOverviewPage";
import {SpotModel} from "../../../model/SpotModel";
import {MswLoader} from "../../../loader/MswLoader";

interface SpotProps {
    spot: SpotModel,
    dragHandleProps: any,
    showGraphOfType: GraphTypeEnum
}

export const Spot = (props: SpotProps) => {
    // @ts-ignore
    const {token, user} = useUserAuth();

    const [showConfirmationModal, setShowConfirmationModal] = useState(false);
    const [isSpotOpen, setIsSpotOpen] = useState(false);

    const handleDeleteSpotAndCloseModal = (spot: SpotModel) => deleteSpot(spot).then(handleCancelConfirmationModal);
    const handleCancelConfirmationModal = () => setShowConfirmationModal(false);
    const handleShowConfirmationModal = () => setShowConfirmationModal(true);

    return <>
        <div key={props.spot.name} className={"spot " + (isSpotOpen ? "open" : "")}>
            <div className="spot-summary">
                {getSpotSummaryContent(props.spot)}
            </div>
            {isSpotOpen &&
                <div className="collapsibleContent">
                    <div className="timestamps">
                        {props.spot.currentSample?.timestamp &&
                            <div className="forecast-timestamp">Sample
                                from: {formatTimestamp(props.spot.currentSample?.timestamp)}</div>}
                        {props.spot.forecast?.timestamp &&
                            <div className="sample-timestamp">Forecast
                                from: {formatTimestamp(props.spot.forecast?.timestamp)}</div>}
                    </div>
                    {getGraph(props.spot, false)}
                </div>
            }
        </div>
    </>;

    function formatTimestamp(timestamp: string): string {
        const formatted = new Intl.DateTimeFormat("de-CH", {
            day: "2-digit",
            month: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
        }).format(new Date(timestamp));

        return formatted.replace(",", "");
    }

    function getSpotSummaryContent(spot: SpotModel) {
        // TODO: other link for other country
        let link = "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/" + spot.stationId.externalId;

        return <>
            <div className='icons-container'>
                {user &&
                    <Button
                        variant="link"
                        className={'icon drag-drop-icon arrow-icon'}
                        {...props.dragHandleProps}
                        aria-label="Sort the spots on your dashboard"
                    >
                        <img alt="" src={drag_drop_icon}/>
                    </Button>
                }
            </div>
            <div className="spotContainer" onClick={() => setIsSpotOpen(!isSpotOpen)}>
                <div className="spot-title">
                    {spot.name}
                </div>
                <MswMeasurement spot={spot}/>
                {!isSpotOpen &&
                    <div className={"miniGraph"} style={{width: '100%', height: '100%'}}>
                        {getGraph(props.spot, true)}
                    </div>
                }
            </div>
            <div className="icons-container">
                <Button
                    variant="link"
                    className="icon"
                    href={link}
                    target="_blank"
                    rel="noreferrer"
                    aria-label="Link to the BAFU station"
                >
                    <img src={link_icon} alt="" title="Link to the BAFU station"/>
                </Button>
                {user &&
                    <MswEditSpot spot={spot}/>
                }
                {user &&
                    <Button
                        variant="link"
                        className="icon"
                        onClick={() => handleShowConfirmationModal()}
                        aria-label="Delete this spot from your dashboard"
                    >
                        <img className="button" alt="" title="Delete this spot from your dashboard."
                             src={delete_icon}/>
                    </Button>
                }
                <Modal show={showConfirmationModal} onHide={handleCancelConfirmationModal}>
                    <Modal.Header closeButton>
                        <Modal.Title>Are you sure?</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>You won't be able to retrieve this spot. If you need it again you will have to add a new
                        one.</Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-dark" onClick={handleCancelConfirmationModal}>
                            Cancel
                        </Button>
                        <Button variant="danger" onClick={() => handleDeleteSpotAndCloseModal(spot)}>
                            Delete Spot
                        </Button>
                    </Modal.Footer>
                </Modal>
                <Button
                    variant="link"
                    className="collapsible-icon icon arrow-icon"
                    onClick={() => setIsSpotOpen(!isSpotOpen)}
                    aria-label="Toggle forecast details"
                >
                    <img alt="Toggle forecast details" src={arrow_down_icon}/>
                </Button>
            </div>
        </>
    }

    function getGraph(spot: SpotModel, isMini: boolean) {
        let forecastContent = <>
            <MswForecastGraph spot={spot} isMini={isMini}/>
        </>;

        let lastMeasurementsContent = <>
            <MswLastMeasurementsGraph spot={spot} isMini={isMini}/>
        </>;

        let historicalYearsContent = <>
            <MswHistoricalYearsGraph spot={spot} isMini={isMini}/>
        </>;

        if (props.showGraphOfType === GraphTypeEnum.Forecast) {
            if (spot.forecastLoaded) {
                return spot.forecast ? forecastContent : lastMeasurementsContent
            } else {
                return <><MswLoader/></>;
            }
        } else {
            return <>{historicalYearsContent}</>;
        }
    }

    async function deleteSpot(spot: SpotModel) {
        let config = await authConfiguration(token);
        new SpotsApi(config).deletePrivateSpot(spot.id!); // no await to not be blocking
        spotsService.deleteSpot(spot.id!);
    }

}
