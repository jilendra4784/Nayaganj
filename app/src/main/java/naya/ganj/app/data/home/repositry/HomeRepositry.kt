package naya.ganj.app.data.home.repositry

import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant

class HomeRepositry(val api: ApiInterface) {

    suspend fun getBannerData(userId:String?) = api.getBannerData(userId, Constant.DEVICE_TYPE)
}