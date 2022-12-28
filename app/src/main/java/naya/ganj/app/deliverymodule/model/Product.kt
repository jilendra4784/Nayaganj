package naya.ganj.app.deliverymodule.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("created")
    val created: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("img")
    val img: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("productBrand")
    val productBrand: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("productStatus")
    val productStatus: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("variantId")
    val variantId: String,
    @SerializedName("variantType")
    val variantType: String,
    @SerializedName("variantUnit")
    val variantUnit: String,
    @SerializedName("variantUnitQuantity")
    val variantUnitQuantity: Int
)
