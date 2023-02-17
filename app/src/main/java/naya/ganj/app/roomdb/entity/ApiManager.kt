package naya.ganj.app.roomdb.entity

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiManager {
    companion object {
        fun updateProductCart(
            context: Context,
            app: Nayaganj,
            action: String,
            productDetail: ProductDetail,
            addremoveText: TextView
        ) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)

            jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
            jsonObject.addProperty(Constant.PROMO_CODE, "")

            if (action == Constant.INSERT) {
                jsonObject.addProperty(Constant.ACTION, "add")
            } else {
                jsonObject.addProperty(Constant.ACTION, action)
            }

            RetrofitClient.instance.addremoveItemRequest(
                app.user.getUserDetails()?.userId, Constant.DEVICE_TYPE, jsonObject
            ).enqueue(object : Callback<AddRemoveModel> {
                override fun onResponse(
                    call: Call<AddRemoveModel>, response: Response<AddRemoveModel>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status) addremoveText.isEnabled = true
                        when (action) {
                            Constant.INSERT -> {
                                LocalDBManager.insertItemInLocal( context, productDetail)
                            }
                            Constant.ADD -> {
                                LocalDBManager.updateProduct( context, productDetail)
                            }

                            Constant.REMOVE -> {
                                if (productDetail.itemQuantity > 0) {
                                    LocalDBManager.updateProduct( context, productDetail)
                                } else {
                                    LocalDBManager.deleteProduct( context, productDetail)
                                }
                            }

                        }

                    } else {
                        Utility.serverNotResponding(
                            context, response.message()
                        )
                    }
                }

                override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                    Utility.serverNotResponding(
                        context, t.message.toString()
                    )
                }
            })
        }
    }

}