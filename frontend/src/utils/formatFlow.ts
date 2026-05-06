export function formatFlow(flow: number): string {
    const MAX_FLOW_FOR_DISPLAY_AS_DOUBLE = 30;
    return flow >= MAX_FLOW_FOR_DISPLAY_AS_DOUBLE ? Math.round(flow).toString() : flow.toFixed(1);
}
