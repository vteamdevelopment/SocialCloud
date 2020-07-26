package com.vteam.testdemo.landing.model

import android.os.Parcel
import android.os.Parcelable

data class UserStatus(
    var status: String? = null,
    var date: String? = null,
    var time: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor() : this(null) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(status)
        parcel.writeString(date)
        parcel.writeString(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserStatus> {
        override fun createFromParcel(parcel: Parcel): UserStatus {
            return UserStatus(parcel)
        }

        override fun newArray(size: Int): Array<UserStatus?> {
            return arrayOfNulls(size)
        }
    }
}