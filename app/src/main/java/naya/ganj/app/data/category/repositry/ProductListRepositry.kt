package naya.ganj.app.data.category.repositry

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.model.ProductListModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductListRepositry {

    private var productList = MutableLiveData<ProductListModel>()
    private var addremoveStatus = MutableLiveData<AddRemoveModel>()

    fun getProductList(
        context: Context,
        userid: String?,
        d: String,
        jsonObject: JsonObject
    ): LiveData<ProductListModel> {
        RetrofitClient.instance.getProductList(userid, d, jsonObject)
            .enqueue(object : Callback<ProductListModel> {
                override fun onResponse(
                    call: Call<ProductListModel>,
                    response: Response<ProductListModel>
                ) {
                    productList.value = response.body()
                }

                override fun onFailure(call: Call<ProductListModel>, t: Throwable) {
                    Utility.serverNotResponding(context,t.message.toString())
                }
            })

        return productList
    }

    fun addremoveItemRequest(
        jsonObject: JsonObject
    ): LiveData<AddRemoveModel> {

        RetrofitClient.instance.addremoveItemRequest(
            "",
            "ANDROID",
            jsonObject
        )
            .enqueue(object : Callback<AddRemoveModel> {
                override fun onResponse(
                    call: Call<AddRemoveModel>,
                    response: Response<AddRemoveModel>
                ) {
                    addremoveStatus.value = response.body()
                }

                override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })

        return addremoveStatus;
    }


}