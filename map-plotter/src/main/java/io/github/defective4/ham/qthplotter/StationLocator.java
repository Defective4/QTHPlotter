package io.github.defective4.ham.qthplotter;

public class StationLocator {
    private final Maidenhead.Locator locator;
    private final String callsign;
    private final int signal;

    public StationLocator(Maidenhead.Locator locator, String callsign, int signal) {
        this.locator = locator;
        this.callsign = callsign;
        this.signal = signal;
    }

    public Maidenhead.Locator getLocator() {
        return locator;
    }

    public String getCallsign() {
        return callsign;
    }

    public int getSignal() {
        return signal;
    }
}
