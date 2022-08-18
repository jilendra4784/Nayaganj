package naya.ganj.app.utility

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import naya.ganj.app.R
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Utility {

    lateinit var dialog: Dialog

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



    companion object {

        fun serverNotResponding(context: Context) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.server_error_dialog)
            dialog.setCancelable(false)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
            val got_it: TextView = dialog.findViewById(R.id.btn_got_it)

            got_it.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        fun isAppOnLine(context: Context): Boolean {
            var isInternetAvailable = false
            if (isNetworkConnected(context)) {
                isInternetAvailable = true
                return isInternetAvailable
            } else {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.no_internet_connection_dialog)
                dialog.setCancelable(false)
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
                val button = dialog.findViewById<Button>(R.id.retry_button)
                button.setOnClickListener {
                    if (isNetworkConnected(context)) {
                        dialog.dismiss()
                        isInternetAvailable = true
                    }
                }
                try {
                    dialog.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return isInternetAvailable
            }
        }

        private fun isNetworkConnected(context: Context): Boolean {

            // register activity with the connectivity manager service
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // if the android version is equal to M
            // or greater we need to use the
            // NetworkCapabilities to check what type of
            // network has the internet connection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Returns a Network object corresponding to
                // the currently active default data network.
                val network = connectivityManager.activeNetwork ?: return false

                // Representation of the capabilities of an active network.
                val activeNetwork =
                    connectivityManager.getNetworkCapabilities(network) ?: return false

                return when {
                    // Indicates this network uses a Wi-Fi transport,
                    // or WiFi has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                    // Indicates this network uses a Cellular transport. or
                    // Cellular has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                    // else return false
                    else -> false
                }
            } else {
                // if the android version is below M
                @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return networkInfo.isConnected
            }
        }


    }

}