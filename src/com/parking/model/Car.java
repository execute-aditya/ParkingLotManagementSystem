package com.parking.model;

/**
 * Car — extends Vehicle.
 * Requires a CAR-sized spot.
 */
public class Car extends Vehicle {

    public Car() {
        setVehicleType(VehicleType.CAR);
    }

    public Car(String licensePlate, String ownerName, String ownerPhone) {
        super(licensePlate, ownerName, ownerPhone);
        setVehicleType(VehicleType.CAR);
    }

    @Override
    public String getRequiredSpotType() {
        return "CAR";
    }

    @Override
    public String describe() {
        return "Car [plate=" + getLicensePlate() + ", owner=" + getOwnerName() + "]";
    }
}
