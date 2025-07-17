import {ApiFlowSample, ApiForecast, ApiHistoricalYears, ApiLineEntry, ApiSample} from "../gen/msw-api-ts";
import {DateTime} from "luxon";

export class DateTimeConverter {
    public static utcForecastToLocalTime(forecast: ApiForecast | undefined): ApiForecast | undefined {
        if (forecast === undefined) {
            return undefined;
        }
        return {
            ...forecast,
            measuredData: this.utcApiLineEntryArrayToLocalTime(forecast.measuredData),
            median: this.utcApiLineEntryArrayToLocalTime(forecast.median),
            twentyFivePercentile: this.utcApiLineEntryArrayToLocalTime(forecast.twentyFivePercentile),
            seventyFivePercentile: this.utcApiLineEntryArrayToLocalTime(forecast.seventyFivePercentile),
            min: this.utcApiLineEntryArrayToLocalTime(forecast.min),
            max: this.utcApiLineEntryArrayToLocalTime(forecast.max)
        }
    }

    public static utcLast40DaysToLocalTime(last40Days: Array<ApiFlowSample> | undefined): Array<ApiFlowSample> | undefined {
        if (last40Days === undefined || last40Days === null || last40Days.length === 0) {
            return last40Days;
        }
        return this.utcApiFlowSampleArrayToLocalTime(last40Days);
    }

    public static utcHistoricalToLocalTime(historical: ApiHistoricalYears | undefined): ApiHistoricalYears | undefined {
        if (historical === undefined || historical === null) {
            return historical;
        }
        return {
            ...historical,
            median: this.utcApiLineEntryArrayToLocalTime(historical.median),
            twentyFivePercentile: this.utcApiLineEntryArrayToLocalTime(historical.twentyFivePercentile),
            seventyFivePercentile: this.utcApiLineEntryArrayToLocalTime(historical.seventyFivePercentile),
            min: this.utcApiLineEntryArrayToLocalTime(historical.min),
            max: this.utcApiLineEntryArrayToLocalTime(historical.max),
            currentYear: this.utcApiLineEntryArrayToLocalTime(historical.currentYear),
        };
    }

    public static utcApiSampleToLocalTime(sample: ApiSample) {
        return {
            ...sample,
            timestamp: this.utcToLocalString(sample.timestamp)
        }
    }

    private static utcApiLineEntryArrayToLocalTime(apiLineEntries: Array<ApiLineEntry>): Array<ApiLineEntry> {
        return apiLineEntries.map(d => this.utcApiLineEntryToLocalTime(d)).sort()
    }

    private static utcApiLineEntryToLocalTime(apiLineEntry: ApiLineEntry): ApiLineEntry {
        return {
            ...apiLineEntry,
            timestamp: this.utcToLocalString(apiLineEntry.timestamp)
        }
    }

    private static utcApiFlowSampleArrayToLocalTime(apiFlowSamples: Array<ApiFlowSample>): Array<ApiFlowSample> {
        return apiFlowSamples.map(d => this.utcApiFlowSampleToLocalTime(d)).sort();
    }

    private static utcApiFlowSampleToLocalTime(apiFlowSample: ApiFlowSample): ApiFlowSample {
        return {
            ...apiFlowSample,
            timestamp: this.utcToLocalString(apiFlowSample.timestamp),
        }
    }

    private static utcToLocalString(timestamp: string): string {
        if (!timestamp.endsWith('Z')) {
            // Looks like this is not utc, so we don't change anything
            return timestamp;
        }
        return DateTime.fromISO(timestamp, {zone: 'utc'}).toLocal().toString();
    }
}
