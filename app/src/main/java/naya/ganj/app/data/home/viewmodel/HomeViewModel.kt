package naya.ganj.app.data.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.utility.NetworkResult

class HomeViewModel(val repo: HomeRepositry) : ViewModel() {
    private val ballerMutableData = MutableLiveData<NetworkResult<HomePageModel>>()

    fun getHomeData(userId: String?, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    ballerMutableData.value = NetworkResult.Success(result.body()!!)
                } else {
                    ballerMutableData.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                ballerMutableData.value = NetworkResult.Error(e.message)
            }
        }

        return ballerMutableData
    }
}