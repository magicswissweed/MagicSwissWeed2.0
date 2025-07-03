import './SpotList.scss';
import React, {useCallback, useEffect, useState} from 'react';
import {DragDropContext, Draggable, Droppable} from 'react-beautiful-dnd';
import {SpotsApi} from '../../gen/msw-api-ts';
import {authConfiguration} from '../../api/config/AuthConfiguration';
import {useUserAuth} from '../../user/UserAuthContext';
import {Spot} from "./spot/Spot";
import {GraphTypeEnum} from "../MswOverviewPage";
import {SpotModel} from "../../model/SpotModel";
import curved_arrow from '../../assets/curved_arrow.svg';
import {useAuthModal} from '../../user/AuthModalContext';

interface SpotListProps {
    title: string,
    spots: Array<SpotModel>,
    showGraphOfType: GraphTypeEnum
}

export const SpotList = (props: SpotListProps) => {
    const [spots, setSpots] = useState<Array<SpotModel>>(props.spots);
    const {setShowSignupModal} = useAuthModal();

    // @ts-ignore
    const {user, token} = useUserAuth();

    useEffect(() => {
        setSpots(props.spots);
    }, [props.spots]);

    // FIXME: is the effect only dependent on the token for the initial order? If so: move to backend for more clarity
    const saveSpotsOrdering = useCallback(async (spots: Array<SpotModel>) => {
        let config = await authConfiguration(token);
        await new SpotsApi(config).orderSpots(
            spots
                .filter(loc => loc.id)
                .map(loc => loc.id!));
    }, [token]);

    useEffect(() => {
        if (user) {
            // no await, because we don't want the frontend to be blocked
            saveSpotsOrdering(spots);
        }
    }, [spots])

    const handleDrop = async (result: any) => {
        if (!result.destination) return;
        const reorderedItems = Array.from(spots);
        const [removed] = reorderedItems.splice(result.source.index, 1);
        reorderedItems.splice(result.destination.index, 0, removed);

        setSpots(reorderedItems);
    };

    return <>
        <div className="spotsContainer">
            <h2>{props.title}</h2>
            <DragDropContext onDragEnd={handleDrop}>
                <Droppable droppableId="locations-wrapper">
                    {(droppableProvided: any) => (
                        <div {...droppableProvided.droppableProps} ref={droppableProvided.innerRef}>
                            {spots.map((spot: SpotModel, index: number) => (
                                <Draggable key={spot.id} draggableId={spot.id!} index={index}>
                                    {(draggableProvided: any) => (
                                        <div className="draggable-container"
                                             ref={draggableProvided.innerRef} {...draggableProvided.draggableProps}>
                                            <Spot spot={spot}
                                                  dragHandleProps={draggableProvided.dragHandleProps}
                                                  showGraphOfType={props.showGraphOfType}/>
                                        </div>
                                    )}
                                </Draggable>
                            ))}
                            {!user && props.title === "Riversurf" &&
                                <div className="draggable-container">
                                    <div className="guest-message">
                                        <div className="curved-arrow">
                                            <img src={curved_arrow} alt=""/>
                                        </div>
                                        <h3>
                                            <span id='black'>Wanna add your own surf spots?</span>
                                            Simply
                                            <span onClick={() => setShowSignupModal(true)}
                                                  style={{cursor: 'pointer', textDecoration: 'underline'}}>
                                                sign up
                                            </span>
                                        </h3>
                                    </div>
                                </div>
                            }
                            {droppableProvided.placeholder}
                        </div>
                    )}
                </Droppable>
            </DragDropContext>
        </div>
    </>;
}
