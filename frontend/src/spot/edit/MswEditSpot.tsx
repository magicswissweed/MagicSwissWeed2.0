import './MswEditSpot.scss';
import 'react-bootstrap-typeahead/css/Typeahead.css';
import React, {useEffect, useRef, useState} from "react";
import {ApiSpot, ApiSpotSpotTypeEnum, ApiStation, EditPrivateSpotRequest, SpotsApi} from '../../gen/msw-api-ts';
import {useUserAuth} from '../../user/UserAuthContext';
import {AxiosResponse} from "axios";
import {authConfiguration} from "../../api/config/AuthConfiguration";
import {spotsService} from "../../service/SpotsService";
import edit_icon from "../../assets/edit.svg";
import {MswAddOrEditSpotModal} from "../MswAddOrEditUtil";
import {stationsService} from "../../service/StationsService";
import {SpotModel} from "../../model/SpotModel";

// specify the properties (inputs) for the MswEditSpot component
interface MswEditSpotProps {
    spot: SpotModel;
}

export const MswEditSpot: React.FC<MswEditSpotProps> = ({spot}) => {
    // define modal states
    const [showEditSpotModal, setShowEditSpotModal] = useState(false);
    const handleShowEditSpotModal = () => setShowEditSpotModal(true);
    const handleEditSpotAndCloseModal = (e: { preventDefault: any; }) => {
        e.preventDefault();
        editSpot().then(() => setIsSubmitButtonDisabled(false));
    }
    const handleCancelEditSpotModal = () => setShowEditSpotModal(false);

    // define form states and use the current spot data as initial values
    const [spotName, setSpotName] = useState(spot.name || "");
    const [type, setType] = useState<ApiSpotSpotTypeEnum>(spot.spotType || ApiSpotSpotTypeEnum.RiverSurf);
    const [stationId, setStationId] = useState<number | undefined>(spot.stationId);
    const [minFlow, setMinFlow] = useState<number | undefined>(spot.minFlow);
    const [maxFlow, setMaxFlow] = useState<number | undefined>(spot.maxFlow);
    const [stations, setStations] = useState<ApiStation[]>([])
    const [isSubmitButtonDisabled, setIsSubmitButtonDisabled] = useState(false);
    const [stationSelectionError, setStationSelectionError] = useState('');

    useEffect(() => {
        setStations(stationsService.getStations());
    }, []);

    // @ts-ignore
    const {token} = useUserAuth();

    const formRef = useRef<HTMLFormElement | null>(null);

    async function editSpot() {
        // set potential error message if no station is selected
        if (!stationId) {
            setStationSelectionError('Please select a valid option.');
            return;
        } else {
            setStationSelectionError('');
        }

        setIsSubmitButtonDisabled(true);
        let config = await authConfiguration(token);

        // create new spot object with the updated values
        const apiSpot: ApiSpot = {
            id: spot.id,
            name: spotName,
            stationId: stationId!,
            spotType: type,
            isPublic: false,
            minFlow: minFlow!,
            maxFlow: maxFlow!,
            station: stations.filter(s => s.id === stationId).pop()!
        };

        // wrap spot object in request object and send to API
        const editPrivateSpotRequest: EditPrivateSpotRequest = {
            spot: apiSpot
        };
        let response: AxiosResponse<void, any> = await new SpotsApi(config).editPrivateSpot(spot.id, editPrivateSpotRequest)
        if (response.status === 200) {
            await spotsService.fetchData(token).then(() => {
                handleCancelEditSpotModal();
            });
        } else {
            alert("Sorry, it looks like we can't update that spot. Maybe the flow is not measured at this station?");
        }
    }

    let isEditMode = true;
    return <>
        <div className="icon" onClick={() => handleShowEditSpotModal()}>
            <img alt="Edit this private spot." title="Edit this private spot." src={edit_icon}/>
        </div>
        {MswAddOrEditSpotModal(
            showEditSpotModal,
            handleCancelEditSpotModal,
            formRef,
            handleEditSpotAndCloseModal,
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
            isSubmitButtonDisabled,
            setIsSubmitButtonDisabled,
            isEditMode)}
    </>;
}
