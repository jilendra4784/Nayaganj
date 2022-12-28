package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.sidemenu.model.MyWalletModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.utility.NetworkResult

class MyWalletViewModel(val repositry: SideMenuDataRepositry) : ViewModel() {
    val walletMutableLiveData = MutableLiveData<NetworkResult<MyWalletModel>>()

    fun getWalletBalance(userId: String?): LiveData<NetworkResult<MyWalletModel>> {
        viewModelScope.launch {
            val result = repositry.getWalletBalance(userId)
            if (result.isSuccessful) {
                walletMutableLiveData.value = NetworkResult.Success(result.body()!!)
            } else {
                walletMutableLiveData.value = NetworkResult.Error(result.message())
            }
        }
       return walletMutableLiveData
    }

}