package com.parking.model;

import java.time.LocalDateTime;

/**
 * POJO representing a Parking Lot.
 */
public class ParkingLot {

    private int    lotId;
    private String lotName;
    private String address;
    private int    totalSpots;
    private LocalDateTime createdAt;

    public ParkingLot() {}

    public ParkingLot(int lotId, String lotName, String address, int totalSpots) {
        this.lotId      = lotId;
        this.lotName    = lotName;
        this.address    = address;
        this.totalSpots = totalSpots;
    }

    // Getters & Setters
    public int getLotId()               { return lotId; }
    public void setLotId(int lotId)     { this.lotId = lotId; }

    public String getLotName()              { return lotName; }
    public void setLotName(String lotName)  { this.lotName = lotName; }

    public String getAddress()              { return address; }
    public void setAddress(String address)  { this.address = address; }

    public int getTotalSpots()              { return totalSpots; }
    public void setTotalSpots(int t)        { this.totalSpots = t; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    @Override
    public String toString() {
        return String.format("ParkingLot[id=%d, name='%s', address='%s', totalSpots=%d]",
                lotId, lotName, address, totalSpots);
    }
}
