package naya.ganj.app.deliverymodule.repositry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel

class DeliveryModuleFactory(val repo: DeliveryModuleRepositry) :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryModuleViewModel::class.java)) {
            return DeliveryModuleViewModel(this.repo) as T
        }else {
            throw IllegalArgumentException("ViewModel Not Found")
        }

    }
}