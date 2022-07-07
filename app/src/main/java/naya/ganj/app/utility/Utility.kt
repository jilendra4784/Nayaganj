package naya.ganj.app.utility

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Utility {

    //Common Function to add or Remove Item
    fun addRemoveItem(
        action: String,
        productId: String,
        variantId: String,
        promoCode: String,
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.PRODUCT_ID, productId)
        jsonObject.addProperty(Constant.ACTION, action)
        jsonObject.addProperty(Constant.VARIANT_ID, variantId)
        jsonObject.addProperty(Constant.PROMO_CODE, "")

        RetrofitClient.instance.addremoveItemRequest(Constant.USER_ID, "Android", jsonObject)
            .enqueue(object : Callback<AddRemoveModel> {
                override fun onResponse(
                    call: Call<AddRemoveModel>,
                    response: Response<AddRemoveModel>
                ) {
                    Log.e("TAG", "onResponse: " + response.message())
                }

                override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })
    }

    fun insertProduct(context: Context, productID: String, variantId: String, amount: Double) {
        AppDataBase.getInstance(context).productDao()
            .insert(ProductDetail(productID, variantId.toInt(), amount))
    }

    fun updateProduct(context: Context, productID: String, variantId: String, amount: Double) {
        AppDataBase.getInstance(context).productDao().updateProduct(productID, variantId, amount)
    }

    fun deleteProduct(context: Context, productID: String, variantId: String) {
        AppDataBase.getInstance(context).productDao().deleteProduct(productID, variantId)
    }

    fun isProductAvailable(
        context: Context,
        productID: String,
        variantId: String
    ): Boolean {
        return AppDataBase.getInstance(context).productDao().isProductExist(productID, variantId)
    }

    fun getAllProductList(
        context: Context
    ): List<ProductDetail> {
        return AppDataBase.getInstance(context).productDao().getProductList()
    }

    fun getSingleProduct(context: Context, productID: String, variantId: String): ProductDetail {
        return AppDataBase.getInstance(context).productDao().getSingleProduct(productID, variantId)
    }

    // Amount Round off for Two places
    fun formatTotalAmount(amount: Double): Double {
        return Math.round(amount * 100.0) / 100.0
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun deleteAllProduct(paymentOptionActivity: Activity) {
        AppDataBase.getInstance(paymentOptionActivity).productDao().deleteAllProduct()
    }

    fun hideAppBar() {

    }

    fun showAppBar() {

    }

}