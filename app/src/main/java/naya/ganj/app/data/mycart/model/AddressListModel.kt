package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class AddressListModel(
    @SerializedName("addressList")
    val addressList: MutableList<Address>,
    @SerializedName("msg")
    val msg: String
) {
    data class Address(
        @SerializedName("address")
        val address: Address,
        @SerializedName("created")
        val created: String,
        @SerializedName("default")
        val default: Boolean,
        @SerializedName("_id")
        val id: String,
        @SerializedName("lastUpdated")
        val lastUpdated: String,
        @SerializedName("loc")
        val loc: Loc,
        @SerializedName("userId")
        val userId: String
    ) {
        data class Address(
            @SerializedName("ApartName")
            val apartName: String,
            @SerializedName("city")
            val city: String,
            @SerializedName("contactNumber")
            val contactNumber: String,
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("houseNo")
            val houseNo: String,
            @SerializedName("landmark")
            val landmark: String,
            @SerializedName("lastName")
            val lastName: String,
            @SerializedName("nickName")
            val nickName: String,
            @SerializedName("pincode")
            val pincode: String,
            @SerializedName("street")
            val street: String
        )

        data class Loc(
            @SerializedName("coordinates")
            val coordinates: List<Any>,
            @SerializedName("type")
            val type: String
        )
    }
}