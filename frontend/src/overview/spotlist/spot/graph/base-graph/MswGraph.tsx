import {SpotModel} from "../../../../../model/SpotModel";
import {ApiMeasurementType} from "../../../../../gen/msw-api-ts";
import {Config, Layout} from 'plotly.js';
// react-plotly.js already bundles this exact module; reuse it so we don't pull a
// second copy of plotly into the bundle.
// @ts-ignore - the dist entry ships without type declarations
import Plotly from 'plotly.js/dist/plotly';
import {useCallback, useRef} from "react";
import {MswTheme} from "../../../../../theme/MswThemeContext";
import {getThemeDependingColors, ThemeDependingColors} from "../../../../../theme/MswThemeHelper";
import {measurementLabel} from "../../../../../helper/ApiMeasurementTypeHelper";

// Structural type covering ApiSample, ApiLineEntry, and ad-hoc {timestamp, value} points.
export type TimeSeriesPoint = { timestamp: string; value: number };

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

// Extract timestamps from a data series
export function getTimestamps(data: TimeSeriesPoint[]): string[] {
    return data.map(item => item.timestamp).sort()
}

// Extract values from a data series
export function getValues(data: TimeSeriesPoint[]): number[] {
    return data.map(item => item.value);
}

// One tick per distinct UTC day, anchored at the given hour:minute (UTC).
export function getTicksAt(hour: number, timestamps: Array<string>, minute: number = 0): Array<string> {
    const seenDays = new Set<string>();
    return timestamps
        .filter(ts => {
            const d = new Date(ts);
            const key = `${d.getUTCFullYear()}-${d.getUTCMonth()}-${d.getUTCDate()}`;
            if (seenDays.has(key)) return false;
            seenDays.add(key);
            return true;
        })
        .map(ts => {
            const d = new Date(ts);
            d.setUTCHours(hour, minute, 0, 0);
            return d.toISOString();
        });
}

// Create a trace for Plotly with common defaults
export function createTrace(
    data: TimeSeriesPoint[],
    showTooltip: boolean,
    isMini: boolean,
    color?: string,
    name?: string,
    measurementType?: ApiMeasurementType) {
    const isMobile = window.innerWidth <= 720;
    return {
        x: getTimestamps(data),
        y: getValues(data),
        type: 'scatter' as const,
        mode: 'lines' as const,
        line: {width: 1, shape: 'spline' as const, color},
        name,
        showlegend: !isMini && !isMobile,
        hoverinfo: showTooltip ? 'all' as const : 'skip' as const,
        hovertemplate: showTooltip ? `%{x|%d.%m.%Y %H:%M}<br>${measurementLabel(measurementType)}: %{y:.1f}<extra></extra>` : undefined,
    };
}

export function createAreaTrace(
    upperData: TimeSeriesPoint[],
    lowerData: TimeSeriesPoint[],
    name: string,
    fillcolor: string,
    isMini: boolean) {
    return [
        {
            ...createTrace(upperData, false, isMini, 'transparent'),
            showlegend: false,
        },
        {
            ...createTrace(lowerData, false, isMini, 'transparent', name),
            fill: 'tonexty',
            fillcolor: fillcolor,
        }
    ];
}

// Plotly config: mini graphs stay fully static, opened graphs allow horizontal
// zoom/pan (scroll/pinch to zoom, drag to pan, double-click to reset to default).
export function getPlotlyConfig(isMini: boolean): Partial<Config> {
    return {
        responsive: true,
        displayModeBar: false,
        staticPlot: isMini,
        scrollZoom: !isMini,
        doubleClick: isMini ? (false as const) : ('reset' as const),
        modeBarButtonsToRemove: ['zoom2d', 'pan2d', 'select2d', 'lasso2d', 'zoomIn2d', 'zoomOut2d', 'autoScale2d', 'resetScale2d']
    };
}

export function getCommonPlotlyLayout(
    isMini: boolean,
    allTimestamps: string[],
    minValue: number,
    maxValue: number,
    showCurrentTimeLine: boolean,
    theme: MswTheme,
    uirevision: string
): Partial<Layout> {
    const themeDependingColors: ThemeDependingColors = getThemeDependingColors(theme);
    return {
        autosize: true,
        paper_bgcolor: 'transparent',
        plot_bgcolor: 'transparent',
        // Preserve the user's zoom/pan across re-renders (data refreshes, theme
        // changes, etc.); resets only when the underlying spot/series changes.
        uirevision: uirevision,
        xaxis: {
            showgrid: false,
            showticklabels: !isMini,
            range: allTimestamps.length ? [allTimestamps[0], allTimestamps[allTimestamps.length - 1]] : undefined,
            // Opened graphs zoom/pan along time only; mini graphs stay fixed.
            // Panning is kept inside the data bounds by useTimeAxisClamp (an
            // onRelayout handler) rather than minallowed/maxallowed, which clamp
            // a single edge and visibly squish/zoom the window.
            fixedrange: isMini,
            spikemode: 'toaxis+across',
            spikethickness: -2,
            spikecolor: `rgba(${themeDependingColors.invertedRgb}, 1)`,
            color: `rgba(${themeDependingColors.invertedRgb}, 1)`,
        },
        yaxis: {
            showticklabels: !isMini,
            // Keep the value scale (and acceptable-range band) stable while the
            // user navigates time horizontally.
            fixedrange: true,
            gridcolor: isMini ? 'transparent' : `rgba(${themeDependingColors.invertedRgb}, 0.3)`,
            ticklabelposition: 'inside' as const,
            tickfont: { // simply using color on yaxis does not work because of ticklabelposition: 'inside'
                color: `rgba(${themeDependingColors.invertedRgb}, 1)`,
            },
        },
        legend: isMini ? undefined : {
            orientation: 'h',
            y: -0.1,
            yanchor: 'top',
            xanchor: 'center',
            x: 0.5,
            itemclick: false,
            itemdoubleclick: false,
        },
        margin: isMini ?
            {l: 5, r: 5, t: 5, b: 5} :
            {l: 30, r: 30, t: 0, b: 30}, // provide space for x-axis labels without legend
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
            ...(minValue !== undefined && maxValue !== undefined && allTimestamps.length > 0 ? [{
                type: 'rect' as const,
                x0: allTimestamps[0],
                x1: allTimestamps[allTimestamps.length - 1],
                y0: minValue,
                y1: maxValue,
                fillcolor: plotColors.acceptableRange.fill,
                line: {width: 0},
                layer: 'below' as const
            }] : []),
        ],
        hoverlabel: isMini ? undefined : {bgcolor: 'white', bordercolor: 'gray', font: {size: 13}},
        hovermode: isMini ? false : 'closest' as const,
        dragmode: isMini ? (false as const) : ('pan' as const),
        font: {
            color: `rgba(${themeDependingColors.invertedRgb}, 1)`
        },
    };
}

// Smallest time window the user can zoom into (prevents the pinch gesture from
// collapsing the range to zero/negative width).
const MIN_ZOOM_WIDTH = ONE_HOUR;

// Slide/shrink [n0, n1] back inside [minMs, maxMs], preserving width when it fits.
function clampToBounds(n0: number, n1: number, minMs: number, maxMs: number): [number, number] {
    const width = n1 - n0;
    if (width >= maxMs - minMs) return [minMs, maxMs]; // wider than data: pin to full extent
    if (n0 < minMs) return [minMs, minMs + width];
    if (n1 > maxMs) return [maxMs - width, maxMs];
    return [n0, n1];
}

// A range value can be a number (ms) or a Plotly date string; normalize to ms.
function toMs(v: any): number {
    return typeof v === 'number' ? v : new Date(v).getTime();
}

// Plotly's cartesian plots zoom only via the mouse wheel (scrollZoom binds to the
// `wheel` event) — there is no native two-finger pinch handler, so touch devices
// can pan but not zoom. This attaches a capture-phase touch handler that zooms the
// x-axis around the pinch midpoint, clamped to [minMs, maxMs]. Capture + preventing
// the default keeps the browser from page-zooming and stops Plotly's single-finger
// pan from firing at the same time. Returns a cleanup function.
function attachPinchZoom(gd: any, boundsRef: {current: {minMs?: number; maxMs?: number}}) {
    let active = false;
    let startDist = 1;
    let startR0 = 0;
    let startR1 = 0;
    let anchorData = 0;
    let midFraction = 0.5;

    const onTouchStart = (e: TouchEvent) => {
        if (e.touches.length !== 2) {
            active = false;
            return;
        }
        const xa = gd._fullLayout && gd._fullLayout.xaxis;
        const {minMs, maxMs} = boundsRef.current;
        if (!xa || minMs === undefined || maxMs === undefined) return;

        const rect = gd.getBoundingClientRect();
        const axisLeft = rect.left + xa._offset;
        const axisWidth = xa._length || 1;

        const x1 = e.touches[0].clientX;
        const x2 = e.touches[1].clientX;
        startDist = Math.max(Math.abs(x1 - x2), 1);
        startR0 = toMs(xa.range[0]);
        startR1 = toMs(xa.range[1]);

        const f1 = (x1 - axisLeft) / axisWidth;
        const f2 = (x2 - axisLeft) / axisWidth;
        midFraction = Math.min(Math.max((f1 + f2) / 2, 0), 1);
        anchorData = startR0 + midFraction * (startR1 - startR0);

        active = true;
        e.preventDefault();
        e.stopPropagation();
    };

    const onTouchMove = (e: TouchEvent) => {
        if (!active || e.touches.length !== 2) return;
        e.preventDefault();
        e.stopPropagation();

        const {minMs, maxMs} = boundsRef.current;
        if (minMs === undefined || maxMs === undefined) return;

        const curDist = Math.max(Math.abs(e.touches[0].clientX - e.touches[1].clientX), 1);
        const scale = startDist / curDist; // fingers spread => smaller window => zoom in
        const fullWidth = maxMs - minMs;
        const newWidth = Math.min(Math.max((startR1 - startR0) * scale, MIN_ZOOM_WIDTH), fullWidth);

        const n0 = anchorData - midFraction * newWidth;
        const [c0, c1] = clampToBounds(n0, n0 + newWidth, minMs, maxMs);
        Plotly.relayout(gd, {'xaxis.range[0]': c0, 'xaxis.range[1]': c1});
    };

    const onTouchEnd = (e: TouchEvent) => {
        if (e.touches.length < 2) active = false;
    };

    const opts = {passive: false, capture: true} as const;
    gd.addEventListener('touchstart', onTouchStart, opts);
    gd.addEventListener('touchmove', onTouchMove, opts);
    gd.addEventListener('touchend', onTouchEnd, opts);

    return () => {
        gd.removeEventListener('touchstart', onTouchStart, opts);
        gd.removeEventListener('touchmove', onTouchMove, opts);
        gd.removeEventListener('touchend', onTouchEnd, opts);
    };
}

// Keeps horizontal pan/zoom within [minMs, maxMs]. When the user drags past an
// edge, the whole window is slid back inside the bounds with its width preserved
// (instead of pinning one edge, which would shrink/zoom the view). Also wires up
// two-finger pinch-to-zoom on touch devices (unless the graph is static/mini).
// Returns Plot event handlers to spread onto the <Plot> element.
export function useTimeAxisClamp(minMs: number | undefined, maxMs: number | undefined, enablePinchZoom = true) {
    const graphDivRef = useRef<any>(null);
    const cleanupRef = useRef<(() => void) | null>(null);
    // Pinch listeners are attached once on init but must see the latest bounds.
    const boundsRef = useRef<{minMs?: number; maxMs?: number}>({minMs, maxMs});
    boundsRef.current = {minMs, maxMs};

    const onInitialized = useCallback((_figure: any, graphDiv: any) => {
        graphDivRef.current = graphDiv;
        if (cleanupRef.current) cleanupRef.current();
        cleanupRef.current = enablePinchZoom ? attachPinchZoom(graphDiv, boundsRef) : null;
    }, [enablePinchZoom]);

    const onRelayout = useCallback((eventData: any) => {
        const gd = graphDivRef.current;
        const {minMs, maxMs} = boundsRef.current;
        if (!gd || minMs === undefined || maxMs === undefined) return;

        const raw0 = eventData['xaxis.range[0]'];
        const raw1 = eventData['xaxis.range[1]'];
        // Ignore events without an explicit x-range (e.g. autorange / reset).
        if (raw0 === undefined || raw1 === undefined) return;

        const r0 = new Date(raw0).getTime();
        const r1 = new Date(raw1).getTime();
        const [n0, n1] = clampToBounds(r0, r1, minMs, maxMs);

        const TOLERANCE = 1000; // ms; prevents a relayout feedback loop on rounding
        if (Math.abs(n0 - r0) > TOLERANCE || Math.abs(n1 - r1) > TOLERANCE) {
            Plotly.relayout(gd, {'xaxis.range[0]': n0, 'xaxis.range[1]': n1});
        }
    }, []);

    return {onInitialized, onRelayout};
}
