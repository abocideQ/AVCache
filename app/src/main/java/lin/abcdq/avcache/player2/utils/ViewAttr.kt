package lin.abcdq.avcache.player2.utils

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class ViewAttr() : Parcelable {

    companion object CREATOR : Creator<ViewAttr> {
        override fun createFromParcel(parcel: Parcel): ViewAttr {
            return ViewAttr(parcel)
        }

        override fun newArray(size: Int): Array<ViewAttr?> {
            return arrayOfNulls(size)
        }
    }

    var x = 0
    var y = 0
    var width = 0
    var height = 0

    constructor(parcel: Parcel) : this() {
        x = parcel.readInt()
        y = parcel.readInt()
        width = parcel.readInt()
        height = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(x)
        dest.writeInt(y)
        dest.writeInt(width)
        dest.writeInt(height)
    }
}