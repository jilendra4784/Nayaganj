package naya.ganj.app.data.home.viewmodel

import androidx.lifecycle.*
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.utility.NetworkResult

class HomeViewModel(val repo: HomeRepositry) : ViewModel() {
    private val homedata = MutableLiveData<NetworkResult<HomePageModel>>()
    private val cateGoryData2 = MutableLiveData<NetworkResult<HomePageModel>>()
    private val cateGoryData3 = MutableLiveData<NetworkResult<HomePageModel>>()
    private val cateGoryData4 = MutableLiveData<NetworkResult<HomePageModel>>()
    private val cateGoryData5 = MutableLiveData<NetworkResult<HomePageModel>>()


    fun getHomeData(userId: String, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    homedata.value = NetworkResult.Success(result.body()!!)
                } else {
                    homedata.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                homedata.value = NetworkResult.Error(e.message)
            }
        }

        return homedata
    }

    fun getHomeDataForIndex2(userId: String, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    cateGoryData2.value = NetworkResult.Success(result.body()!!)
                } else {
                    cateGoryData2.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                cateGoryData2.value = NetworkResult.Error(e.message)
            }
        }

        return cateGoryData2
    }

    fun getHomeDataForIndex3(userId: String, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    cateGoryData3.value = NetworkResult.Success(result.body()!!)
                } else {
                    cateGoryData3.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                cateGoryData3.value = NetworkResult.Error(e.message)
            }
        }

        return cateGoryData3
    }


    fun getHomeDataForIndex4(userId: String, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    cateGoryData4.value = NetworkResult.Success(result.body()!!)
                } else {
                    cateGoryData4.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                cateGoryData4.value = NetworkResult.Error(e.message)
            }
        }

        return cateGoryData4
    }

    fun getHomeDataForIndex5(userId: String, jsonObject: JsonObject): LiveData<NetworkResult<HomePageModel>> {
        viewModelScope.launch {
            try {
                val result = repo.getHomeData(userId, jsonObject)
                if (result.isSuccessful) {
                    cateGoryData5.value = NetworkResult.Success(result.body()!!)
                } else {
                    cateGoryData5.value = NetworkResult.Error(result.message())
                }
            } catch (e: Exception) {
                cateGoryData5.value = NetworkResult.Error(e.message)
            }
        }

        return cateGoryData5
    }

}