package naya.ganj.app.data.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.home.model.BannerModel
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.utility.NetworkResult

class HomeViewModel(val repo: HomeRepositry) : ViewModel() {
    private val ballerMutableData = MutableLiveData<NetworkResult<BannerModel>>()

    fun getBannerList(userId: String?): LiveData<NetworkResult<BannerModel>> {
        viewModelScope.launch {
            try{
                val result = repo.getBannerData(userId)
                if (result.isSuccessful) {
                    ballerMutableData.value = NetworkResult.Success(result.body()!!)
                } else {
                    ballerMutableData.value = NetworkResult.Error(result.message())
                }
            }catch (e:Exception){
                ballerMutableData.value = NetworkResult.Error(e.message)
            }
        }

        return ballerMutableData
    }
}