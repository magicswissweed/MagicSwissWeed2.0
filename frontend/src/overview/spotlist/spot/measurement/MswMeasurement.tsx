import './MswMeasurement.scss'
import {Component} from 'react';
import {SpotModel} from "../../../../model/SpotModel";
import {formatValue} from "../../../../utils/formatValue";
import {measurementUnit} from "../../../../helper/ApiMeasurementTypeHelper";

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
                    {this.getMeasurement(this.spot.currentSample.value)}
                </div>

                {this.spot.currentTemperature &&
                    <div className="measurement_row meas temp">
                        {this.getTemp(this.spot.currentTemperature.value)}
                    </div>
                }
            </div>
        </>;
    }

    private getMeasurement(value: number) {
        return <>
            <div className={this.spot.flowStatus}>{formatValue(value)}</div>
            <div className="unit">
                {measurementUnit(this.spot.measurementType)}
            </div>
        </>;
    }

    private getTemp(temp: number) {
        return <>
            <div>{temp.toFixed(1)}</div>
            <div className="unit">°C</div>
        </>;
    }
}
