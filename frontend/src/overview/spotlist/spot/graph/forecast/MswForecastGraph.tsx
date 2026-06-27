import '../base-graph/MswGraph.scss'
import Plot from 'react-plotly.js';
import {ApiForecast, ApiLineEntry, ApiSample} from '../../../../../gen/msw-api-ts';
import {
    createAreaTrace,
    createTrace,
    getCommonPlotlyLayout,
    getPlotlyConfig,
    getTicksAt,
    getTimestamps,
    MswGraphProps,
    ONE_DAY,
    plotColors,
    TimeSeriesPoint,
    useTimeAxisClamp
} from "../base-graph/MswGraph";
import {MswLoader} from "../../../../../loader/MswLoader";
import {useMemo} from "react";
import {useTheme} from "../../../../../theme/MswThemeContext";

interface MswForecastGraphProps extends MswGraphProps {
    forecast: ApiForecast | undefined;
    loaded: boolean;
    lastFewDays?: Array<ApiSample>;
    lastFewDaysLoaded?: boolean;
}

export const MswForecastGraph = (props: MswForecastGraphProps) => {
    const {theme} = useTheme();

    // Get data for plotting
    const {currentSample} = props.spot ?? {};
    const {minValue, maxValue} = props.spot ?? {};
    const {measuredData, median, twentyFivePercentile, seventyFivePercentile, max, min} = props.forecast ?? {};

    const currentTime = currentSample?.timestamp;

    const useHistory = !props.isMini && (props.lastFewDays?.length ?? 0) > 0;
    const pastMeasured: TimeSeriesPoint[] = useHistory
        ? props.lastFewDays!.filter(s => !currentTime || s.timestamp <= currentTime)
        : (measuredData ?? []);

    // Get timestamps for x-axis grid and labels
    const allTimestamps = getTimestamps([...pastMeasured, ...median ?? []]);

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
        measured: pastMeasured,
        median: removeSamplesBeforeCurrentTime(median ?? []),
        min: removeSamplesBeforeCurrentTime(min ?? []),
        max: removeSamplesBeforeCurrentTime(max ?? []),
        p25: removeSamplesBeforeCurrentTime(twentyFivePercentile ?? []),
        p75: removeSamplesBeforeCurrentTime(seventyFivePercentile ?? [])
    };

    // Get common layout and extend it with forecast-specific settings
    let midDayTicks = getTicksAt(12, allTimestamps);
    let startOfDayTicks = getTicksAt(0, allTimestamps)
    const uirevision = `${props.spot.stationId.externalId}-${props.spot.measurementType}`;
    const firstMs = allTimestamps.length ? Date.parse(allTimestamps[0]) : undefined;
    const lastMs = allTimestamps.length ? Date.parse(allTimestamps[allTimestamps.length - 1]) : undefined;
    const currentMs = currentTime ? Date.parse(currentTime) : undefined;
    // With history available, default to one day before the current moment, then
    // the forecast; older history stays reachable by panning. Without history
    // (mini preview, or logged-out users who can't fetch it) keep the original
    // full-extent default range.
    const defaultXRange = (useHistory && currentMs !== undefined && lastMs !== undefined)
        ? [currentMs - ONE_DAY, lastMs]
        : undefined;
    const clampHandlers = useTimeAxisClamp(firstMs, lastMs, !props.isMini);
    const layout = useMemo(() => {
        let baseLayout = getCommonPlotlyLayout(props.isMini, allTimestamps, minValue, maxValue, true, theme, uirevision);

        return {
            ...baseLayout,
            xaxis: {
                ...baseLayout.xaxis,
                // Default view: one day before current → end of forecast.
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
        minValue,
        maxValue,
        theme,
        uirevision,
        currentMs,
        lastMs
    ]);

    if (!props.loaded || (!props.isMini && !props.lastFewDaysLoaded)) {
        return <MswLoader/>;
    }
    if (!props.forecast) {
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
                    props.spot.measurementType,
                ),
                createTrace(
                    processedData.measured!,
                    !props.isMini,
                    props.isMini,
                    plotColors.measured,
                    'Measured',
                    props.spot.measurementType,
                )
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
