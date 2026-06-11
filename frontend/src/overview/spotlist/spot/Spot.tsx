import './Spot.scss'
import React, {useEffect, useState} from 'react';
import {ApiForecast, ApiSample, CountryEnum, ForecastApi, SampleApi, SpotsApi} from '../../../gen/msw-api-ts';
import {MswEditSpot} from "../../../spot/edit/MswEditSpot";
import {MswMeasurement} from './measurement/MswMeasurement';
import {ReactComponent as ArrowDownIcon} from '../../../assets/arrow_down.svg';
import {ReactComponent as DeleteIcon} from '../../../assets/trash.svg';
import {ReactComponent as LinkIcon} from '../../../assets/link.svg';
import {ReactComponent as DragDropIcon} from '../../../assets/drag_drop_icon.svg';
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
import {DateTimeConverter} from "../../../service/DateTimeConverter";

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

    const [forecast, setForecast] = useState<ApiForecast | undefined>(undefined);
    const [forecastLoaded, setForecastLoaded] = useState(false);

    const [lastFewDays, setLastFewDays] = useState<Array<ApiSample> | undefined>(undefined);
    const [lastFewDaysLoaded, setLastFewDaysLoaded] = useState(false);

    const shouldLoadForecast = props.showGraphOfType === GraphTypeEnum.Forecast;

    useEffect(() => {
        if (!shouldLoadForecast) return;
        let cancelled = false;
        setForecastLoaded(false);
        setForecast(undefined);
        (async () => {
            const config = await authConfiguration(token);
            try {
                const res = await new ForecastApi(config).getForecast(props.spot.stationId, props.spot.measurementType);
                if (cancelled) return;
                setForecast(DateTimeConverter.utcForecastToLocalTime(res.data));
            } catch (e: any) {
                if (cancelled) return;
                if (e?.response?.status !== 404) {
                    // no forecast for spot
                    setForecast(undefined);
                }
            } finally {
                if (!cancelled) setForecastLoaded(true);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [shouldLoadForecast, props.spot.stationId, props.spot.measurementType, token]);

    useEffect(() => {
        if (!shouldLoadForecast) return;
        let cancelled = false;
        setLastFewDays(undefined);
        // Logged-out users can't fetch a spot's recent measurements; mark it as
        // "loaded" with no data so the forecast graph just shows the forecast (as
        // before), without panning back into history.
        if (!user) {
            setLastFewDaysLoaded(true);
            return;
        }
        setLastFewDaysLoaded(false);
        (async () => {
            const config = await authConfiguration(token);
            try {
                const res = await new SampleApi(config).getLastFewDaysSamples(props.spot.id);
                if (cancelled) return;
                setLastFewDays(DateTimeConverter.utcLastFewDaysToLocalTime(res.data));
            } finally {
                if (!cancelled) setLastFewDaysLoaded(true);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [shouldLoadForecast, props.spot.id, props.spot.currentSample, token, user]);

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
                        {forecast?.timestamp &&
                            <div className="sample-timestamp">Forecast
                                from: {formatTimestamp(forecast?.timestamp)}</div>}
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

    function getStationLinkBaseUrl(country: CountryEnum) {
        switch (country) {
            case CountryEnum.Ch:
                return "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/";
            case CountryEnum.Fr:
                return "https://www.vigicrues.gouv.fr/station/"
            case CountryEnum.DeBw:
                return "https://www.hvz.baden-wuerttemberg.de/pegel.html?id="
        }
        return assertUnreachable(country);
    }

    // This is a bit of a hack to make the switch exhaustive and remind us to add new enum types here.
    function assertUnreachable(x: never): never {
        throw new Error("Forgot to declare a link to a station in the switch statement.");
    }

    function getSpotSummaryContent(spot: SpotModel) {
        let stationLinkUrl = getStationLinkBaseUrl(spot.stationId.country) + spot.stationId.externalId;

        return <>
            <div className='icons-container'>
                {user &&
                    <Button
                        variant="link"
                        className={'icon drag-drop-icon arrow-icon'}
                        {...props.dragHandleProps}
                        aria-label="Sort the spots on your dashboard"
                    >
                        <DragDropIcon className="svg-icon inverted-bg-icon"/>
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
                    href={stationLinkUrl}
                    target="_blank"
                    rel="noreferrer"
                    aria-label="Link to the station"
                >
                    <LinkIcon className="svg-icon inverted-bg-icon"/>
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
                        <DeleteIcon className="svg-icon"/>
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
                    <ArrowDownIcon className="svg-icon inverted-bg-icon"/>
                </Button>
            </div>
        </>
    }

    function getGraph(spot: SpotModel, isMini: boolean) {
        let forecastContent = <>
            <MswForecastGraph spot={spot} isMini={isMini} forecast={forecast} loaded={forecastLoaded}
                              lastFewDays={lastFewDays} lastFewDaysLoaded={lastFewDaysLoaded}/>
        </>;

        let lastMeasurementsContent = <>
            <MswLastMeasurementsGraph spot={spot} isMini={isMini} lastFewDays={lastFewDays} loaded={lastFewDaysLoaded}/>
        </>;

        let historicalYearsContent = <>
            <MswHistoricalYearsGraph spot={spot} isMini={isMini}/>
        </>;

        if (props.showGraphOfType === GraphTypeEnum.Forecast) {
            if (forecastLoaded) {
                return forecast ? forecastContent : lastMeasurementsContent
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
