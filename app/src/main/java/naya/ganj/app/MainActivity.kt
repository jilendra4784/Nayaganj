package naya.ganj.app


import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.view.LoginActivity
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.data.sidemenu.view.*
import naya.ganj.app.databinding.ActivityMainBinding
import naya.ganj.app.databinding.BottomAudioDialogBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.retrofit.URLConstant
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var orderID: String? = null
    var notificationsBadge: View? = null
    lateinit var navController: NavController
    lateinit var app: Nayaganj
    lateinit var imageUri: Uri
    lateinit var viewModel: MainActivityViewModel
    lateinit var rvAddressList: RecyclerView
    var addressId = ""
    private var virtualCaptureImge = ""
    private var virtualCameraAlertDialog: AlertDialog? = null
    var bottomSheetAddressDialog: BottomSheetDialog? = null

    // AudioRecording
    var bottomSheetAudioRecorder: BottomSheetDialog? = null
    lateinit var aBinding: BottomAudioDialogBinding
    lateinit var countDownTimer: CountDownTimer

    lateinit var mediaRecorder: MediaRecorder
    var mediaPlayer: MediaPlayer? = null
    var second = -1
    var minute: Int = 0
    var hour: Int = 0
    var filePath: String? = null
    var audioFile: String? = null
    var isImageOrderRequest = false
    private var doubleBackToExitPressedOnce = false

    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
            ?.childFragmentManager
            ?.fragments
            ?.first()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[MainActivityViewModel::class.java]

        setToolBar()

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.navigation_home -> {

                    applyTransition()

                    if (app.user.getAppLanguage() == 1) {
                        binding.include14.textView2.text = resources.getString(R.string.nayaganj_h)
                    } else {
                        binding.include14.textView2.text = "Nayaganj"
                    }

                    if(app.user.getLoginSession()){
                        binding.include14.ivCameraIcon.visibility = View.VISIBLE
                    }else{
                        binding.include14.ivCameraIcon.visibility = View.GONE
                    }
                    binding.include14.imageView9.visibility = View.VISIBLE
                    binding.include14.ivUserImageview.visibility = View.VISIBLE
                }
                R.id.navigation_dashboard -> {
                    if (app.user.getAppLanguage() == 1) {
                        binding.include14.textView2.text =
                            resources.getString(R.string.shopby_category_h)
                    } else {
                        binding.include14.textView2.text = "Shop By Category"
                    }


                    binding.include14.imageView9.visibility = View.GONE
                    binding.include14.ivCameraIcon.visibility = View.GONE
                    binding.include14.ivUserImageview.visibility = View.GONE
                }
                R.id.navigation_notifications -> {}
                R.id.navigation_search -> {
                    startActivity(Intent(this@MainActivity, ProductListActivity::class.java))
                }
                R.id.navigation_mycart -> {
                    val intent = Intent(this@MainActivity, MyCartActivity::class.java)
                    intent.putExtra("ORDER_ID", orderID)
                    startActivity(intent)

                }
            }
        }
        binding.sideNavigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    isDrawerIsOpen()
                    moveToHomeFragment()
                }
                R.id.myaccount -> {
                    startActivity(Intent(this@MainActivity, MyAccountActivity::class.java))
                }
                R.id.shop_category -> {
                    isDrawerIsOpen()
                    moveToDashboard()
                }

                R.id.my_order -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, MyOrderActivity::class.java))
                }
                R.id.myvirtualorder -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, MyVirtualActivity::class.java))
                }
                R.id.all_offer -> {
                    showMessage(item.title.toString())
                }
                R.id.share_App -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                    )
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                }
                R.id.refer_earn -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity,ReferAndEarnActivity::class.java))
                }
                R.id.customer_support -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, CustomerSupportActivity::class.java))
                }
                R.id.about_us -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, AboutUsActivity::class.java))
                }
                R.id.privacy_policy -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                }
                R.id.logout -> {
                    isDrawerIsOpen()
                    showLogoutDialog()
                }

            }
            true
        }

        binding.sideNavigation.getHeaderView(0).setOnClickListener {
            if (!app.user.getLoginSession()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
        }

        binding.navView.setOnItemReselectedListener { }
        binding.include14.llSearchLayout.setOnClickListener {
            startActivity(Intent(this@MainActivity, ProductListActivity::class.java))
        }


    }

    private fun applyTransition() {
        currentNavigationFragment?.apply {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        doubleBackToExitPressedOnce = false

        setUIDataForLoginUser()
        setBadgeCount()
        LocationService.getInstance().startLocationUpdate(this@MainActivity, binding.include14.tvLocation)
        applyTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationService.getInstance().stopLocation()
    }

    private fun setUIDataForLoginUser() {

        if(app.user.getAppLanguage()==1){
            binding.include14.textView2.text=resources.getString(R.string.nayaganj_h)
            // Todo Bottom Menu Title in Hindi
            binding.navView.menu.findItem(R.id.navigation_home).title=resources.getString(R.string.home_h)
            binding.navView.menu.findItem(R.id.navigation_dashboard).title=resources.getString(R.string.category_h)
            binding.navView.menu.findItem(R.id.navigation_search).title=resources.getString(R.string.search_h)
            binding.navView.menu.findItem(R.id.navigation_notifications).title=resources.getString(R.string.all_offers_h)
            binding.navView.menu.findItem(R.id.navigation_mycart).title=resources.getString(R.string.my_cart_h)

            // Todo Hide Side Menu Item in Hindi
            binding.sideNavigation.menu.findItem(R.id.home).title=resources.getString(R.string.home_h)
            binding.sideNavigation.menu.findItem(R.id.myaccount).title=resources.getString(R.string.my_account_h)
            binding.sideNavigation.menu.findItem(R.id.shop_category).title=resources.getString(R.string.category_h)
            binding.sideNavigation.menu.findItem(R.id.my_order).title=resources.getString(R.string.myorders_h)
            binding.sideNavigation.menu.findItem(R.id.myvirtualorder).title=resources.getString(R.string.my_virtual_order_hindi)
            binding.sideNavigation.menu.findItem(R.id.all_offer).title=resources.getString(R.string.all_offers_h)
            binding.sideNavigation.menu.findItem(R.id.share_App).title=resources.getString(R.string.share_h)
            binding.sideNavigation.menu.findItem(R.id.refer_earn).title=resources.getString(R.string.refer_and_earn_h)
            binding.sideNavigation.menu.findItem(R.id.customer_support).title=resources.getString(R.string.csupport_h)
            binding.sideNavigation.menu.findItem(R.id.about_us).title=resources.getString(R.string.about_us_h)
            binding.sideNavigation.menu.findItem(R.id.privacy_policy).title=resources.getString(R.string.privacy_policy_h)
            binding.sideNavigation.menu.findItem(R.id.logout).title=resources.getString(R.string.logout_h)
        }

        // Todo Hide Side Menu Item
        binding.sideNavigation.menu.findItem(R.id.myaccount).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.my_order).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.myvirtualorder).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.refer_earn).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.logout).isVisible = app.user.getLoginSession()

        val userName = binding.sideNavigation.getHeaderView(0).findViewById(R.id.tv_user_name) as TextView
        val mobileNo =
            binding.sideNavigation.getHeaderView(0).findViewById(R.id.tv_mobile) as TextView
        val llLoginSignUp = binding.sideNavigation.getHeaderView(0)
            .findViewById(R.id.ll_login_signup_layout) as LinearLayout
        val llUserInfoLayout = binding.sideNavigation.getHeaderView(0)
            .findViewById(R.id.ll_user_info_layout) as LinearLayout


        if (app.user.getLoginSession()) {
            if (app.user.getUserDetails()?.name.equals("")) {
                userName.text = resources.getString(R.string.user_name)
            } else {
                userName.text = app.user.getUserDetails()?.name
            }
            mobileNo.text = app.user.getUserDetails()?.mNumber
            llUserInfoLayout.visibility = View.VISIBLE
            llLoginSignUp.visibility = View.GONE

            // Creating File for Capture Order Request

            binding.include14.ivCameraIcon.visibility = View.VISIBLE
            try {
                val dateFormat = SimpleDateFormat("mmddyyyyhhmmss", Locale.US)
                val date: String = dateFormat.format(Date())
                audioFile = "REC$date"
                filePath = "${externalCacheDir?.absolutePath}/" + audioFile

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            llLoginSignUp.visibility = View.VISIBLE
            binding.include14.ivCameraIcon.visibility = View.GONE
        }
    }


    private fun moveToDashboard() {
        Log.e(TAG, "moveToDashboard: ")
        if (navController.currentDestination?.id != R.id.navigation_dashboard) {
            if (navController.currentDestination?.id == R.id.navigation_home) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_home_to_navigation_dashboard)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_dashboard)
            }
        }

    }

    private fun moveToHomeFragment() {
        Log.e(TAG, "moveToHomeFragment: ")
        if (navController.currentDestination?.id != R.id.navigation_home) {
            if (navController.currentDestination?.id == R.id.navigation_notifications) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_home)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_dashboard_to_navigation_home)
            }
        }
    }

    private fun showLogoutDialog() {
        var title: String
        var message: String
        var yes: String
        var no: String
        if(app.user.getAppLanguage()==1){
            title=resources.getString(R.string.logout_h)
            message=resources.getString(R.string.logout_text_h)
            yes=resources.getString(R.string.yes_h)
            no=resources.getString(R.string.no_h)
        }else{
            title="LOGOUT"
            message=" Are you sure, you want to logout?"
            yes="YES"
            no="CANCEL"
        }
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                yes
            ) { dialogInterface, i ->
                isDrawerIsOpen()
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDataBase.getInstance(this@MainActivity).productDao().deleteAllProduct()
                }
                URLConstant.BaseImageUrl=app.user.getUserDetails()?.configObj?.productImgUrl.toString()
                app.user.clearSharedPreference()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialogInterface.dismiss()
                finish()
            }
            .setNegativeButton(
                no
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

    private fun setBadgeCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<ProductDetail> = Utility().getAllProductList(this@MainActivity)
            withContext(Dispatchers.Main) {
                addBadge(list.size.toString())
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun isDrawerIsOpen() {
        if (binding.drawerlayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerlayout.closeDrawer(Gravity.LEFT); //CLOSE Nav Drawer!
        } else {
            binding.drawerlayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
        }
    }

    private fun addBadge(count: String) {
        val mbottomNavigationMenuView = binding.navView.getChildAt(0) as BottomNavigationMenuView
        notificationsBadge = LayoutInflater.from(this).inflate(
            R.layout.custom_badge_layout,
            mbottomNavigationMenuView, false
        )
        val textView = notificationsBadge?.findViewById<TextView>(R.id.badge_count)
        if (textView != null) {
            textView.text = count
        }
        binding.navView.addView(notificationsBadge)
    }

     public fun updateCartValue(value:String){
         addBadge(value)
     }

    private fun setToolBar() {

        binding.include14.ivHumbergerIcon.setOnClickListener { isDrawerIsOpen() }
        binding.include14.ivCameraIcon.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.virtual_order_dialog, null)
            dialogBuilder.setView(dialogView)

            val tvCameraRequest = dialogView.findViewById(R.id.tv_camera_request) as TextView
            val tvVoiceRequest = dialogView.findViewById(R.id.tv_voice_record_request) as TextView
            val ivClose = dialogView.findViewById(R.id.imageView13) as ImageView
            ivClose.setOnClickListener { virtualCameraAlertDialog?.dismiss() }
            tvCameraRequest.setOnClickListener {
                //openCamera()
                if (Utility().checkPermission(this@MainActivity)) {
                    takePictureFromCamera()
                }
            }
            tvVoiceRequest.setOnClickListener {//open Voice Recorder
                if (Utility().checkPermission(this@MainActivity)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        showAudioDialog()
                    }
                }
            }
            virtualCameraAlertDialog = dialogBuilder.create()
            virtualCameraAlertDialog!!.show()
        }
        binding.include14.ivUserImageview.setOnClickListener {
            if (app.user.getLoginSession()) {
                startActivity(Intent(this@MainActivity, MyAccountActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showAudioDialog() {
        bottomSheetAudioRecorder = BottomSheetDialog(this, R.style.BottomSheetDialog)
        aBinding = BottomAudioDialogBinding.inflate(layoutInflater)
        bottomSheetAudioRecorder?.setContentView(aBinding.root)
        bottomSheetAudioRecorder?.show()

        aBinding.ivClose.setOnClickListener {
            bottomSheetAudioRecorder?.dismiss()
            try {
                countDownTimer.cancel()
                mediaRecorder.release()
                aBinding.tvAudioTimer.text = "00:00:00"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        aBinding.ivStartRecording.setOnClickListener {
            aBinding.ivStartRecording.visibility = View.GONE
            aBinding.ivPlayRecording.visibility = View.GONE
            aBinding.ivPauseRecording.visibility = View.VISIBLE
            aBinding.tvRecordtitle.text = "Pause"

            startRecording()
        }

        aBinding.ivPauseRecording.setOnClickListener {
            aBinding.ivPauseRecording.visibility = View.GONE
            aBinding.ivPlayRecording.visibility = View.VISIBLE
            aBinding.tvRecordtitle.text = "Play"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pauseRecording()
            }
        }

        aBinding.ivPlayRecording.setOnClickListener {
            aBinding.ivPlayRecording.visibility = View.GONE
            aBinding.ivPauseRecording.visibility = View.VISIBLE
            aBinding.tvRecordtitle.text = "Pause"
            mediaRecorder.resume()

            countDownTimer.start()
        }

        aBinding.ivStop.setOnClickListener {
            aBinding.ivPlayRecording.visibility = View.GONE
            aBinding.ivPauseRecording.visibility = View.GONE
            aBinding.ivStartRecording.visibility = View.VISIBLE
            aBinding.tvRecordtitle.text = "Record Audio"

            stopRecording()
            playRecording()
        }

        aBinding.ivProceed.setOnClickListener {
            isImageOrderRequest = false
            stopRecording()
            val videoBytes: ByteArray
            try {
                val baos = ByteArrayOutputStream()
                val fis = FileInputStream(File(filePath))
                val buf = ByteArray(1024)
                while (-1 != fis.read(buf)) {
                    baos.write(buf)
                }
                videoBytes = baos.toByteArray()
                virtualCaptureImge = Base64.encodeToString(videoBytes, Base64.DEFAULT)
                Log.d("capturedImage", "" + virtualCaptureImge)

            } catch (e: java.lang.Exception) {
            }
            isImageOrderRequest = false
            checkAddressExist()

        }
    }

    private fun startRecording() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e(TAG, "timer running ")
                second++
                aBinding.tvAudioTimer.text = recorderTime()
            }

            override fun onFinish() {

            }
        }
        countDownTimer.start()

        initializeMediaRecorder()

    }


    private fun initializeMediaRecorder() {
        Log.e("TAG", "initializeMediaRecorder: " + filePath)
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("", "prepare() failed")
            }
            start()

        }
    }

    private fun releaseMediaRecorder() {
        try {
            mediaRecorder.apply {
                stop()
                release()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        countDownTimer.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause()
        }
    }

    private fun playRecording() {
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.setOnCompletionListener({ stopPlaying() })
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: IOException) {
            Log.e(
                "" + ":playRecording()",
                "prepare() failed"
            )
        }

    }

    private fun stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun stopRecording() {
        countDownTimer.cancel()
        second = -1
        minute = 0
        hour = 0

        try {
            countDownTimer.cancel()
            second = -1
            minute = 0
            hour = 0
            aBinding.tvAudioTimer.text = "00:00:00"

            //creating content resolver and put the values
            val values = ContentValues()
            values.put(MediaStore.Audio.Media.DATA, filePath)
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
            values.put(MediaStore.Audio.Media.TITLE, audioFile)
            //store audio recorder file in the external content uri
            contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

            releaseMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    fun recorderTime(): String {
        if (second == 60) {
            minute++
            second = 0
        }
        if (minute == 60) {
            hour++
            minute = 0
        }
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }


    private fun takePictureFromCamera() {
        imageUri = createImageFile()!!
        cameraResult.launch(imageUri)
    }

    private fun createImageFile(): Uri? {
        val image = File(applicationContext.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(
            applicationContext, "com.example.android.fileprovider",
            image
        )
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {

        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    var cameraResult = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        Log.i("nayaganj", ": capturing image...")
        if (it) {
            val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val converetdImage = selectedImage?.let { getResizedBitmap(it, 500) }
            val baos1 = ByteArrayOutputStream()
            converetdImage!!.compress(Bitmap.CompressFormat.JPEG, 80, baos1)
            val imageByteArray2 = baos1.toByteArray()
            virtualCaptureImge = Base64.encodeToString(imageByteArray2, Base64.DEFAULT)

            isImageOrderRequest = true
            checkAddressExist()
        } else {
            Log.i("nayaganj", ": " + it)
            virtualCameraAlertDialog?.dismiss()
            Toast.makeText(this@MainActivity, "Image is not Captured!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkAddressExist() {

        RetrofitClient.instance.getAddressListForMain(
            app.user.getUserDetails()?.userId,
            Constant.DEVICE_TYPE
        )
            .enqueue(object : Callback<AddressListModel> {
                override fun onResponse(
                    call: Call<AddressListModel>,
                    response: Response<AddressListModel>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.addressList?.size!! > 0) {
                            showBottomSheetDialog()
                            rvAddressList.adapter =
                                OrderByImageVirtualAdapter(
                                    response.body()?.addressList!!,
                                    object : OnitemClickListener {
                                        override fun onclick(position: Int, data: String) {
                                            Log.i("nayaganj", "onclick: Address Selected $data")
                                            addressId = data
                                        }
                                    })

                        } else {
                            showAddAddressDialog()
                        }
                    }
                }

                override fun onFailure(call: Call<AddressListModel>, t: Throwable) {

                }
            })
    }

    private fun showBottomSheetDialog() {

        bottomSheetAddressDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetAddressDialog?.setContentView(R.layout.bottom_sheet_address)
        val dismissBottom = bottomSheetAddressDialog?.findViewById<ImageView>(R.id.iv_close)
        val btnAddAddress = bottomSheetAddressDialog?.findViewById<Button>(R.id.btn_add_address)
        val btnPlaceOrder = bottomSheetAddressDialog?.findViewById<Button>(R.id.btn_place_order)

        rvAddressList = bottomSheetAddressDialog?.findViewById<RecyclerView>(R.id.rv_address_list)!!
        rvAddressList.layoutManager = LinearLayoutManager(this@MainActivity)

        bottomSheetAddressDialog?.setCancelable(false)
        bottomSheetAddressDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dismissBottom?.setOnClickListener { bottomSheetAddressDialog?.dismiss() }
        btnAddAddress?.setOnClickListener {
            // Add Address Dialog
            showAddAddressDialog()
        }
        btnPlaceOrder!!.setOnClickListener {
            Log.i("nayaganj", "placing order: ")
            if (addressId != "") {
                if(Utility.isAppOnLine(this@MainActivity,object : OnInternetCheckListener{
                        override fun onInternetAvailable() {
                            placeOrderApi(addressId, bottomSheetAddressDialog!!)
                        }
                    }))
                    placeOrderApi(addressId, bottomSheetAddressDialog!!)
            } else {
                Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetAddressDialog?.show()
    }

    private fun showAddAddressDialog() {
        val ad = BottomSheetDialog(this)
        ad.setContentView(R.layout.virtual_add_address)
        val dismissAddAddress = ad.findViewById<ImageView>(R.id.iv_close)
        val addAddressRequest = ad.findViewById<Button>(R.id.btn_add_address)

        ad.setCancelable(false)
        ad.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dismissAddAddress?.setOnClickListener { ad.dismiss() }
        ad.show()

        val firstName = ad.findViewById<TextInputLayout>(R.id.tv_first_name) as TextInputLayout
        val lastname = ad.findViewById<TextInputLayout>(R.id.tv_last_name) as TextInputLayout
        val mobile = ad.findViewById<TextInputLayout>(R.id.tv_mobile) as TextInputLayout
        val house = ad.findViewById<TextInputLayout>(R.id.tv_house) as TextInputLayout
        val apart = ad.findViewById<TextInputLayout>(R.id.tv_apart) as TextInputLayout
        val street = ad.findViewById<TextInputLayout>(R.id.tv_street) as TextInputLayout
        val address = ad.findViewById<TextInputLayout>(R.id.tv_address) as TextInputLayout
        val city = ad.findViewById<TextInputLayout>(R.id.tv_city) as TextInputLayout
        val pin = ad.findViewById<TextInputLayout>(R.id.tv_pin) as TextInputLayout
        val autoComplte =
            ad.findViewById<TextInputLayout>(R.id.autoCompleteTextView) as AutoCompleteTextView

        val addressList =
            listOf("Home(7 AM - 9 PM delivery)", "Office/Commercial(10 AM - 6 PM delivery)")
        val addressAdapter =
            ArrayAdapter(this@MainActivity, R.layout.auto_list_item, addressList)
        autoComplte.setAdapter(addressAdapter)

        addAddressRequest?.setOnClickListener {

            if (firstName.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please Enter First Name")
            } else if (lastname.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please Enter Last Name")
            } else if (mobile.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please Enter Mobile Number")
            } else if (mobile.editText?.text.toString().startsWith("0")) {
                Utility().showToast(this@MainActivity, "Please enter a valid mobile number")
            } else if (mobile.editText?.text.toString().length < 10) {
                Utility().showToast(this@MainActivity, "Please enter a valid mobile number")
            } else if (house.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please enter house number")
            } else if (apart.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please enter apartment name")
            } else if (street.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please enter street name")
            } else if (address.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please enter area details")
            } else if (city.editText?.text.toString().equals("")) {
                Utility().showToast(this@MainActivity, "Please select city")
            } else if (pin.equals("")) {
                Utility().showToast(this@MainActivity, "Please enter pincode")
            } else if (pin.editText?.text.toString().startsWith("0")) {
                Utility().showToast(this@MainActivity, "Invalid pincode")
            } else if (pin.editText?.text.toString().length < 6) {
                Utility().showToast(this@MainActivity, "Invalid pincode")
            } else if (autoComplte.text.toString().equals("", ignoreCase = true)) {
                Utility().showToast(this@MainActivity, "Please select address type")
            } else {
                val jsonObject = JsonObject()
                jsonObject.addProperty(Constant.firstName, firstName.editText?.text.toString())
                jsonObject.addProperty(Constant.LastName, lastname.editText?.text.toString())
                jsonObject.addProperty(Constant.contactNumber, mobile.editText?.text.toString())
                jsonObject.addProperty(Constant.houseNo, house.editText?.text.toString())
                jsonObject.addProperty(Constant.ApartName, apart.editText?.text.toString())
                jsonObject.addProperty(Constant.street, street.editText?.text.toString())
                jsonObject.addProperty(Constant.landmark, address.editText?.text.toString())
                jsonObject.addProperty(Constant.city, city.editText?.text.toString())
                jsonObject.addProperty(Constant.pincode, pin.editText?.text.toString())
                jsonObject.addProperty(Constant.nickName, autoComplte.text.toString())
                jsonObject.addProperty(Constant.lat, "")
                jsonObject.addProperty(Constant.long, "")
                addAddressRequestAPI(jsonObject, ad)
            }
        }
    }

    private fun addAddressRequestAPI(jsonObject: JsonObject, aDialog: BottomSheetDialog) {

        RetrofitClient.instance.AddAddressForMain(
            app.user.getUserDetails()?.userId,
            Constant.DEVICE_TYPE,
            jsonObject
        )
            .enqueue(object : Callback<AddressListModel> {
                override fun onResponse(
                    call: Call<AddressListModel>,
                    response: Response<AddressListModel>
                ) {
                    if (response.isSuccessful) {
                        aDialog.dismiss()
                        bottomSheetAddressDialog?.dismiss()
                        checkAddressExist()

                        Toast.makeText(this@MainActivity, response.body()!!.msg, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<AddressListModel>, t: Throwable) {
                }
            })
    }

    private fun placeOrderApi(
        addressId: String,
        bottomSheetDialog: BottomSheetDialog
    ) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.data, virtualCaptureImge)
        jsonObject.addProperty(Constant.addressId, addressId)

        if (isImageOrderRequest) {
            jsonObject.addProperty(Constant.format, Constant.jpg)
        } else {
            jsonObject.addProperty(Constant.format, Constant.Mp3)
        }

        viewModel.placeVirtualOrderRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(
                this
            ) {
                if (it.status) {
                    Log.i("TAG", "place order success: ")
                    try {
                        bottomSheetDialog.dismiss()
                        bottomSheetAddressDialog?.dismiss()
                        bottomSheetAudioRecorder?.dismiss()
                        virtualCameraAlertDialog?.dismiss()
                    } catch (e: Exception) {
                        this.addressId = ""
                        this.virtualCaptureImge = ""
                        e.printStackTrace()
                    }
                    Toast.makeText(
                        this@MainActivity,
                        resources.getString(R.string.order_placed),
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Log.i("TAG", "place order fail: " + it.msg.toString())
                }
            }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.exit_press_back_twice_message, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
