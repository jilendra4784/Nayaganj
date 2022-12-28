package naya.ganj.app.deliverymodule.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.gson.JsonObject
import com.google.maps.android.PolyUtil
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityTrackLocationByDeliveryBoyBinding
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.PermissionUtils
import naya.ganj.app.utility.Utility
import org.json.JSONObject
import java.util.*

class TrackLocationByDeliveryBoy : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityTrackLocationByDeliveryBoyBinding
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var mLocationRequest: LocationRequest? = null
    private var mapFragment: SupportMapFragment? = null
    var mMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private val phoneRequest = 101
    lateinit var app: Nayaganj
    lateinit var viewModel: DeliveryModuleViewModel
    private val REQUEST_CHECK_SETTING = 1001

    var name = ""
    var mobileNumber = ""
    var address = ""
    var orderId = ""
    var userId = ""
    var lat = ""
    var long = ""
    var orderStatus = ""
    private var paymentMode = ""
    var deliveryboylat = ""
    var deliveryboylong = ""
    lateinit var time:TextView
    lateinit var distance:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackLocationByDeliveryBoyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj
        viewModel = ViewModelProvider(this, DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]
        setUIData()
    }

    private fun setUIData() {

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        name = intent.getStringExtra("name").toString()
        mobileNumber = intent.getStringExtra("mobileNumber").toString()
        address = intent.getStringExtra("address").toString()
        orderId = intent.getStringExtra(Constant.orderId).toString()
        lat = intent.getStringExtra(Constant.lat).toString()
        long = intent.getStringExtra(Constant.long).toString()
        orderStatus = intent.getStringExtra(Constant.orderStatus).toString()
        paymentMode = intent.getStringExtra("paymentMode").toString()

        time=findViewById(R.id.time)
        distance=findViewById(R.id.distance)
        binding.customerName.text =   name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1);
        binding.customerAddress.text = address

        if (orderStatus.split("|")[0].equals(Constant.DISPATCHED, ignoreCase = true)) {
            binding.deliverNow.visibility = View.VISIBLE
        } else {
            binding.deliverNow.visibility = View.GONE
        }

        binding.phoneCall.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@TrackLocationByDeliveryBoy,arrayOf(Manifest.permission.CALL_PHONE), phoneRequest)
            } else {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.setData(Uri.parse("tel:+" + mobileNumber))
                startActivityForResult(callIntent, phoneRequest)
            }
        }
        binding.deliverNow.setOnClickListener {
            Log.e("TAG", "setUIData: paymentMode"+paymentMode+", orderStatus"+orderStatus )
            if (paymentMode.equals("COD", ignoreCase = true) && orderStatus.split("|")[0] == Constant.DISPATCHED) {
                deliverProductDialog(orderId, "DELIVERED")
            } else {
                orderStatusDialog(orderId, "DELIVERED")
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun orderStatusDialog(orderId: String, s: String) {

        val builder = AlertDialog.Builder(this)
        builder.setIcon(ContextCompat.getDrawable(this, R.drawable.my_order))

        builder.setTitle("Deliver Order")
        builder.setMessage("Have you reached Customer location & Ready to Deliver Product.")
        builder.setPositiveButton("Yes")
        { _, _ ->
            ChangeStatusApi(orderId, orderStatus)
        }

        builder.setNegativeButton("No") { _, _ -> }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(R.color.gray))
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.dark_gray))

    }

    private fun ChangeStatusApi(orderId: String, orderStatus: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.orderId, orderId)
        jsonObject.addProperty(Constant.orderStatus, orderStatus)
        viewModel.changeOrderStatusAPIRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this) {

                if (it.status) {

                    Toast.makeText(
                        this@TrackLocationByDeliveryBoy,
                        "You have delivered product successfully to the customer",
                        Toast.LENGTH_SHORT
                    ).show()

                    sendlatandlong(deliveryboylat, deliveryboylong)

                    val intent = Intent(
                        this@TrackLocationByDeliveryBoy,
                        DeliveryBoyDashboardActivity::class.java
                    )
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this@TrackLocationByDeliveryBoy, it.msg, Toast.LENGTH_SHORT)
                        .show()
                }
            }


    }

    private fun sendlatandlong(deliveryboylat: String, deliveryboylong: String) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.lat, deliveryboylat)
        jsonObject.addProperty(Constant.long, deliveryboylong)
        viewModel.setDeliveryBoyLocationRequest(app.user.getUserDetails()?.userId,jsonObject).observe(this){
            Log.e("TAG", "sendlatandlong: "+it )
        }

    }

    private fun deliverProductDialog(orderId: String, paymentMode: String) {
        var paymentModeStatus = ""
        val dialog = Dialog(this@TrackLocationByDeliveryBoy)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.deliver_order_payment_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val ivClose = dialog.findViewById(R.id.iv_close) as ImageView
        val edtAmount = dialog.findViewById(R.id.edt_amount) as EditText
        val radioCash = dialog.findViewById(R.id.radioButton_cash) as RadioButton
        val radioPaytm = dialog.findViewById(R.id.radio_button_paytm) as RadioButton
        val btnSubmit = dialog.findViewById(R.id.btn_submit) as Button

        ivClose.setOnClickListener { dialog.dismiss() }


        radioCash.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                radioCash.isChecked = true
                radioPaytm.isChecked = false
                paymentModeStatus = "CASH"
            }
        }

        radioPaytm.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                radioCash.isChecked = false
                radioPaytm.isChecked = true
                paymentModeStatus = "PAYTM"
            }
        }

        btnSubmit.setOnClickListener {
            val amount = edtAmount.text.toString()
            if (amount == "") {
                Utility().showToast(this@TrackLocationByDeliveryBoy, "Please Enter the Amount.")
            } else if (paymentModeStatus == "") {
                Utility().showToast(this@TrackLocationByDeliveryBoy, "Please Select Payment Mode")
            } else {
                dialog.dismiss()

                if(Utility.isAppOnLine(this@TrackLocationByDeliveryBoy,object :
                        OnInternetCheckListener {
                        override fun onInternetAvailable() {
                            deliveryOrderPaymentApi(orderId, amount, paymentModeStatus)
                        }
                    }))
                deliveryOrderPaymentApi(orderId, amount, paymentModeStatus)
            }
        }

        dialog.show()
    }

    private fun deliveryOrderPaymentApi(
        orderId: String,
        amount: String,
        paymentModeStatus: String
    ) {

            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.paymentMode, paymentModeStatus)
            jsonObject.addProperty(Constant.amount, amount)

            viewModel.deliverOrderPaymentRequest(app.user.getUserDetails()?.userId, jsonObject)
                .observe(this) {
                    if (it.status) {
                        Toast.makeText(
                            this@TrackLocationByDeliveryBoy,
                            "You have delivered product successfully to the customer.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(
                            this@TrackLocationByDeliveryBoy,
                            DeliveryBoyDashboardActivity::class.java
                        )
                        startActivity(intent)


                    } else {
                        Toast.makeText(
                            this@TrackLocationByDeliveryBoy,
                            "Order is not delivered!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun setUpLocationListener(){
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = LocationRequest().setInterval(10000).setFastestInterval(10000)
            .setPriority(PRIORITY_HIGH_ACCURACY)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest!!, mLocationCallback, Looper.myLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
    }

    private  var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                try {
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude,
                        location.longitude,
                        1)

                    deliveryboylat=location.latitude.toString()
                    deliveryboylong=location.longitude.toString()

                    val latLngOrigin = LatLng(location.latitude, location.longitude)
                    val latLngDestination = LatLng(lat.toDouble(), long.toDouble())

                    mMap!!.clear()
                    mMap!!.addMarker(
                        MarkerOptions().position(latLngOrigin).title(getCompleteAddressString(location.latitude, location.longitude)).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.pin)))
                    mMap!!.addMarker(
                        MarkerOptions().position(latLngDestination).title(getCompleteAddressString(lat.toDouble(), long.toDouble())).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.green_pin)))

                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14f))

                    mMap!!.uiSettings.isZoomControlsEnabled = true;

                    val path: MutableList<List<LatLng>> = ArrayList()

                    val urlDirections = getDirectionsUrl(latLngOrigin, latLngDestination)

                    val directionsRequest = object : StringRequest(
                        Method.GET,
                        urlDirections,
                        Response.Listener { response ->

                            val jsonResponse = JSONObject(response)
                            // Get routes
                            val routes = jsonResponse.getJSONArray("routes")
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val steps = legs.getJSONObject(0).getJSONArray("steps")
                            val objDistance: JSONObject = legs.getJSONObject(0).getJSONObject("distance")
                            val objDuration: JSONObject = legs.getJSONObject(0).getJSONObject("duration")

                            time.setText("Time remaining: "+objDuration.get("text").toString())
                            distance.setText("Distance: "+objDistance.get("text").toString())

                            for (i in 0 until steps.length()) {
                                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                                path.add(PolyUtil.decode(points))
                            }
                            for (i in 0 until path.size) {
                                mMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                            }
                        },
                        Response.ErrorListener { _ ->
                        }){}
                    val requestQueue = Volley.newRequestQueue(this@TrackLocationByDeliveryBoy)
                    requestQueue.add(directionsRequest)


                } catch (e: Exception) {
                }
            }
        }
    }
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {

        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            } else {
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        val parameters = "$str_origin&$str_dest&$sensor&$mode"
        // Output format
        val output = "json"
        //Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyDfIa5Kw9H2eES-BTnYjSTgJiV54l6sDUU"
    }

    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
                        checkLocationSetting()
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun checkLocationSetting() {

        mLocationRequest = LocationRequest.create()
        mLocationRequest?.apply { priority = PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(applicationContext).checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
                Log.d(TAG, "checkSetting: GPS On")
            } catch (e: ApiException) {

                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@TrackLocationByDeliveryBoy, REQUEST_CHECK_SETTING)
                        Log.d(TAG, "checkSetting: RESOLUTION_REQUIRED")
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // USER DEVICE DOES NOT HAVE LOCATION OPTION
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CHECK_SETTING -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        setUpLocationListener()
                    }
                    Activity.RESULT_CANCELED -> {
                        finish()
                    }
                }
            }
        }

    }


}