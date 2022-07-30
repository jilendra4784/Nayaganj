package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
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
        binding.include10.toolbarTitle.text = "My Account"

        binding.cvMyOrder.setOnClickListener { startActivity(Intent(this@MyAccountActivity, MyOrderActivity::class.java)) }
        binding.cvMyaddress.setOnClickListener{startActivity(Intent(this@MyAccountActivity, AddressListActivity::class.java))}
        binding.cvCustomerSupport.setOnClickListener{startActivity(Intent(this@MyAccountActivity, CustomerSupportActivity::class.java))}
        binding.cvAboutUs.setOnClickListener{startActivity(Intent(this@MyAccountActivity, AboutUsActivity::class.java))}
        binding.cvLogout.setOnClickListener{ showLogoutDialog() }
        binding.ivEdit.setOnClickListener{startActivity(Intent(this@MyAccountActivity, UpdateProfileActivity::class.java))}



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
}