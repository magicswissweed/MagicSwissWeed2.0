import "./MswOverviewPage.scss";
import React, {useEffect, useMemo, useState} from "react";
import {MswHeader} from '../header/MswHeader';
import {MswFooter} from '../footer/MswFooter';
import {SpotList} from './spotlist/SpotList'
import {MswLoader} from '../loader/MswLoader';
import {useUserAuth} from '../user/UserAuthContext';
import {spotsService} from "../service/SpotsService";
import {Col, Form, Row} from "react-bootstrap";
import {MswSpotMap} from "./map/spot-map/MswSpotMap";
import {FlowColorEnum, SpotModel} from "../model/SpotModel";
import {stationsService} from "../service/StationsService";
import {FlowStatusFilterChips} from "./filter/FlowStatusFilterChips";
import {MswAddSpot} from "../spot/add/MswAddSpot";
import PullToRefresh from 'pulltorefreshjs';
import {isIosPwa} from "../utils/deviceDetection";

const pullToRefreshStyles = `
.__PREFIX__ptr {
    box-shadow: inset 0 -3px 5px rgba(0, 0, 0, 0.12);
    pointer-events: none;
    font-size: 0.85em;
    font-weight: bold;
    top: 0;
    height: 0;
    transition: height 0.3s, min-height 0.3s;
    text-align: center;
    width: 100%;
    overflow: hidden;
    display: flex;
    align-items: flex-end;
    align-content: stretch;
}

.__PREFIX__box {
    padding: 10px;
    flex-basis: 100%;
    background-color: var(--ptr-bg);
}

.__PREFIX__pull {
    transition: none;
}

.__PREFIX__text {
    margin-top: .33em;
    color: var(--ptr-text-color);
}

.__PREFIX__icon {
    color: var(--ptr-icon-color);
    transition: transform .3s;
}

.__PREFIX__top {
    touch-action: pan-x pan-down pinch-zoom;
}

.__PREFIX__release .__PREFIX__icon {
    transform: rotate(180deg);
}
`;

function isNotEmpty(array: Array<any> | undefined) {
    return array && array.length > 0;
}

export enum GraphTypeEnum {
    Forecast = 'FORECAST',
    Historical = 'HISTORICAL'
}

export const MswOverviewPage = () => {
    const [spots, setSpots] = useState<Array<SpotModel>>([]);
    const [selectedFlowStatuses, setSelectedFlowStatuses] = useState([FlowColorEnum.GREEN, FlowColorEnum.ORANGE, FlowColorEnum.RED]);
    const [showGraphOfType, setShowGraphOfType] = useState<GraphTypeEnum>(GraphTypeEnum.Forecast);
    const [isLoading, setIsLoading] = useState(true);

    const filteredSpots = useMemo(
        () =>
            spots.filter(spot =>
                selectedFlowStatuses.includes(spot.flowStatus)
            ),
        [spots, selectedFlowStatuses]
    );

    const areAllFlowStatusEnumsSelected = useMemo(
        () => selectedFlowStatuses.length === 3,
        [selectedFlowStatuses]
    );

    // @ts-ignore
    const {user, token} = useUserAuth();

    useEffect(() => {
        // Wait until Firebase has resolved the auth state (user is either null or non-null)
        const authResolved = user !== undefined;

        // Only fetch once when auth state is known
        if (authResolved) {
            spotsService.fetchData(token);
        }
    }, [user, token]);

    useEffect(() => {
        const updateSpots = (newSpots: SpotModel[]) => {
            setSpots(newSpots);
            setIsLoading(false);
        };
        spotsService.subscribe(updateSpots);

        return () => spotsService.unsubscribe(updateSpots);
    }, []);

    // initial loading
    useEffect(() => {
        stationsService.fetchData();
    }, []);

    // Initialize pull-to-refresh for iOS PWA
    useEffect(() => {
        if (isIosPwa()) {
            const ptr = PullToRefresh.init({
                mainElement: '.App',
                getStyles: () => pullToRefreshStyles,
                onRefresh() {
                    return spotsService.fetchData(token).then(() => {
                        // Optional: add a small delay to ensure smooth animation
                        return new Promise(resolve => setTimeout(resolve, 300));
                    });
                },
                instructionsPullToRefresh: 'Pull down to refresh',
                instructionsReleaseToRefresh: 'Release to refresh',
                instructionsRefreshing: 'Refreshing...',
                distThreshold: 60,
                distMax: 80,
                distReload: 50,
                // resistance: 2.5
            });

            return () => {
                ptr.destroy();
            };
        }
    }, [token]);

    return <>
        <div className="App">
            <MswHeader/>
            {isLoading ? (
                <MswLoader />
            ) : spots.length > 0 ? (
                getContent()
            ) : (
                <div className="no-spots-message-container">
                    <div>No spots saved.</div>
                    <div className="add-spot-link">Add a new spot <MswAddSpot/></div>
                </div>
            )}
            <MswFooter/>
        </div>
    </>;

    function getContent() {
        let riverSurfSpots = filteredSpots.filter(l => l.spotType === "RIVER_SURF");
        let bungeeSurfSpots = filteredSpots.filter(l => l.spotType === "BUNGEE_SURF");
        return <>
            <FlowStatusFilterChips
                initialSelected={selectedFlowStatuses}
                onChange={statuses => {
                    setSelectedFlowStatuses(statuses);
                }}
            />
            <div className="surfspots">
                {isNotEmpty(riverSurfSpots) &&
                    <SpotList title="Riversurf" spots={riverSurfSpots} showGraphOfType={showGraphOfType}
                              isSavingNewOrderAllowed={areAllFlowStatusEnumsSelected}/>}
                {isNotEmpty(bungeeSurfSpots) &&
                    <SpotList title="Bungeesurf" spots={bungeeSurfSpots} showGraphOfType={showGraphOfType}
                              isSavingNewOrderAllowed={areAllFlowStatusEnumsSelected}/>}
            </div>
            <Form>
                <Row className="align-items-center">
                    <Col className="text-end">Forecast</Col>
                    <Col xs="auto">
                        <Form.Check
                            type="switch"
                            id="graph-toggle"
                            checked={showGraphOfType === GraphTypeEnum.Historical}
                            onChange={() => {
                                if (showGraphOfType === GraphTypeEnum.Forecast) {
                                    setShowGraphOfType(GraphTypeEnum.Historical)
                                } else {
                                    setShowGraphOfType(GraphTypeEnum.Forecast)
                                }
                            }}
                        />
                    </Col>
                    <Col className="text-start">Historical</Col>
                </Row>
            </Form>
            <MswSpotMap riverSurfSpots={riverSurfSpots} bungeeSurfSpots={bungeeSurfSpots}/>
        </>;
    }
}
