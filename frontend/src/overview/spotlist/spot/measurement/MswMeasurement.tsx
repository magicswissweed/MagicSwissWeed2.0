import './MswMeasurement.scss'
import {Component} from 'react';
import {SpotModel} from "../../../../model/SpotModel";
import {formatFlow} from "../../../../utils/formatFlow";
import {ApiMeasurementType} from "../../../../gen/msw-api-ts";

interface MeasurementsProps {
    spot: SpotModel
}

export class MswMeasurement extends Component<MeasurementsProps> {

    private readonly spot: SpotModel;

    constructor(props: MeasurementsProps) {
        super(props);
        this.spot = props.spot;
    }

    render() {
        if (!this.spot.currentSample) {
            return <>
                <div className="measurements pending"
                     tabIndex={0}>
                    <div className="pending-message">Data is being fetched...</div>
                </div>
            </>;
        }

        return <>
            <div className="measurements"
                 tabIndex={0}>
                <div className="measurement_row meas flow">
                    {this.getFlow(this.spot.currentSample.flow)}
                </div>

                {this.spot.currentSample.temperature &&
                    <div className="measurement_row meas temp">
                        {this.getTemp(this.spot.currentSample.temperature)}
                    </div>
                }
            </div>
        </>;
    }

    private getFlow(flow: number) {
        let flowUnit = <>m<sup>3</sup>/s</>;
        let heightUnit = "cm";
        return <>
            <div className={this.spot.flowStatus}>{formatFlow(flow)}</div>
            <div className="unit">
                {this.spot.measurementType === ApiMeasurementType.Height ? heightUnit : flowUnit}
            </div>
        </>;
    }


    private getTemp(temperature: number) {
        return <>
            <div>{temperature}</div>
            <div className="unit">°C</div>
        </>;
    }
}
