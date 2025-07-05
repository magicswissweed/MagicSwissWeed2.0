import '../base-graph/MswGraph.scss'
import {
    commonPlotlyConfig,
    createTrace,
    getAspectRatio,
    getCommonPlotlyLayout,
    getTimestamps,
    MswGraphProps,
    ONE_WEEK,
    plotColors
} from "../base-graph/MswGraph";
import {MswLoader} from "../../../../../loader/MswLoader";
import Plot from 'react-plotly.js';

export const MswLastMeasurementsGraph = (props: MswGraphProps) => {
    if (props.spot.last40DaysLoaded) {
        if (!props.spot.last40Days || props.spot.last40Days.length === 0) {
            return <div>Detailed Graph not possible at the moment...</div>
        }
    } else {
        return <MswLoader/>
    }

    // Calculate weekly ticks for x-axis
    const now = new Date();
    const sixWeeksAgo = new Date(now.getTime() - (6 * ONE_WEEK));
    const weeklyTicks = [];
    const weeklyLabels = [];

    for (let date = new Date(now); date >= sixWeeksAgo; date = new Date(date.getTime() - ONE_WEEK)) {
        weeklyTicks.push(date.getTime());
        weeklyLabels.push(
            `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1).toString().padStart(2, '0')}`
        );
    }

    // Get common layout and extend it with last measurements specific settings
    const layout = {
        ...getCommonPlotlyLayout(
            props.isMini,
            getTimestamps(props.spot.last40Days),
            props.spot.minFlow,
            props.spot.maxFlow,
            false // do not show current time line
        ),
        xaxis: {
            ...getCommonPlotlyLayout(props.isMini, getTimestamps(props.spot.last40Days)).xaxis,
            tickvals: weeklyTicks,
            ticktext: weeklyLabels,
        }
    };

    return (
        <Plot
            data={[
                createTrace(props.spot.last40Days, props.isMini, plotColors.measured, 'Measured')
            ]}
            layout={layout}
            style={{width: '100%', aspectRatio: getAspectRatio(props.isMini)}}
            useResizeHandler={true}
            config={{...commonPlotlyConfig, staticPlot: props.isMini}}
        />
    );
};
