package styx.mobile.elxpos.model;

/**
 * Created by amalg on 16-11-2017.
 */

public class Entry {
    private long transactionNumber;
    private long registrationNumber;
    private long columnNumber;
    private float amountPaid;
    private String vehicleClass;
    private String paymentMethod;
    private String passType;
    private String lane;

    public Entry(long transactionNumber, long registrationNumber, long columnNumber, float amountPaid, String vehicleClass, String paymentMethod, String passType, String lane) {
        this.transactionNumber = transactionNumber;
        this.registrationNumber = registrationNumber;
        this.columnNumber = columnNumber;
        this.amountPaid = amountPaid;
        this.vehicleClass = vehicleClass;
        this.paymentMethod = paymentMethod;
        this.passType = passType;
        this.lane = lane;
    }

    public long getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(long transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public long getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(long registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public long getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(long columnNumber) {
        this.columnNumber = columnNumber;
    }

    public float getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(float amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPassType() {
        return passType;
    }

    public void setPassType(String passType) {
        this.passType = passType;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }
}
