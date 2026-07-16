package com.parking.model;

/**
 * Van — extends Vehicle.
 * Requires a VAN-sized spot.
 */
public class Van extends Vehicle {

    public Van() {
        setVehicleType(VehicleType.VAN);
    }

    public Van(String licensePlate, String ownerName, String ownerPhone) {
        super(licensePlate, ownerName, ownerPhone);
        setVehicleType(VehicleType.VAN);
    }

    @Override
    public String getRequiredSpotType() {
        return "VAN";
    }

    @Override
    public String describe() {
        return "Van [plate=" + getLicensePlate() + ", owner=" + getOwnerName() + "]";
    }
}
