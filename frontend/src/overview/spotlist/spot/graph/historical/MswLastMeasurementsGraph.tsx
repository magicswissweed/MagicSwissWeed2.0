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
import {useTheme} from "../../../../../theme/MswThemeContext";
import {useMemo} from "react";

export const MswLastMeasurementsGraph = (props: MswGraphProps) => {
    const {theme} = useTheme();

    let lineData = props.spot.lastFewDaysLoaded && props.spot.lastFewDays ?
        [
            ...props.spot.lastFewDays,
            {timestamp: props.spot.currentSample.timestamp, flow: props.spot.currentSample.flow}
        ] :
        [];

    // keep only the last 7 days
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - ONE_WEEK);
    lineData = lineData.filter(sample => {
        return Date.parse(sample.timestamp) >= oneWeekAgo.getTime();
    });

    let midDayTicks = getTicksAt(12, getTimestamps(lineData));
    let startOfDayTicks = getTicksAt(0, getTimestamps(lineData));

    const layout = useMemo(() => {
        const baseLayout = getCommonPlotlyLayout(
            props.isMini,
            getTimestamps(lineData),
            props.spot.minFlow,
            props.spot.maxFlow,
            false,
            theme);
        return {
            ...baseLayout,
            xaxis: {
                ...baseLayout.xaxis,
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
                ...(baseLayout.shapes || []),
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
    }, [
        props.isMini,
        props.spot.minFlow,
        props.spot.maxFlow,
        lineData,
        theme
    ]);

    if (props.spot.lastFewDaysLoaded) {
        if (!lineData || lineData.length === 0) {
            return <div>Detailed Graph not possible at the moment...</div>
        }
    } else {
        return <MswLoader/>
    }

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
