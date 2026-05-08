export function formatValue(value: number): string {
    const MAX_VALUE_FOR_DISPLAY_AS_DOUBLE = 30;
    return value >= MAX_VALUE_FOR_DISPLAY_AS_DOUBLE ? Math.round(value).toString() : value.toFixed(1);
}
