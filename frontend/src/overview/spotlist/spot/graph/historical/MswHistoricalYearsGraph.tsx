import '../base-graph/MswGraph.scss'
import {
    calculateMaxY,
    commonPlotlyConfig,
    createAreaTrace,
    createTrace,
    getAspectRatio,
    getCommonPlotlyLayout,
    getTimestamps,
    MswGraphProps,
    plotColors
} from "../base-graph/MswGraph";
import Plot from 'react-plotly.js';

export const MswHistoricalYearsGraph = (props: MswGraphProps) => {
    if (!props.spot.historical) {
        return (
            <div>Detailed Graph not possible at the moment...</div>
        );
    }

    // no data processing needed for this graph
    const processedData = {
        measured: props.spot.historical.currentYear,
        median: props.spot.historical.median,
        min: props.spot.historical.min,
        max: props.spot.historical.max,
        p25: props.spot.historical.twentyFivePercentile,
        p75: props.spot.historical.seventyFivePercentile
    };

    // Get timestamps for x-axis grid and labels
    const allTimestamps = getTimestamps(processedData.median);

    // Calculate y-axis maximum with 10% padding
    const maxY = calculateMaxY(processedData.measured || [], processedData.max || [], 10);

    const layout = {
        ...getCommonPlotlyLayout(props.isMini, allTimestamps, props.spot.minFlow, props.spot.maxFlow),
        xaxis: {
            ...getCommonPlotlyLayout(props.isMini).xaxis,
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
            ...getCommonPlotlyLayout(props.isMini).yaxis,
            // Force graph to show x-axis by setting explicit range from zero
            range: [0, maxY],
        },
        shapes: [
            ...(getCommonPlotlyLayout(props.isMini, allTimestamps, props.spot.minFlow, props.spot.maxFlow).shapes || []),
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
                    color: 'rgba(169, 169, 169, 0.8)', // Dark gray for month lines
                    width: 1
                },
                layer: 'below' as const
            }))
        ]
    };

    return (
        <Plot
            data={[
                // Bottom layer: Min-max range
                ...createAreaTrace(
                    processedData.max,
                    processedData.min,
                    'Min-Max',
                    plotColors.minMaxRange.fill,
                    props.isMini),

                // Middle layer: 25-75 percentile range
                ...createAreaTrace(
                    processedData.p75,
                    processedData.p25,
                    '25-75%',
                    plotColors.percentileRange.fill,
                    props.isMini
                ),

                // Top layers: Historical median and measured data
                createTrace(
                    processedData.median,
                    props.isMini,
                    plotColors.median,
                    'Median',
                ),
                createTrace(
                    processedData.measured,
                    props.isMini,
                    'Measured',
                    plotColors.measured,)
            ]}
            layout={layout}
            style={{
                width: '100%',
                aspectRatio: getAspectRatio(props.isMini)
            }}
            useResizeHandler={true}
            config={{...commonPlotlyConfig, staticPlot: props.isMini}}
        />
    );
};

