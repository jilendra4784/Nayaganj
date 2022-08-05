package naya.ganj.app


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
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
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.view.LoginActivity
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.data.sidemenu.view.*
import naya.ganj.app.databinding.ActivityMainBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream


open class MainActivity : AppCompatActivity(), OnitemClickListener {
    private lateinit var binding: ActivityMainBinding
    var orderID: String? = null
    var notificationsBadge: View? = null
    lateinit var navController: NavController
    lateinit var app: Nayaganj
    lateinit var imageUri: Uri
    lateinit var viewModel: MainActivityViewModel

    var virtualCameraAlertDialog: AlertDialog? = null


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
                R.id.navigation_home -> {}
                R.id.navigation_dashboard -> {}
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
                    startActivity(Intent(this@MainActivity,MyAccountActivity::class.java))
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
                    /* val sendIntent = Intent()
                     sendIntent.action = Intent.ACTION_SEND
                     sendIntent.putExtra(Intent.EXTRA_TEXT,
                         "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                     sendIntent.type = "text/plain"
                     startActivity(sendIntent)*/
                }
                R.id.refer_earn -> {
                    showMessage(item.title.toString())
                }
                R.id.customer_support -> {
                    startActivity(Intent(this@MainActivity, CustomerSupportActivity::class.java))
                }
                R.id.about_us -> {
                    startActivity(Intent(this@MainActivity, AboutUsActivity::class.java))
                }
                R.id.privacy_policy -> {
                    startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                }
                R.id.logout -> {
                    showLogoutDialog()
                }

            }
            true
        }

        // Todo Hide Side Menu Item
        binding.sideNavigation.menu.findItem(R.id.myaccount).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.my_order).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.myvirtualorder).isVisible =
            app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.refer_earn).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.logout).isVisible = app.user.getLoginSession()


        val userName =
            binding.sideNavigation.getHeaderView(0).findViewById(R.id.tv_user_name) as TextView
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
        } else {
            llLoginSignUp.visibility = View.VISIBLE
        }

        binding.sideNavigation.getHeaderView(0).setOnClickListener {
            if (!app.user.getLoginSession()) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        LoginActivity::class.java
                    )
                )
            }
        }
    }

    private fun moveToDashboard() {
        if (navController.currentDestination?.id != R.id.navigation_dashboard) {
            if (navController.currentDestination?.id == R.id.navigation_home) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_home_to_navigation_dashboard)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_dashboard)
            }
        }
    }

    private fun moveToHomeFragment() {
        if (navController.currentDestination?.id != R.id.navigation_home) {
            if (navController.currentDestination?.id == R.id.navigation_notifications) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_home)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_dashboard_to_navigation_home)
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("LOGOUT")
            .setMessage("Are you sure, you want to logout?")
            .setPositiveButton(
                "YES"
            ) { dialogInterface, i ->
                isDrawerIsOpen()
                app.user.clearSharedPreference()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialogInterface.dismiss()
                finish()
            }
            .setNegativeButton(
                "CANCEL"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

    private fun setBadgeCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<ProductDetail> = Utility().getAllProductList(this@MainActivity)
            if (!list.isEmpty()) {
                withContext(Dispatchers.Main) {
                    addBadge(list.size.toString())
                }
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
            tvCameraRequest.setOnClickListener {//openCamera()

                if (Utility().checkPermission(
                        this@MainActivity,
                        android.Manifest.permission.CAMERA
                    )
                ) {
                    takePictureFromCamera()
                }
            }
            /* tvVoiceRequest.setOnClickListener {//open Voice Recorder
                 if (Utility().checkPermission(
                         this@MainActivity,
                         android.Manifest.permission.RECORD_AUDIO
                     )
                 ) {
                     val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                     resultLauncher.launch(cameraIntent)
                 }
             }*/
            virtualCameraAlertDialog = dialogBuilder.create()
            virtualCameraAlertDialog!!.show()
        }
    }


    private fun takePictureFromCamera() {
        imageUri = createImageFile()!!
        cameraResult.launch(imageUri)
    }

    private fun createImageFile(): Uri? {
        val image = File(applicationContext.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.android.fileprovider",
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
        val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
        val selectedImage = BitmapFactory.decodeStream(imageStream)
        val converetdImage = selectedImage?.let { getResizedBitmap(it, 500) }
        val baos1 = ByteArrayOutputStream()
        converetdImage!!.compress(Bitmap.CompressFormat.JPEG, 95, baos1)
        val imageByteArray2 = baos1.toByteArray()
        val encodedImage: String = Base64.encodeToString(imageByteArray2, Base64.DEFAULT)
        showBottomSheetDialog(encodedImage)
    }


    private fun showBottomSheetDialog(encodedImage: String) {
        var addressId = ""
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_address)
        val dismissBottom = bottomSheetDialog.findViewById<ImageView>(R.id.iv_close)
        val btnAddAddress = bottomSheetDialog.findViewById<Button>(R.id.btn_add_address)
        val btnPlaceOrder = bottomSheetDialog.findViewById<Button>(R.id.btn_place_order)

        val rvAddressList = bottomSheetDialog.findViewById<RecyclerView>(R.id.rv_address_list)
        rvAddressList?.layoutManager = LinearLayoutManager(this@MainActivity)


        /*   if (sharedPreference.getlanguage(ApiConstants.language) == 1) {
               add_new_address.setText(getString(R.string.add_new_address_h))
               select_address?.setText(getString(R.string.select_address_h))
           }*/

        viewModel.getAddressListRequest(app.user.getUserDetails()?.userId).observe(this, Observer {
            if (it.addressList.size > 0) {
                rvAddressList?.adapter =
                    OrderByImageVirtualAdapter(it.addressList, object : OnitemClickListener {
                        override fun onclick(position: Int, data: String) {
                            addressId = data
                        }
                    })
            }
        })

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dismissBottom?.setOnClickListener { bottomSheetDialog.dismiss() }
        /*  add_new_address?.setOnClickListener {
              add_new_address?.isEnabled = false
              addAddressDialog()
          }*/
        btnPlaceOrder!!.setOnClickListener {
            if (!addressId.equals("")) {
                placeOrderApi(addressId, encodedImage, bottomSheetDialog)
            } else {
                Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun placeOrderApi(
        addressId: String,
        encodedImage: String,
        bottomSheetDialog: BottomSheetDialog
    ) {
        val jsonObject = JsonObject()

        jsonObject.addProperty(Constant.data, encodedImage)
        jsonObject.addProperty(Constant.addressId, addressId)
        jsonObject.addProperty(Constant.format, Constant.jpg)

        viewModel.placeVirtualOrderRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this,
                Observer {
                    Log.e("TAG", "placeOrderApi: " + it)

                    if (it.status) {
                        bottomSheetDialog.dismiss()
                        virtualCameraAlertDialog?.dismiss()
                    }
                })

        /*if (audioType.equals(ApiConstants.mp3, ignoreCase = true)) {
            jsonObject.put(ApiConstants.format, ApiConstants.mp3)
        } else {
            jsonObject.put(ApiConstants.format, ApiConstants.jpg)
        }*/
    }


    override fun onclick(position: Int, data: String) {
        /* if(data=="CLOSE"){

         }*/
    }
}