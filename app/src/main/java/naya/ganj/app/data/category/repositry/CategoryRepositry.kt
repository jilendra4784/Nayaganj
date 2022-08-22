package naya.ganj.app.data.category.repositry

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryRepositry {

    private val categoryData = MutableLiveData<CategoryDataModel>()
    fun getCategoryData(context: Context): LiveData<CategoryDataModel> {

        RetrofitClient.instance.getAllCateData().enqueue(object : Callback<CategoryDataModel> {
            override fun onResponse(
                call: Call<CategoryDataModel>,
                response: Response<CategoryDataModel>
            ) {
                if(response.isSuccessful){
                    categoryData.postValue(response.body())
                }else{
                    Utility.showToast(context,response.message())
                }

            }

            override fun onFailure(call: Call<CategoryDataModel>, t: Throwable) {
                Utility.serverNotResponding(context,t.message.toString())
            }
        })
        return categoryData;
    }
}