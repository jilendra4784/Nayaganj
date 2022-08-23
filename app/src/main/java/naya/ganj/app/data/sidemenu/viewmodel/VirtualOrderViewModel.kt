package naya.ganj.app.data.sidemenu.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.model.VirtualOrderModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility

class VirtualOrderViewModel(val repo : SideMenuDataRepositry): ViewModel() {
    private val mutableLiveData = MutableLiveData<NetworkResult<VirtualOrderModel>>()

    fun getMyVirtualOrderList(context: Context,userid: String?): LiveData<NetworkResult<VirtualOrderModel>> {
        viewModelScope.launch {
            val result = repo.getMyVirtualOrderList(userid)
           try {
               if(result.isSuccessful){
                   mutableLiveData.value = NetworkResult.Success(result.body()!!)
               }else{
                   Utility.serverNotResponding(context,result.message().toString())
               }
           }catch (e: Exception){
               Utility.serverNotResponding(context,e.message.toString())
           }



        }
        return mutableLiveData
    }


}