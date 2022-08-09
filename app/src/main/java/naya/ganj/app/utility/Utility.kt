package naya.ganj.app.utility

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Utility {

    //Common Function to add or Remove Item
    fun addRemoveItem(
        userId: String?,
        action: String,
        productId: String,
        variantId: String,
        promoCode: String,

        ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.PRODUCT_ID, productId)
        jsonObject.addProperty(Constant.ACTION, action)
        jsonObject.addProperty(Constant.VARIANT_ID, variantId)
        jsonObject.addProperty(Constant.PROMO_CODE, "")

        RetrofitClient.instance.addremoveItemRequest(
            userId,
            Constant.DEVICE_TYPE,
            jsonObject
        )
            .enqueue(object : Callback<AddRemoveModel> {
                override fun onResponse(
                    call: Call<AddRemoveModel>,
                    response: Response<AddRemoveModel>
                ) {
                    Log.e("TAG", "onResponse: " + response.message())
                }

                override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                    Log.e("TAG", "onFailure: " + t.message)
                }
            })
    }

    fun insertProduct(context: Context, productDetail: ProductDetail) {
        AppDataBase.getInstance(context).productDao()
            .insert(productDetail)
    }

    fun updateProduct(context: Context, productID: String, variantId: String, itemCount: Int) {
        AppDataBase.getInstance(context).productDao().updateProduct(itemCount, productID, variantId)
    }

    fun deleteProduct(context: Context, productID: String, variantId: String) {
        AppDataBase.getInstance(context).productDao().deleteProduct(productID, variantId)
    }

    fun isProductAvailable(
        context: Context,
        productID: String,
        variantId: String
    ): Boolean {
        return AppDataBase.getInstance(context).productDao().isProductExist(productID, variantId)
    }

    fun getAllProductList(
        context: Context
    ): List<ProductDetail> {
        return AppDataBase.getInstance(context).productDao().getProductList()
    }

    fun getSingleProduct(context: Context, productID: String, variantId: String): ProductDetail {
        return AppDataBase.getInstance(context).productDao().getSingleProduct(productID, variantId)
    }

    // Amount Round off for Two places
    fun formatTotalAmount(amount: Double): Double {
        return Math.round(amount * 100.0) / 100.0
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun deleteAllProduct(paymentOptionActivity: Activity) {
        AppDataBase.getInstance(paymentOptionActivity).productDao().deleteAllProduct()
    }

    fun checkPermission(context: Context): Boolean {
        var isPermissionGranted = false
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if (report.areAllPermissionsGranted()) {
                        isPermissionGranted = true
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        isPermissionGranted = false
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    isPermissionGranted = false
                    MaterialAlertDialogBuilder(context)
                        .setMessage("Please Enable this permission for your order.")
                        .setPositiveButton("Ok", null)
                        .show()
                }
            }).check()
        return isPermissionGranted
    }

}