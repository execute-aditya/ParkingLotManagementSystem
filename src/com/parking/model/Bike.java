package com.parking.model;

/**
 * Bike — extends Vehicle.
 * Requires a BIKE-sized spot (smallest).
 */
public class Bike extends Vehicle {

    public Bike() {
        setVehicleType(VehicleType.BIKE);
    }

    public Bike(String licensePlate, String ownerName, String ownerPhone) {
        super(licensePlate, ownerName, ownerPhone);
        setVehicleType(VehicleType.BIKE);
    }

    @Override
    public String getRequiredSpotType() {
        return "BIKE";
    }

    @Override
    public String describe() {
        return "Bike [plate=" + getLicensePlate() + ", owner=" + getOwnerName() + "]";
    }
}
