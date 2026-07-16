package com.parking.model;

/**
 * POJO representing a pricing Rate configuration.
 */
public class Rate {

    public enum FareType    { HOURLY, DAILY, MONTHLY }
    public enum VehicleType { BIKE, CAR, VAN, TRUCK }

    private int         rateId;
    private int         lotId;
    private VehicleType vehicleType;
    private FareType    fareType;
    private double      baseRate;         // ₹ per hour / day / month
    private int         gracePeriodMin;   // free minutes before charging
    private double      surgeMultiplier;  // e.g. 1.5 = 50% surge

    public Rate() {}

    public Rate(int lotId, VehicleType vehicleType, FareType fareType,
                double baseRate, int gracePeriodMin, double surgeMultiplier) {
        this.lotId           = lotId;
        this.vehicleType     = vehicleType;
        this.fareType        = fareType;
        this.baseRate        = baseRate;
        this.gracePeriodMin  = gracePeriodMin;
        this.surgeMultiplier = surgeMultiplier;
    }

    // Getters & Setters
    public int getRateId()                          { return rateId; }
    public void setRateId(int rateId)               { this.rateId = rateId; }

    public int getLotId()                           { return lotId; }
    public void setLotId(int lotId)                 { this.lotId = lotId; }

    public VehicleType getVehicleType()             { return vehicleType; }
    public void setVehicleType(VehicleType vt)      { this.vehicleType = vt; }

    public FareType getFareType()                   { return fareType; }
    public void setFareType(FareType fareType)      { this.fareType = fareType; }

    public double getBaseRate()                     { return baseRate; }
    public void setBaseRate(double baseRate)        { this.baseRate = baseRate; }

    public int getGracePeriodMin()                  { return gracePeriodMin; }
    public void setGracePeriodMin(int gp)           { this.gracePeriodMin = gp; }

    public double getSurgeMultiplier()              { return surgeMultiplier; }
    public void setSurgeMultiplier(double sm)       { this.surgeMultiplier = sm; }

    @Override
    public String toString() {
        return String.format(
            "Rate[id=%d, lot=%d, type=%s, fare=%s, base=%.2f, grace=%dmin, surge=%.2f]",
            rateId, lotId, vehicleType, fareType, baseRate, gracePeriodMin, surgeMultiplier);
    }
}
