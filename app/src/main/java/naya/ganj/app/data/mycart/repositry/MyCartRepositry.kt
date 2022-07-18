package naya.ganj.app.data.mycart.repositry

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyCartRepositry {
    var mycart = MutableLiveData<MyCartModel>()
    fun getMyCartData(userId: String, jsonObject: JsonObject): LiveData<MyCartModel> {

        RetrofitClient.instance.getMyCartData(userId, Constant.DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<MyCartModel> {
                override fun onResponse(
                    call: Call<MyCartModel>,
                    response: Response<MyCartModel>
                ) {
                    mycart.value = response.body()
                }

                override fun onFailure(call: Call<MyCartModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })

        return mycart
    }
}