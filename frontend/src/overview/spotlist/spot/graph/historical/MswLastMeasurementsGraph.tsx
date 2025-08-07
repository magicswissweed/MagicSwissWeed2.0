import '../base-graph/MswGraph.scss'
import {
    commonPlotlyConfig,
    createTrace,
    getCommonPlotlyLayout,
    getTicksAt,
    getTimestamps,
    MswGraphProps,
    ONE_WEEK,
    plotColors
} from "../base-graph/MswGraph";
import {MswLoader} from "../../../../../loader/MswLoader";
import Plot from 'react-plotly.js';

export const MswLastMeasurementsGraph = (props: MswGraphProps) => {
    let lineData = props.spot.last40DaysLoaded && props.spot.last40Days ?
        [
            ...props.spot.last40Days,
            {timestamp: props.spot.currentSample.timestamp, flow: props.spot.currentSample.flow}
        ] :
        [];
    if (props.spot.last40DaysLoaded) {
        if (!lineData || lineData.length === 0) {
            return <div>Detailed Graph not possible at the moment...</div>
        }
    } else {
        return <MswLoader/>
    }

    // keep only the last 7 days
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - ONE_WEEK);
    lineData = lineData.filter(sample => {
        // Ensure sample.timestamp is a number for comparison
        const sampleTimestamp = typeof sample.timestamp === 'string'
            ? Date.parse(sample.timestamp)
            : sample.timestamp;
        return sampleTimestamp >= oneWeekAgo.getTime();
    });

    // Get common layout and extend it with forecast-specific settings
    let midDayTicks = getTicksAt(12, getTimestamps(lineData));
    let startOfDayTicks = getTicksAt(0, getTimestamps(lineData));

    // Get common layout and extend it with last measurements specific settings
    const layout = {
        ...getCommonPlotlyLayout(
            props.isMini,
            getTimestamps(lineData),
            props.spot.minFlow,
            props.spot.maxFlow,
            false // do not show current time line
        ),
        xaxis: {
            ...getCommonPlotlyLayout(props.isMini, getTimestamps(lineData)).xaxis,
            // Only show labels at noon
            tickvals: midDayTicks,
            // Format labels as weekday names
            ticktext: midDayTicks
                .map(timestamp => {
                    const date = new Date(timestamp);
                    const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
                    return weekdays[date.getDay()];
                }),
        },
        shapes: [
            ...(getCommonPlotlyLayout(
              props.isMini, 
              getTimestamps(lineData), 
              props.spot.minFlow, 
              props.spot.maxFlow,
              false
            ).shapes || []),
            // Vertical lines at midnight (darker than noon grid)
            ...(getTimestamps(lineData).length > 0 ?
              startOfDayTicks
                  .map(timestamp => ({
                      type: 'line' as const,
                      x0: timestamp,
                      x1: timestamp,
                      y0: 0,
                      y1: 1,
                      yref: 'paper' as const,
                      line: {
                          color: 'rgba(169, 169, 169, 0.8)',  // Dark gray for midnight lines
                          width: 1
                      },
                      layer: 'below' as const
                  }))
              : []
      ),
        ]
    };

    return (
        <Plot
            data={[
                createTrace(lineData, !props.isMini, props.isMini, plotColors.measured, 'Measured')
            ]}
            layout={layout}
            style={{width: '100%', height: '100%'}}
            useResizeHandler={true}
            config={{...commonPlotlyConfig, staticPlot: props.isMini}}
        />
    );
};
