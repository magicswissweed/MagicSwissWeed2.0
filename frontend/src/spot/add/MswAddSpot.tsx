import './MswAddSpot.scss';
import 'react-bootstrap-typeahead/css/Typeahead.css';
import React, {useEffect, useRef, useState} from "react";
import {Button} from 'react-bootstrap';
import {ApiSpot, ApiSpotSpotTypeEnum, ApiStation, SpotsApi} from '../../gen/msw-api-ts';
import {useUserAuth} from '../../user/UserAuthContext';
import {AxiosResponse} from "axios";
import {v4 as uuid} from 'uuid';
import {authConfiguration} from "../../api/config/AuthConfiguration";
import {spotsService} from "../../service/SpotsService";
import {MswAddOrEditSpotModal} from "../MswAddOrEditUtil";
import {stationsService} from "../../service/StationsService";
import plus_icon from '../../assets/plus_icon.svg';

export const MswAddSpot = () => {

    const [showAddSpotModal, setShowAddSpotModal] = useState(false);
    const handleShowAddSpotModal = () => setShowAddSpotModal(true);

    function resetValuesToDefault() {
        setSpotName("");
        setType(ApiSpotSpotTypeEnum.RiverSurf);
        setStationId(undefined);
        setMinFlow(undefined);
        setMaxFlow(undefined);
    }

    const handleAddSpotAndCloseModal = (e: { preventDefault: any; }) => {
        e.preventDefault();
        addSpot().then(() => {
            setIsSubmitButtonDisabled(false);
            resetValuesToDefault();
        });
    }
    const handleCancelAddSpotModal = () => {
        setShowAddSpotModal(false);
        resetValuesToDefault();
    }

    const [spotName, setSpotName] = useState("");
    const [type, setType] = useState<ApiSpotSpotTypeEnum>(ApiSpotSpotTypeEnum.RiverSurf);
    const [stationId, setStationId] = useState<number | undefined>(undefined);
    const [minFlow, setMinFlow] = useState<number | undefined>(undefined);
    const [maxFlow, setMaxFlow] = useState<number | undefined>(undefined);
    const [withNotification, setWithNotification] = useState(false);
    const [stations, setStations] = useState<ApiStation[]>([])
    const [isSubmitButtonDisabled, setIsSubmitButtonDisabled] = useState(false);
    const [stationSelectionError, setStationSelectionError] = useState('');

    useEffect(() => {
        setStations(stationsService.getStations());
    }, []);


    // @ts-ignore
    const {token} = useUserAuth();

    const formRef = useRef<HTMLFormElement | null>(null);

    async function addSpot() {
        if (!stationId) {
            setStationSelectionError('Please select a valid option.');
            return;
        } else {
            setStationSelectionError('');
        }

        setIsSubmitButtonDisabled(true);
        let config = await authConfiguration(token);
        const apiSpot: ApiSpot = {
            id: uuid(),
            name: spotName,
            stationId: stationId!,
            spotType: type,
            isPublic: false,
            minFlow: minFlow!,
            maxFlow: maxFlow!,
            station: stations.filter(s => s.id === stationId).pop()!,
            withNotification: withNotification
        };
        let response: AxiosResponse<void, any> = await new SpotsApi(config).addPrivateSpot({spot: apiSpot, position: 0})
        if (response.status === 200) {
            spotsService.fetchData(token).then(handleCancelAddSpotModal);
        } else {
            alert("Sorry, it looks like we can't add that spot. Maybe the flow is not measured at this station?");
        }
    }

    let isEditMode = false;
    return <>
        {/* <Button variant='msw-outline me-2' size='sm' onClick={() => handleShowAddSpotModal()}>Add Spot</Button> */}
        <div className='icon' onClick={() => handleShowAddSpotModal()}>
            <img className="button" alt="Add a new spot to your dashboard." title="Add a new spot to your dashboard." src={plus_icon}/>
        </div>
        {MswAddOrEditSpotModal(
            showAddSpotModal,
            handleCancelAddSpotModal,
            formRef,
            handleAddSpotAndCloseModal,
            spotName,
            setSpotName,
            type,
            setType,
            setStationId,
            setStationSelectionError,
            stations,
            stationId,
            stationSelectionError,
            minFlow,
            setMinFlow,
            maxFlow,
            setMaxFlow,
            withNotification,
            setWithNotification,
            isSubmitButtonDisabled,
            setIsSubmitButtonDisabled,
            isEditMode)}
    </>;
}
