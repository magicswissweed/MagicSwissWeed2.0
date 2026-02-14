import '../base-graph/MswGraph.scss'
import Plot from 'react-plotly.js';
import {ApiLineEntry} from '../../../../../gen/msw-api-ts';
import {
    commonPlotlyConfig,
    createAreaTrace,
    createTrace,
    getCommonPlotlyLayout,
    getTicksAt,
    getTimestamps,
    MswGraphProps,
    plotColors
} from "../base-graph/MswGraph";
import {MswLoader} from "../../../../../loader/MswLoader";
import {useMemo} from "react";
import {useTheme} from "../../../../../theme/MswThemeContext";

export const MswForecastGraph = (props: MswGraphProps) => {
    const {theme} = useTheme();

    // Get data for plotting
    const {currentSample} = props.spot ?? {};
    const {minFlow, maxFlow} = props.spot ?? {};
    const {measuredData, median, twentyFivePercentile, seventyFivePercentile, max, min} = props.spot.forecast ?? {};

    // Get timestamps for x-axis grid and labels
    const allTimestamps = getTimestamps([...measuredData ?? [], ...median ?? []]);

    // Update all series with current measurement if available
    const removeSamplesBeforeCurrentTime = (series: ApiLineEntry[]) => {
        if (!currentSample || !currentSample.timestamp) {
            return series;
        }

        const currentTime = currentSample.timestamp;
        return [...series.filter(item => item.timestamp > currentTime)];
    };

    // Process all data series
    const processedData = {
        measured: currentSample
            ? [...measuredData ?? [], {timestamp: currentSample.timestamp, flow: currentSample.flow}]
            : measuredData,
        median: removeSamplesBeforeCurrentTime(median ?? []),
        min: removeSamplesBeforeCurrentTime(min ?? []),
        max: removeSamplesBeforeCurrentTime(max ?? []),
        p25: removeSamplesBeforeCurrentTime(twentyFivePercentile ?? []),
        p75: removeSamplesBeforeCurrentTime(seventyFivePercentile ?? [])
    };

    // Get common layout and extend it with forecast-specific settings
    let midDayTicks = getTicksAt(12, allTimestamps);
    let startOfDayTicks = getTicksAt(0, allTimestamps)
    const layout = useMemo(() => {
        let baseLayout = getCommonPlotlyLayout(props.isMini, allTimestamps, minFlow, maxFlow, true, theme);

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
                ...(allTimestamps.length > 0 ?
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
        allTimestamps,
        minFlow,
        maxFlow,
        theme
    ]);

    if (!props.spot.forecastLoaded) {
        return <MswLoader/>;
    }
    if (!props.spot.forecast) {
        return <div>Detailed Forecast not possible at the moment...</div>;
    }

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
                    props.isMini),

                // Top layers: Forecast median and measured data
                createTrace(
                    processedData.median,
                    !props.isMini,
                    props.isMini,
                    plotColors.median,
                    'Median',
                ),
                createTrace(
                    processedData.measured!,
                    !props.isMini,
                    props.isMini,
                    plotColors.measured,
                    'Measured',
                )
            ]}
            layout={layout}
            style={{width: '100%', height: '100%'}}
            useResizeHandler={true}
            config={{
                ...commonPlotlyConfig,
                staticPlot: props.isMini
            }}
        />
    );
};
