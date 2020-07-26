package com.vteam.testdemo.landing.model

import android.os.Parcel
import android.os.Parcelable

data class Users(
    var uId: String? = null,
    var name: String? = null,
    var image: String? = null,
    var status: String? = null,
    var userStatus: UserStatus? = null,
    var isSelected : Boolean? = false
) :Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(UserStatus::class.java.classLoader),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    ) {
    }

    constructor(uid: String) : this(uId = uid) {
        this.uId = uid
    }

    constructor() : this(null) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uId)
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(status)
        parcel.writeParcelable(userStatus, flags)
        parcel.writeValue(isSelected)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Users> {
        override fun createFromParcel(parcel: Parcel): Users {
            return Users(parcel)
        }

        override fun newArray(size: Int): Array<Users?> {
            return arrayOfNulls(size)
        }
    }
}