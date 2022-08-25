package naya.ganj.app.data.mycart.repositry

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyCartRepositry {
    var mycart = MutableLiveData<MyCartModel>()
    fun getMyCartData(context: Context,userId: String, jsonObject: JsonObject): LiveData<MyCartModel> {

        RetrofitClient.instance.getMyCartData(userId, Constant.DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<MyCartModel> {
                override fun onResponse(
                    call: Call<MyCartModel>,
                    response: Response<MyCartModel>
                ) {
                    if(response.isSuccessful){
                        mycart.value = response.body()
                    }else{
                        Utility.showToast(context,response.message())
                    }
                }

                override fun onFailure(call: Call<MyCartModel>, t: Throwable) {
                    Utility.serverNotResponding(context,t.message.toString())
                }
            })

        return mycart
    }



}