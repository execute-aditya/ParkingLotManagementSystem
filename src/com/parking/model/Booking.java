package com.parking.model;

import java.time.LocalDateTime;

/**
 * POJO representing a parking Booking.
 */
public class Booking {

    public enum BookingStatus { ACTIVE, COMPLETED, CANCELLED }
    public enum FareType      { HOURLY, DAILY, MONTHLY }

    private int           bookingId;
    private int           vehicleId;
    private int           spotId;
    private int           lotId;
    private FareType      fareType;
    private BookingStatus status;
    private LocalDateTime createdAt;

    // Transient (not stored but loaded for display)
    private String  licensePlate;
    private String  spotNumber;

    public Booking() {}

    public Booking(int vehicleId, int spotId, int lotId, FareType fareType) {
        this.vehicleId = vehicleId;
        this.spotId    = spotId;
        this.lotId     = lotId;
        this.fareType  = fareType;
        this.status    = BookingStatus.ACTIVE;
    }

    // Getters & Setters
    public int getBookingId()                   { return bookingId; }
    public void setBookingId(int bookingId)     { this.bookingId = bookingId; }

    public int getVehicleId()                   { return vehicleId; }
    public void setVehicleId(int vehicleId)     { this.vehicleId = vehicleId; }

    public int getSpotId()                      { return spotId; }
    public void setSpotId(int spotId)           { this.spotId = spotId; }

    public int getLotId()                       { return lotId; }
    public void setLotId(int lotId)             { this.lotId = lotId; }

    public FareType getFareType()               { return fareType; }
    public void setFareType(FareType fareType)  { this.fareType = fareType; }

    public BookingStatus getStatus()            { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime c)   { this.createdAt = c; }

    public String getLicensePlate()             { return licensePlate; }
    public void setLicensePlate(String lp)      { this.licensePlate = lp; }

    public String getSpotNumber()               { return spotNumber; }
    public void setSpotNumber(String sn)        { this.spotNumber = sn; }

    @Override
    public String toString() {
        return String.format("Booking[id=%d, vehicle=%d, spot=%d, fare=%s, status=%s, created=%s]",
                bookingId, vehicleId, spotId, fareType, status, createdAt);
    }
}
