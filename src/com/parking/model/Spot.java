package com.parking.model;

/**
 * POJO representing a single Parking Spot.
 */
public class Spot {

    public enum SpotType { BIKE, CAR, VAN, TRUCK }

    private int      spotId;
    private int      lotId;
    private String   spotNumber;  // e.g. "C-01"
    private SpotType spotType;
    private int      floor;
    private boolean  occupied;

    public Spot() {}

    public Spot(int spotId, int lotId, String spotNumber, SpotType spotType, int floor, boolean occupied) {
        this.spotId     = spotId;
        this.lotId      = lotId;
        this.spotNumber = spotNumber;
        this.spotType   = spotType;
        this.floor      = floor;
        this.occupied   = occupied;
    }

    // Getters & Setters
    public int getSpotId()                  { return spotId; }
    public void setSpotId(int spotId)       { this.spotId = spotId; }

    public int getLotId()                   { return lotId; }
    public void setLotId(int lotId)         { this.lotId = lotId; }

    public String getSpotNumber()           { return spotNumber; }
    public void setSpotNumber(String sn)    { this.spotNumber = sn; }

    public SpotType getSpotType()           { return spotType; }
    public void setSpotType(SpotType st)    { this.spotType = st; }

    public int getFloor()                   { return floor; }
    public void setFloor(int floor)         { this.floor = floor; }

    public boolean isOccupied()             { return occupied; }
    public void setOccupied(boolean occ)    { this.occupied = occ; }

    @Override
    public String toString() {
        return String.format("Spot[id=%d, number='%s', type=%s, floor=%d, occupied=%s]",
                spotId, spotNumber, spotType, floor, occupied ? "YES" : "NO");
    }
}
