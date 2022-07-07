package naya.ganj.app.data.mycart.repositry

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyCartRepositry {
    var mycartData = MutableLiveData<MyCartModel>()
    fun getMyCartData(jsonObject: JsonObject): LiveData<MyCartModel> {

        RetrofitClient.instance.getMyCartData(Constant.USER_ID, "ANDROID", jsonObject)
            .enqueue(object : Callback<MyCartModel> {
                override fun onResponse(
                    call: Call<MyCartModel>,
                    response: Response<MyCartModel>
                ) {
                    mycartData.value = response.body()
                }

                override fun onFailure(call: Call<MyCartModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })

        return mycartData
    }
}