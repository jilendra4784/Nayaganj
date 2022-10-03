package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName
import naya.ganj.app.data.mycart.model.UpdatedCart

data class AddRemoveModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("promoCodeDiscountAmount")
    val promoCodeDiscountAmount: Double,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("updatedCartList")
    val updatedCartList: ArrayList<UpdatedCart>
)