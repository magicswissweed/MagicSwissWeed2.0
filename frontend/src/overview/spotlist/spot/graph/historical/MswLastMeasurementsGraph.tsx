import '../base-graph/MswGraph.scss'
import {
    createTrace,
    getCommonPlotlyLayout,
    getPlotlyConfig,
    getTicksAt,
    getTimestamps,
    MswGraphProps,
    ONE_WEEK,
    plotColors,
    useTimeAxisClamp
} from "../base-graph/MswGraph";
import {MswLoader} from "../../../../../loader/MswLoader";
import Plot from 'react-plotly.js';
import {useTheme} from "../../../../../theme/MswThemeContext";
import {useMemo} from "react";
import {ApiSample} from "../../../../../gen/msw-api-ts";

interface MswLastMeasurementsGraphProps extends MswGraphProps {
    lastFewDays: Array<ApiSample> | undefined;
    loaded: boolean;
}

export const MswLastMeasurementsGraph = (props: MswLastMeasurementsGraphProps) => {
    const {theme} = useTheme();
    const {lastFewDays, loaded} = props;

    const lineData = loaded && lastFewDays ?
        [
            ...lastFewDays,
            ...(props.spot.currentSample
                ? [{timestamp: props.spot.currentSample.timestamp, value: props.spot.currentSample.value}]
                : [])
        ] :
        [];

    // All available data (~8 days) is kept so the user can pan back, but the
    // default view shows the most recent 7 days.
    const sortedTimestamps = getTimestamps(lineData);
    const lastMs = sortedTimestamps.length
        ? Date.parse(sortedTimestamps[sortedTimestamps.length - 1])
        : undefined;
    const defaultXRange = lastMs !== undefined ? [lastMs - ONE_WEEK, lastMs] : undefined;

    let midDayTicks = getTicksAt(12, sortedTimestamps);
    let startOfDayTicks = getTicksAt(0, sortedTimestamps);

    const uirevision = `${props.spot.stationId.externalId}-${props.spot.measurementType}`;
    const clampHandlers = useTimeAxisClamp(
        sortedTimestamps.length ? Date.parse(sortedTimestamps[0]) : undefined,
        lastMs);
    const layout = useMemo(() => {
        const baseLayout = getCommonPlotlyLayout(
            props.isMini,
            sortedTimestamps,
            props.spot.minValue,
            props.spot.maxValue,
            false,
            theme,
            uirevision);
        return {
            ...baseLayout,
            xaxis: {
                ...baseLayout.xaxis,
                // Default view: most recent 7 days (older data reachable by panning).
                range: defaultXRange ?? baseLayout.xaxis?.range,
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
                ...(sortedTimestamps.length > 0 ?
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
        props.spot.minValue,
        props.spot.maxValue,
        lineData,
        theme,
        uirevision
    ]);

    if (loaded) {
        if (!lineData || lineData.length === 0) {
            return <div>Detailed Graph not possible at the moment...</div>
        }
    } else {
        return <MswLoader/>
    }

    return (
        <Plot
            data={[
                createTrace(lineData, !props.isMini, props.isMini, plotColors.measured, 'Measured', props.spot.measurementType)
            ]}
            layout={layout}
            style={{width: '100%', height: '100%'}}
            useResizeHandler={true}
            config={getPlotlyConfig(props.isMini)}
            onInitialized={clampHandlers.onInitialized}
            onRelayout={clampHandlers.onRelayout}
        />
    );
};
