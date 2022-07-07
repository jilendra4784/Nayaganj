package naya.ganj.app.data.category.repositry

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant.DEVICE_TYPE
import naya.ganj.app.utility.Constant.USER_ID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailRepositry {

    var mutableLiveData = MutableLiveData<ProductDetailModel>()

    fun getProductDetailData(jsonObject: JsonObject): LiveData<ProductDetailModel> {

        RetrofitClient.instance.getProductDetail(USER_ID, DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<ProductDetailModel> {
                override fun onResponse(
                    call: Call<ProductDetailModel>,
                    response: Response<ProductDetailModel>
                ) {
                    mutableLiveData.value = response.body()
                }

                override fun onFailure(call: Call<ProductDetailModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })

        return mutableLiveData
    }
}