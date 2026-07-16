package com.parking.model;

import java.time.LocalDateTime;

/**
 * POJO representing a Payment record.
 */
public class Payment {

    public enum PaymentMode   { CASH, CARD, UPI }
    public enum PaymentStatus { PENDING, PAID, REFUNDED }

    private int           paymentId;
    private int           bookingId;
    private int           vehicleId;
    private double        baseAmount;
    private double        surgeAmount;
    private double        discountAmount;
    private double        totalAmount;
    private PaymentMode   paymentMode;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(int bookingId, int vehicleId, double baseAmount,
                   double surgeAmount, double discountAmount,
                   double totalAmount, PaymentMode mode) {
        this.bookingId      = bookingId;
        this.vehicleId      = vehicleId;
        this.baseAmount     = baseAmount;
        this.surgeAmount    = surgeAmount;
        this.discountAmount = discountAmount;
        this.totalAmount    = totalAmount;
        this.paymentMode    = mode;
        this.paymentStatus  = PaymentStatus.PENDING;
    }

    // Getters & Setters
    public int getPaymentId()                       { return paymentId; }
    public void setPaymentId(int paymentId)         { this.paymentId = paymentId; }

    public int getBookingId()                       { return bookingId; }
    public void setBookingId(int bookingId)         { this.bookingId = bookingId; }

    public int getVehicleId()                       { return vehicleId; }
    public void setVehicleId(int vehicleId)         { this.vehicleId = vehicleId; }

    public double getBaseAmount()                   { return baseAmount; }
    public void setBaseAmount(double baseAmount)    { this.baseAmount = baseAmount; }

    public double getSurgeAmount()                  { return surgeAmount; }
    public void setSurgeAmount(double surgeAmount)  { this.surgeAmount = surgeAmount; }

    public double getDiscountAmount()               { return discountAmount; }
    public void setDiscountAmount(double d)         { this.discountAmount = d; }

    public double getTotalAmount()                  { return totalAmount; }
    public void setTotalAmount(double totalAmount)  { this.totalAmount = totalAmount; }

    public PaymentMode getPaymentMode()             { return paymentMode; }
    public void setPaymentMode(PaymentMode pm)      { this.paymentMode = pm; }

    public PaymentStatus getPaymentStatus()         { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus ps)  { this.paymentStatus = ps; }

    public LocalDateTime getPaidAt()                { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt)     { this.paidAt = paidAt; }

    @Override
    public String toString() {
        return String.format(
            "Payment[id=%d, booking=%d, base=%.2f, surge=%.2f, disc=%.2f, total=%.2f, mode=%s, status=%s]",
            paymentId, bookingId, baseAmount, surgeAmount, discountAmount, totalAmount, paymentMode, paymentStatus);
    }
}
