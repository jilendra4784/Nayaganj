package naya.ganj.app.data.category.repositry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryRepositry {

    private val categoryData = MutableLiveData<CategoryDataModel>()
    fun getCategoryData(): LiveData<CategoryDataModel> {

        RetrofitClient.instance.getAllCateData().enqueue(object : Callback<CategoryDataModel> {
            override fun onResponse(
                call: Call<CategoryDataModel>,
                response: Response<CategoryDataModel>
            ) {
                categoryData.postValue(response.body())
            }

            override fun onFailure(call: Call<CategoryDataModel>, t: Throwable) {

            }
        })
        return categoryData;
    }
}