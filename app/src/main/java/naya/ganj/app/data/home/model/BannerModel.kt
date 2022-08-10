package naya.ganj.app.data.home.model


import com.google.gson.annotations.SerializedName

data class BannerModel(
    @SerializedName("itemsInCart")
    val itemsInCart: Int,
    @SerializedName("promoBannerList")
    val promoBannerList: List<PromoBanner>,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("totalAmountAfterDis")
    val totalAmountAfterDis: Double
) {
    data class PromoBanner(
        @SerializedName("_id")
        val id: String,
        @SerializedName("img")
        val img: String
    )
}