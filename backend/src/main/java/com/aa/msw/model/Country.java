package com.aa.msw.model;

/**
 * ISO 3166-1 alpha-2 country codes of the countries our own providers cover.
 * The country of a station is a plain string (any ISO code); these constants only exist
 * so the code of our providers does not scatter string literals.
 */
public final class Country {
    public static final String CH = "CH";
    public static final String FR = "FR";
    public static final String DE = "DE";

    private Country() {
    }
}
