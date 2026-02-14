import '../base-graph/MswGraph.scss'
import {
    commonPlotlyConfig,
    createAreaTrace,
    createTrace,
    getCommonPlotlyLayout,
    getTimestamps,
    MswGraphProps,
    plotColors
} from "../base-graph/MswGraph";
import Plot from 'react-plotly.js';
import {useMemo} from "react";
import {useTheme} from "../../../../../theme/MswThemeContext";
import {SpotModel} from "../../../../../model/SpotModel";

export const MswHistoricalYearsGraph = (props: MswGraphProps) => {
    const {theme} = useTheme();

    const maxY = useMemo(() => {
        return calculateMaxY(props.spot);
    }, [props.spot, props.spot.historical]);

    const layout = useMemo(() => {
        const invertedRgb = getComputedStyle(document.documentElement)
            .getPropertyValue('--bg-inverted-rgb')
            .trim();

        let baseLayout = getCommonPlotlyLayout(
            props.isMini,
            getTimestamps(props.spot.historical?.median || []),
            props.spot.minFlow,
            props.spot.maxFlow,
            true,
            theme);
        return {
            ...baseLayout,
            xaxis: {
                ...baseLayout.xaxis,
                // Show month labels in the middle of each month
                tickvals: Array.from({length: 12}, (_, i) => {
                    const date = new Date();
                    date.setMonth(i);
                    date.setDate(15); // Middle of month
                    date.setHours(12, 0, 0, 0);
                    return date.getTime();
                }),
                // Format labels as month abbreviations
                ticktext: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],

            },
            yaxis: {
                ...baseLayout.yaxis,
                range: [0, maxY],
            },
            shapes: [
                ...(baseLayout.shapes || []),
                // Vertical lines at month boundaries (1st of each month)
                ...Array.from({length: 11}, (_, i) => {
                    const date = new Date();
                    date.setMonth(i + 1);
                    date.setDate(1);
                    date.setHours(0, 0, 0, 0);
                    return date.getTime();
                }).map(timestamp => ({
                    type: 'line' as const,
                    x0: timestamp,
                    x1: timestamp,
                    y0: 0,
                    y1: 1,
                    yref: 'paper' as const,
                    line: {
                        color: `rgba(${invertedRgb}, 0.3)`,
                        width: 1
                    },
                    layer: 'below' as const
                }))
            ]
        }
    }, [
        props.isMini,
        getTimestamps(props.spot.historical?.median || []),
        props.spot.minFlow,
        props.spot.maxFlow,
        theme,
        maxY
    ]);

    if (!props.spot.historical) {
        return (
            <div>Detailed Graph not possible at the moment...</div>
        );
    }

    return (
        <Plot
            data={[
                // Bottom layer: Min-max range
                ...createAreaTrace(
                    props.spot.historical?.max!,
                    props.spot.historical?.min!,
                    'Min-Max',
                    plotColors.minMaxRange.fill,
                    props.isMini),

                // Middle layer: 25-75 percentile range
                ...createAreaTrace(
                    props.spot.historical?.seventyFivePercentile!,
                    props.spot.historical?.twentyFivePercentile!,
                    '25-75%',
                    plotColors.percentileRange.fill,
                    props.isMini),

                // Top layers: Historical median and measured data
                createTrace(
                    props.spot.historical?.median!,
                    !props.isMini,
                    props.isMini,
                    plotColors.median,
                    'Median',
                ),
                createTrace(
                    props.spot.historical?.currentYear!,
                    !props.isMini,
                    props.isMini,
                    plotColors.measured,
                    'Measured')
            ]}
            layout={layout}
            style={{
                width: '100%',
                height: '100%'
            }}
            useResizeHandler={true}
            config={{...commonPlotlyConfig, staticPlot: props.isMini}}
        />
    );
};

function calculateMaxY(spot: SpotModel): number {
    const paddingPercent = 10;
    const maxAllowedFlow = spot.maxFlow || 0;

    // FIXME: looks like min and max got confused on fetching the data. We simply 'fix' it in the frontend by using min instead of max here
    let maxOfHistoricalMax = Math.max(...(spot.historical?.min || []).map(m => m.flow));
    let maxOfCurrentYearMax = Math.max(...(spot.historical?.currentYear || []).map(m => m.flow));
    const max = Math.max(
        maxOfCurrentYearMax,
        maxOfHistoricalMax,
        maxAllowedFlow
    )

    return max * (1 + paddingPercent / 100);
}

