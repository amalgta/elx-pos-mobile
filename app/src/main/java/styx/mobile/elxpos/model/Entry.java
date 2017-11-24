package styx.mobile.elxpos.model;

import android.os.Parcel;
import android.os.Parcelable;

import styx.mobile.elxpos.application.Constants;

/**
 * Created by amalg on 16-11-2017.
 */

public class Entry implements Parcelable {
    private String transactionNumber;
    private String registrationNumber;
    private String columnNumber;
    private String amountPaid;
    private String vehicleClass;
    private String paymentMethod;
    private String passType;
    private String lane;

    public Entry(String transactionNumber, String registrationNumber, String columnNumber, String amountPaid, String vehicleClass, String paymentMethod, String passType, String lane) {
        this.transactionNumber = transactionNumber;
        this.registrationNumber = registrationNumber;
        this.columnNumber = columnNumber;
        this.amountPaid = amountPaid;
        this.vehicleClass = vehicleClass;
        this.paymentMethod = paymentMethod;
        this.passType = passType;
        this.lane = lane;
    }

    public Entry() {
    }

    protected Entry(Parcel in) {
        transactionNumber = in.readString();
        registrationNumber = in.readString();
        columnNumber = in.readString();
        amountPaid = in.readString();
        vehicleClass = in.readString();
        paymentMethod = in.readString();
        passType = in.readString();
        lane = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionNumber);
        dest.writeString(registrationNumber);
        dest.writeString(columnNumber);
        dest.writeString(amountPaid);
        dest.writeString(vehicleClass);
        dest.writeString(paymentMethod);
        dest.writeString(passType);
        dest.writeString(lane);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
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
