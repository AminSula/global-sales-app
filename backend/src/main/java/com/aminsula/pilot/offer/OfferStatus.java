package com.aminsula.pilot.offer;

public enum OfferStatus {
    /** Counts today. */
    ACTIVE,
    /** Starts on a later day. */
    SCHEDULED,
    /** Its last day has passed. */
    EXPIRED,
    /** Switched off by the administrator. */
    PAUSED
}
