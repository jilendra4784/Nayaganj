package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.view.AddressListActivity
import naya.ganj.app.databinding.ActivityMyAccountBinding

class MyAccountActivity : AppCompatActivity() {

    lateinit var binding: ActivityMyAccountBinding
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app=applicationContext as Nayaganj
        binding.include10.ivBackArrow.setOnClickListener { finish() }

        if(app.user.getAppLanguage()==1){
            binding.tvMyOrder.text=resources.getString(R.string.myorders_h)
            binding.tvMyVirtualOrder.text=resources.getString(R.string.my_virtual_order_hindi)
            binding.tvMyaddress.text=resources.getString(R.string.my_addresses_h)
            binding.tvWallet.text=resources.getString(R.string.my_wallet_h)
            binding.tvBeRetailer.text=resources.getString(R.string.be_a_retailer_h)
            binding.tvShare.text=resources.getString(R.string.share_h)
            binding.tvCustomerSupport.text=resources.getString(R.string.csupport_h)
            binding.tvAboutUs.text=resources.getString(R.string.about_us_h)
            binding.tvLogout.text=resources.getString(R.string.logout_h)
            binding.include10.toolbarTitle.text = resources.getString(R.string.my_account_h)
        }else{
            binding.include10.toolbarTitle.text = "My Account"
        }


        binding.cvMyOrder.setOnClickListener { startActivity(Intent(this@MyAccountActivity, MyOrderActivity::class.java)) }
        binding.cvMyVirtualOrder.setOnClickListener{startActivity(Intent(this@MyAccountActivity, MyVirtualActivity::class.java))}
        binding.cvMyaddress.setOnClickListener{startActivity(Intent(this@MyAccountActivity, AddressListActivity::class.java))}
        binding.cvCustomerSupport.setOnClickListener{startActivity(Intent(this@MyAccountActivity, CustomerSupportActivity::class.java))}
        binding.cvAboutUs.setOnClickListener{startActivity(Intent(this@MyAccountActivity, AboutUsActivity::class.java))}
        binding.cvLogout.setOnClickListener{ showLogoutDialog() }
        binding.ivEdit.setOnClickListener{startActivity(Intent(this@MyAccountActivity, UpdateProfileActivity::class.java))}
        binding.cvShare.setOnClickListener{
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" +"naya.ganj.app" )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        binding.cvRetailer.setOnClickListener{startActivity(Intent(this@MyAccountActivity, RetailerActivity::class.java))}
    }
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@MyAccountActivity)
            .setTitle("LOGOUT")
            .setMessage("Are you sure, you want to logout?")
            .setPositiveButton(
                "YES"
            ) { dialogInterface, i ->
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

    override fun onResume() {
        super.onResume()
        binding.tvuUsername.text = app.user.getUserDetails()?.name
        binding.tvMobile.text = app.user.getUserDetails()?.mNumber
    }
}