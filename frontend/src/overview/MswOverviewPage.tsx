import "./MswOverviewPage.scss";
import React, {useEffect, useState} from "react";
import {MswHeader} from '../header/MswHeader';
import {MswFooter} from '../footer/MswFooter';
import {SpotList} from './spotlist/SpotList'
import {MswLoader} from '../loader/MswLoader';
import {useUserAuth} from '../user/UserAuthContext';
import {spotsService} from "../service/SpotsService";
import {Col, Form, Row} from "react-bootstrap";
import {MswSpotMap} from "./map/spot-map/MswSpotMap";
import {SpotModel} from "../model/SpotModel";
import {stationsService} from "../service/StationsService";
import {MswAddSpot} from "../spot/add/MswAddSpot";

function isNotEmpty(array: Array<any> | undefined) {
    return array && array.length > 0;
}

export enum GraphTypeEnum {
    Forecast = 'FORECAST',
    Historical = 'HISTORICAL'
}

export const MswOverviewPage = () => {
    const [spots, setSpots] = useState<Array<SpotModel>>([]);
    const [showGraphOfType, setShowGraphOfType] = useState<GraphTypeEnum>(GraphTypeEnum.Forecast);
    const [isLoading, setIsLoading] = useState(true);

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
        let riverSurfSpots = spots.filter(l => l.spotType === "RIVER_SURF");
        let bungeeSurfSpots = spots.filter(l => l.spotType === "BUNGEE_SURF");
        return <>
            <div className="surfspots">
                {isNotEmpty(riverSurfSpots) &&
                    <SpotList title="Riversurf" spots={riverSurfSpots} showGraphOfType={showGraphOfType}/>}
                {isNotEmpty(bungeeSurfSpots) &&
                    <SpotList title="Bungeesurf" spots={bungeeSurfSpots} showGraphOfType={showGraphOfType}/>}
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
