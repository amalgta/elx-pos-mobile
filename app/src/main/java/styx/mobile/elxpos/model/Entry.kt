package styx.mobile.elxpos.model

import android.os.Parcel
import android.os.Parcelable

import styx.mobile.elxpos.application.Constants

/**
 * Created by amalg on 16-11-2017.
 */

class Entry : Parcelable {

    var transactionNumber: String = ""
    var registrationNumber: String = ""
    var columnNumber: String = ""
    var amountPaid: String = ""
    var vehicleClass: String = ""
    var paymentMethod: String = ""
    var passType: String = ""
    var lane: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var operator: String = ""

    constructor()

    constructor(parcel: Parcel) : this() {
        transactionNumber = parcel.readString()
        registrationNumber = parcel.readString()
        columnNumber = parcel.readString()
        amountPaid = parcel.readString()
        vehicleClass = parcel.readString()
        paymentMethod = parcel.readString()
        passType = parcel.readString()
        lane = parcel.readString()
        startTime = parcel.readString()
        endTime = parcel.readString()
        operator = parcel.readString()
    }

    constructor(transactionNumber: String, registrationNumber: String, columnNumber: String, amountPaid: String, vehicleClass: String, paymentMethod: String, passType: String, lane: String, startTime: String, endTime: String, operatorName: String) {
        this.transactionNumber = transactionNumber
        this.registrationNumber = registrationNumber
        this.columnNumber = columnNumber
        this.amountPaid = amountPaid
        this.vehicleClass = vehicleClass
        this.paymentMethod = paymentMethod
        this.passType = passType
        this.lane = lane
        this.startTime = startTime
        this.endTime = endTime
        this.operator = operatorName
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transactionNumber)
        parcel.writeString(registrationNumber)
        parcel.writeString(columnNumber)
        parcel.writeString(amountPaid)
        parcel.writeString(vehicleClass)
        parcel.writeString(paymentMethod)
        parcel.writeString(passType)
        parcel.writeString(lane)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(operator)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Entry> {
        override fun createFromParcel(parcel: Parcel): Entry {
            return Entry(parcel)
        }

        override fun newArray(size: Int): Array<Entry?> {
            return arrayOfNulls(size)
        }
    }
}
