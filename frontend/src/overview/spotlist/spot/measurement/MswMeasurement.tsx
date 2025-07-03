import './MswMeasurement.scss'
import {Component} from 'react';
import {SpotModel} from "../../../../model/SpotModel";

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
        return <>
            <div className="measurements"
                 title={'Measured at: ' + this.convertTimeStampToDisplayableString(this.spot.currentSample!.timestamp!)}
                 tabIndex={0}>
                <div className="measurement_row meas flow">
                    {this.getFlow()}
                </div>

                {this.spot.currentSample!.temperature &&
                    <div className="measurement_row meas temp">
                        {this.getTemp()}
                    </div>
                }
            </div>
        </>;
    }

    private getFlow() {
        return <>
            <div className={this.spot.flowStatus}>{this.spot.currentSample.flow}</div>
            <div className="unit">
                m<sup>3</sup>/s
            </div>
        </>;
    }


    private getTemp() {
        let temp: number = this.spot.currentSample!.temperature ?? 0;
        return <>
            <div>{temp}</div>
            <div className="unit">°C</div>
        </>;
    }

    private convertTimeStampToDisplayableString(timestampString: string) {
        let date = new Date(timestampString);
        return date.getHours() + ":" + ((date.getMinutes() === 0) ? "00" : date.getMinutes());
    }
}
