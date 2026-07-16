package com.parking.service;

import com.parking.model.Rate;

/**
 * ════════════════════════════════════════════════════════════════
 *  POLYMORPHISM DEMONSTRATION — Fare Calculation Strategy
 * ════════════════════════════════════════════════════════════════
 *
 * Abstract base class for all fare strategies.
 * Subclasses implement calculateFare() differently:
 *   HourlyFare  → charges per hour with grace period
 *   DailyFare   → capped at daily rate
 *   MonthlyPass → flat monthly fee, no per-entry charge
 */
public abstract class FareCalculator {

    protected Rate rate;

    public FareCalculator(Rate rate) {
        this.rate = rate;
    }

    /**
     * Calculate fare (in ₹) for a given parking duration in minutes.
     * @param durationMinutes  total minutes parked
     * @param surgeMultiplier  dynamic pricing surge factor (1.0 = no surge)
     * @param hasMonthlyPass   true if vehicle has valid monthly pass
     * @return calculated amount
     */
    public abstract double calculateFare(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass);

    /**
     * Returns a printable breakdown of how the fare was computed.
     */
    public abstract String getFareBreakdown(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass);

    /**
     * Factory method — returns the correct FareCalculator based on fare type.
     */
    public static FareCalculator of(Rate rate) {
        switch (rate.getFareType()) {
            case HOURLY:  return new HourlyFare(rate);
            case DAILY:   return new DailyFare(rate);
            case MONTHLY: return new MonthlyPassFare(rate);
            default:      return new HourlyFare(rate);
        }
    }

    // ── Inner classes (concrete strategies) ──────────────────────

    /**
     * HOURLY fare: ceil(minutes / 60) × hourly rate × surge
     * Grace period: first N minutes are free.
     */
    public static class HourlyFare extends FareCalculator {
        public HourlyFare(Rate rate) { super(rate); }

        @Override
        public double calculateFare(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            if (hasMonthlyPass) return 0.0;                        // monthly pass → free

            long billableMinutes = Math.max(0, durationMinutes - rate.getGracePeriodMin());
            if (billableMinutes == 0) return 0.0;

            // Ceiling to next hour
            double hours = Math.ceil(billableMinutes / 60.0);
            double base  = hours * rate.getBaseRate();
            return Math.round(base * surgeMultiplier * 100.0) / 100.0;
        }

        @Override
        public String getFareBreakdown(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            if (hasMonthlyPass) return "  MONTHLY PASS — No charge";
            long billable = Math.max(0, durationMinutes - rate.getGracePeriodMin());
            double hours  = Math.ceil(billable / 60.0);
            double base   = hours * rate.getBaseRate();
            double total  = Math.round(base * surgeMultiplier * 100.0) / 100.0;
            return String.format(
                "  Duration      : %d min\n" +
                "  Grace period  : %d min (free)\n" +
                "  Billable      : %d min → %.0f hour(s)\n" +
                "  Hourly rate   : ₹%.2f\n" +
                "  Base fare     : ₹%.2f\n" +
                "  Surge (×%.2f) : ₹%.2f\n" +
                "  TOTAL         : ₹%.2f",
                durationMinutes, rate.getGracePeriodMin(),
                billable, hours,
                rate.getBaseRate(),
                base,
                surgeMultiplier, (base * surgeMultiplier - base),
                total);
        }
    }

    /**
     * DAILY fare: min(days × daily_rate, actual days × daily_rate) × surge.
     * Each started day is charged fully.
     */
    public static class DailyFare extends FareCalculator {
        public DailyFare(Rate rate) { super(rate); }

        @Override
        public double calculateFare(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            if (hasMonthlyPass) return 0.0;

            long billableMinutes = Math.max(0, durationMinutes - rate.getGracePeriodMin());
            if (billableMinutes == 0) return 0.0;

            double days = Math.ceil(billableMinutes / (60.0 * 24));
            double base = days * rate.getBaseRate();
            return Math.round(base * surgeMultiplier * 100.0) / 100.0;
        }

        @Override
        public String getFareBreakdown(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            if (hasMonthlyPass) return "  MONTHLY PASS — No charge";
            long billable = Math.max(0, durationMinutes - rate.getGracePeriodMin());
            double days   = Math.ceil(billable / (60.0 * 24));
            double base   = days * rate.getBaseRate();
            double total  = Math.round(base * surgeMultiplier * 100.0) / 100.0;
            return String.format(
                "  Duration      : %d min\n" +
                "  Grace period  : %d min (free)\n" +
                "  Billable      : %d min → %.0f day(s)\n" +
                "  Daily rate    : ₹%.2f\n" +
                "  Base fare     : ₹%.2f\n" +
                "  Surge (×%.2f) : ₹%.2f\n" +
                "  TOTAL         : ₹%.2f",
                durationMinutes, rate.getGracePeriodMin(),
                billable, days,
                rate.getBaseRate(),
                base,
                surgeMultiplier, (base * surgeMultiplier - base),
                total);
        }
    }

    /**
     * MONTHLY PASS fare: flat monthly charge, no per-use charge.
     */
    public static class MonthlyPassFare extends FareCalculator {
        public MonthlyPassFare(Rate rate) { super(rate); }

        @Override
        public double calculateFare(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            // Monthly pass purchase is a flat fee; per-exit is always 0
            return hasMonthlyPass ? 0.0 : rate.getBaseRate(); // first purchase charges flat fee
        }

        @Override
        public String getFareBreakdown(long durationMinutes, double surgeMultiplier, boolean hasMonthlyPass) {
            if (hasMonthlyPass) {
                return "  MONTHLY PASS ACTIVE — Entry/Exit is FREE";
            }
            return String.format(
                "  Monthly Pass Purchase\n" +
                "  Monthly rate  : ₹%.2f\n" +
                "  TOTAL         : ₹%.2f",
                rate.getBaseRate(), rate.getBaseRate());
        }
    }
}
