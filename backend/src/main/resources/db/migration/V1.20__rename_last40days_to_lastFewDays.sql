ALTER TABLE last_40_days_samples_table
    RENAME TO last_few_days_samples_table;

ALTER TABLE last_few_days_samples_table
    RENAME COLUMN last40DaysSamples TO lastFewDaysSamples;
