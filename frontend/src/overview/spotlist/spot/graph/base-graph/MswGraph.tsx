import {ApiFlowSample} from "../../../../../gen/msw-api-ts";
import {SpotModel} from "../../../../../model/SpotModel";
import {Config, Layout} from 'plotly.js';

export class MswGraphProps {
    spot: SpotModel;
    isMini: boolean;

    constructor(spot: SpotModel, isMini = false) {
        this.spot = spot;
        this.isMini = isMini;
    }
}

// Common time constants
const ONE_HOUR = 60 * 60 * 1000;
const ONE_DAY = 24 * ONE_HOUR;
export const ONE_WEEK = 7 * ONE_DAY;

// Color configuration for the plots
const green = 'rgb(15, 125, 72)';
const blue = 'rgb(59, 96, 232)';
const darkBlue = 'rgba(30, 144, 150, 0.7)';
const lightBlue = 'rgba(117, 212, 217, 0.7)';
const transparentGreen = 'rgba(7, 169, 37, 0.5)';
export const plotColors = {
    measured: green,
    median: blue,
    percentileRange: {line: 'gray', fill: darkBlue},
    minMaxRange: {line: 'gray', fill: lightBlue},
    currentTime: {line: 'gray'},
    acceptableRange: {fill: transparentGreen}
};

// Extract timestamps and flows from a data series
export function getTimestamps(data: ApiFlowSample[]): string[] {
    return data.map(item => item.timestamp).sort()
}

function getFlows(data: ApiFlowSample[]): number[] {
    return data.map(item => item.flow);
}

export function getAspectRatio(isMini: boolean): number {
    return isMini ? 3 : 2;
}

// Calculate maximum Y value from data series with padding
export function calculateMaxY(measured: ApiFlowSample[], max: ApiFlowSample[], paddingPercent: number = 10): number {
    const maxY = Math.max(
        Math.max(...measured.map(m => m.flow)),
        Math.max(...max.map(m => m.flow))
    )

    return maxY * (1 + paddingPercent / 100); // Add padding percentage
}

// Create a trace for Plotly with common defaults
export function createTrace(
    data: ApiFlowSample[],
    isMini: boolean,
    color?: string,
    name?: string) {
    return {
        x: getTimestamps(data),
        y: getFlows(data),
        type: 'scatter' as const,
        mode: 'lines' as const,
        line: {width: 1, shape: 'spline' as const, color},
        name,
        showlegend: !isMini,
        hoverinfo: isMini ? 'skip' as const : 'all' as const,
        hovertemplate: isMini ? undefined : '%{x|%d.%m.%Y %H:%M}<br>Flow: %{y:.1f}<extra></extra>',
    };
}

export function createAreaTrace(
    upperData: ApiFlowSample[],
    lowerData: ApiFlowSample[],
    name: string,
    fillcolor: string,
    isMini: boolean) {
    return [
        {
            ...createTrace(upperData, isMini, 'transparent'),
            showlegend: false
        },
        {
            ...createTrace(lowerData, isMini, 'transparent', name),
            fill: 'tonexty',
            fillcolor: fillcolor,
        }
    ];
}

// Common Plotly config
export const commonPlotlyConfig: Partial<Config> = {
    responsive: true,
    displayModeBar: false,
    scrollZoom: false,
    doubleClick: false as const,
    modeBarButtonsToRemove: ['zoom2d', 'pan2d', 'select2d', 'lasso2d', 'zoomIn2d', 'zoomOut2d', 'autoScale2d', 'resetScale2d']
};

// Common Plotly layout configuration
export function getCommonPlotlyLayout(
    isMini: boolean,
    allTimestamps: string[] = [],
    minFlow?: number,
    maxFlow?: number,
    showCurrentTimeLine: boolean = true
): Partial<Layout> {
    const lightGray = 'rgba(211, 211, 211, 0.5)';
    return {
        // TODO: clean up unused properties
        // autosize: true,
        paper_bgcolor: 'transparent',
        plot_bgcolor: 'transparent',
        xaxis: {
            showgrid: true,
            gridcolor: lightGray,
            showticklabels: !isMini,
            range: allTimestamps.length ? [allTimestamps[0], allTimestamps[allTimestamps.length - 1]] : undefined,
        },
        yaxis: {
            showticklabels: !isMini,
            gridcolor: isMini ? 'transparent' : lightGray,
            ticklabelposition: 'inside' as const
        },
        legend: !isMini ? {
            orientation: 'h',
            y: -0.1,
            yanchor: 'top',
            xanchor: 'center',
            x: 0.5,
            itemclick: false,
            itemdoubleclick: false
        } : undefined,
        margin: isMini ?
            {l: 5, r: 5, t: 5, b: 5} :
            {l: 30, r: 30, t: 0, b: 0}, // provide space for x-axis labels without legend
        shapes: [
            // Vertical line showing current time
            ...(
                showCurrentTimeLine
                    ? [{
                        type: 'line' as const,
                        x0: new Date().getTime(),
                        x1: new Date().getTime(),
                        y0: 0,
                        y1: 1,
                        yref: 'paper' as const,
                        line: {
                            color: plotColors.currentTime.line,
                            width: 2,
                            dash: 'dash' as const
                        }
                    }]
                    : []
            ),
            // Horizontal band for acceptable flow range
            ...(minFlow !== undefined && maxFlow !== undefined && allTimestamps.length > 0 ? [{
                type: 'rect' as const,
                x0: allTimestamps[0],
                x1: allTimestamps[allTimestamps.length - 1],
                y0: minFlow,
                y1: maxFlow,
                fillcolor: plotColors.acceptableRange.fill,
                line: {width: 0},
                layer: 'below' as const
            }] : []),
        ],
        hoverlabel: isMini ? undefined : {bgcolor: 'white', bordercolor: 'gray', font: {size: 13}},
        hovermode: isMini ? false : 'closest' as const,
        dragmode: false as const
    };
}
