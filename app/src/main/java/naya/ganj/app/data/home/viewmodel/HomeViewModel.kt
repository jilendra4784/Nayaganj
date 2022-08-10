package naya.ganj.app.data.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.home.model.BannerModel
import naya.ganj.app.data.home.repositry.HomeRepositry

class HomeViewModel(val repo: HomeRepositry) : ViewModel() {
    private val ballerMutableData = MutableLiveData<BannerModel>()

    fun getBannerList(userId: String?): LiveData<BannerModel> {
        viewModelScope.launch{
           val result= repo.getBannerData(userId)
            ballerMutableData.value=result.body()
        }

        return ballerMutableData
    }
}