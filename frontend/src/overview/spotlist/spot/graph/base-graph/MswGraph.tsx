import {ApiFlowSample, ApiLineEntry} from "../../../../../gen/msw-api-ts";
import React from "react";
import {CartesianGrid, Label, Line, ReferenceArea, ReferenceLine, Tooltip, XAxis} from 'recharts';
import {SpotModel} from "../../../../../model/SpotModel";

export const DATA_KEY_MEDIAN = "median";
export const DATA_KEY_MEASURED = "measured";

export const LINE_NAME_MEASURED = "measured";
export let LINE_NAME_MEDIAN = "median";

export interface MswGraphProps {
    spot: SpotModel,
    aspectRatio: number,
    withLegend?: boolean | undefined,
    withYAxis?: boolean | undefined
    withXAxis?: boolean | undefined;
    withMinMaxReferenceLines?: boolean | undefined;
    withTooltip?: boolean | undefined;
}

export type NormalizedDataItem = { datetime: Date, [lineName: string]: unknown; };

export function getMinMaxReferenceLines(spot: SpotModel) {
    return <>
        <ReferenceLine y={spot.minFlow}>
            <Label value={spot.minFlow} position="insideRight"/>
        </ReferenceLine>
        <ReferenceLine y={spot.maxFlow}>
            <Label value={spot.maxFlow} position="insideRight"/>
        </ReferenceLine>
    </>;
}

export function getTooltip() {
    // @ts-ignore
    const MswTooltip = ({active, payload, label}) => {
        if (active && payload && payload.length) {

            let flowStr: string = "";
            let color: string = "black";
            for (let payloadItem of payload) {
                if (payloadItem.dataKey === DATA_KEY_MEASURED) {
                    flowStr = "Measured: " + payloadItem.value;
                    color = payloadItem.stroke;
                } else if (payloadItem.dataKey === DATA_KEY_MEDIAN) {
                    flowStr = "Median: " + payloadItem.value;
                    color = payloadItem.stroke;
                }
            }

            return (
                <div className="tooltip">
                    <p className="tooltip_timestamp">{label.getDate() + "." + (label.getMonth() + 1) + ". " + label.getHours() + ":00"}</p>
                    <p className="tooltip_value" style={{color: color}}>{flowStr}</p>
                </div>
            );
        }

        return null;
    };

    // @ts-ignore
    return <Tooltip content={MswTooltip}/>;
}

export function normalizeGraphDataLine(line: ApiFlowSample[] | ApiLineEntry[], name: string): NormalizedDataItem[] {
    let normalizedData: any[] = [];
    for (let linePoint of line) {
        let obj: NormalizedDataItem = {datetime: new Date(linePoint.timestamp!)};
        obj[name] = linePoint.flow;
        normalizedData.push(obj);
    }
    return normalizedData;
}

export function getReferenceArea(spot: SpotModel) {
    return <ReferenceArea y1={spot.minFlow}
                          y2={spot.maxFlow}
                          ifOverflow="extendDomain"
                          fill="green"/>;
}

export function getCurrentTimeReferenceLine() {
    return <ReferenceLine x={Date.now()} stroke="#666666"/>;
}

export function getMeasuredLine() {
    return <Line type="monotone"
                 dataKey={DATA_KEY_MEASURED}
                 stroke="green"
                 dot={false}
                 name={LINE_NAME_MEASURED}
                 activeDot={{stroke: 'green', strokeWidth: 1, r: 4}}/>;
}

export function getCartesianGrid() {
    return <CartesianGrid/>;
}

export function getXAxis(ticks: number[], withXAxis: boolean, tickFormatter: (v: any) => string) {
    const from = ticks[0];
    const to = ticks[ticks.length - 1];

    return (
        <XAxis
            type="number"
            dataKey="datetime"
            domain={[from, to]}
            scale="time"
            ticks={ticks}
            tickFormatter={tickFormatter}
            minTickGap={1}
            hide={!withXAxis}
            tick={{dx: 20}}
        />
    );
}
