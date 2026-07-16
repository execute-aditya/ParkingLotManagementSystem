package com.parking.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for all vehicle types.
 * Demonstrates INHERITANCE — Car, Bike, Truck, Van extend this.
 */
public abstract class Vehicle {

    public enum VehicleType { BIKE, CAR, VAN, TRUCK }

    private int         vehicleId;
    private String      licensePlate;
    private VehicleType vehicleType;
    private String      ownerName;
    private String      ownerPhone;
    private boolean     monthlyPass;
    private LocalDateTime passExpiry;
    private LocalDateTime registeredAt;

    public Vehicle() {}

    public Vehicle(String licensePlate, String ownerName, String ownerPhone) {
        this.licensePlate = licensePlate;
        this.ownerName    = ownerName;
        this.ownerPhone   = ownerPhone;
    }

    /**
     * Each subclass declares what size of spot it requires.
     * Demonstrates POLYMORPHISM.
     */
    public abstract String getRequiredSpotType();

    /**
     * Returns a friendly description of the vehicle.
     */
    public abstract String describe();

    // ── Getters & Setters ──────────────────────────────────────

    public int getVehicleId()                   { return vehicleId; }
    public void setVehicleId(int vehicleId)     { this.vehicleId = vehicleId; }

    public String getLicensePlate()             { return licensePlate; }
    public void setLicensePlate(String lp)      { this.licensePlate = lp; }

    public VehicleType getVehicleType()         { return vehicleType; }
    public void setVehicleType(VehicleType vt)  { this.vehicleType = vt; }

    public String getOwnerName()                { return ownerName; }
    public void setOwnerName(String ownerName)  { this.ownerName = ownerName; }

    public String getOwnerPhone()               { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone){ this.ownerPhone = ownerPhone; }

    public boolean isMonthlyPass()              { return monthlyPass; }
    public void setMonthlyPass(boolean mp)      { this.monthlyPass = mp; }

    public LocalDateTime getPassExpiry()        { return passExpiry; }
    public void setPassExpiry(LocalDateTime pe) { this.passExpiry = pe; }

    public LocalDateTime getRegisteredAt()      { return registeredAt; }
    public void setRegisteredAt(LocalDateTime r){ this.registeredAt = r; }

    @Override
    public String toString() {
        return String.format("Vehicle[id=%d, plate='%s', type=%s, owner='%s', pass=%s]",
                vehicleId, licensePlate, vehicleType, ownerName, monthlyPass ? "YES" : "NO");
    }
}
