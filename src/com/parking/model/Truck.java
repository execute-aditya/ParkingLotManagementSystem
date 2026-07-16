package com.parking.model;

/**
 * Truck — extends Vehicle.
 * Requires a TRUCK-sized spot (largest).
 */
public class Truck extends Vehicle {

    public Truck() {
        setVehicleType(VehicleType.TRUCK);
    }

    public Truck(String licensePlate, String ownerName, String ownerPhone) {
        super(licensePlate, ownerName, ownerPhone);
        setVehicleType(VehicleType.TRUCK);
    }

    @Override
    public String getRequiredSpotType() {
        return "TRUCK";
    }

    @Override
    public String describe() {
        return "Truck [plate=" + getLicensePlate() + ", owner=" + getOwnerName() + "]";
    }
}
