import './MswMeasurement.scss'
import {Component} from 'react';
import {SpotModel} from "../../../../model/SpotModel";
import {formatValue} from "../../../../utils/formatValue";
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
                    {this.getMeasurement()}
                </div>

                {this.spot.currentTemperature &&
                    <div className="measurement_row meas temp">
                        {this.getTemp(this.spot.currentSample.temperature)}
                    </div>
                }
            </div>
        </>;
    }

    private getMeasurement() {
        let flowUnit = <>m<sup>3</sup>/s</>;
        let heightUnit = "cm";
        return <>
            <div className={this.spot.flowStatus}>{formatValue(this.spot.currentSample.value)}</div>
            <div className="unit">
                {this.spot.measurementType === ApiMeasurementType.Height ? heightUnit : flowUnit}
            </div>
        </>;
    }


    private getTemp() {
        let temp: number = this.spot.currentTemperature?.value ?? 0;
        return <>
            <div>{temp.toFixed(1)}</div>
            <div className="unit">°C</div>
        </>;
    }
}
