package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class CouponModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("promoCodeList")
    val promoCodeList: List<PromoCode>,
    @SerializedName("status")
    val status: Boolean
) {
    data class PromoCode(
        @SerializedName("codeName")
        val codeName: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("details")
        val details: List<String>,
        @SerializedName("enable")
        val enable: Boolean,
        @SerializedName("_id")
        val id: String,
        @SerializedName("max")
        val max: Int,
        @SerializedName("min")
        val min: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("offer")
        val offer: String,
        @SerializedName("percentage")
        val percentage: Int,
        @SerializedName("upto")
        val upto: Int
    )
}