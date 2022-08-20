package naya.ganj.app.data.category.repositry

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant.DEVICE_TYPE
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailRepositry {
    var mutableLiveData = MutableLiveData<ProductDetailModel>()
    fun getProductDetailData(
        context: Context,
        jsonObject: JsonObject
    ): LiveData<ProductDetailModel> {

        RetrofitClient.instance.getProductDetail("", DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<ProductDetailModel> {
                override fun onResponse(
                    call: Call<ProductDetailModel>,
                    response: Response<ProductDetailModel>
                ) {
                    mutableLiveData.value = response.body()
                }

                override fun onFailure(call: Call<ProductDetailModel>, t: Throwable) {
                    Utility.serverNotResponding(context, t.message.toString())
                }
            })

        return mutableLiveData
    }
}