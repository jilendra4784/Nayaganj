package naya.ganj.app.data.mycart.model
import com.google.gson.annotations.SerializedName
data class CouponResponseModel(
    @SerializedName("codeName")
    val codeName: String,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("offer")
    val offer: String,
    @SerializedName("promoCodeDiscountAmount")
    val promoCodeDiscountAmount: Double,
    @SerializedName("promoCodeId")
    val promoCodeId: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("updatedCartList")
    val updatedCartList: ArrayList<UpdatedCart>
)