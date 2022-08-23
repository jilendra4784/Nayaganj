package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.view.OTPVerifyActivity
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.data.sidemenu.viewmodel.UpdateProfileActivityViewModel
import naya.ganj.app.databinding.ActivityUpdateProfileBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant


class UpdateProfileActivity : AppCompatActivity(), OnitemClickListener {
    lateinit var binding: ActivityUpdateProfileBinding
    lateinit var app: Nayaganj
    private var valueIdentifier = 0
    lateinit var viewModel: UpdateProfileActivityViewModel
    private lateinit var modalBottomSheet: BottomSheetDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[UpdateProfileActivityViewModel::class.java]

        if (app.user.getAppLanguage() == 1) {
            binding.tvStaticName.text = resources.getString(R.string.name_h)
            binding.tvStaticEmail.text = resources.getString(R.string.email_h)
            binding.tvStaticNumber.text = resources.getString(R.string.mobile_number_h)
            binding.tvStaticLang.text = resources.getString(R.string.selected_lan_h)
            binding.include11.toolbarTitle.text = resources.getString(R.string.update_profile_h)
        } else {
            binding.include11.toolbarTitle.text = "Update Profile"
        }

        binding.tvName.text = app.user.getUserDetails()?.name
        binding.tvEmail.text = app.user.getUserDetails()?.emailId
        binding.tvMobile.text = app.user.getUserDetails()?.mNumber
        binding.tvLang.text = "English"


        binding.include11.ivBackArrow.setOnClickListener { finish() }


        binding.ivNameEdit.setOnClickListener {
            valueIdentifier = 1
            showBottomSheetDialog(
                valueIdentifier,
                app.user.getUserDetails()?.name
            )
        }
        binding.ivEmailEdit.setOnClickListener {
            valueIdentifier = 2
            showBottomSheetDialog(
                valueIdentifier,
                app.user.getUserDetails()?.emailId
            )
        }
        binding.ivEditMobile.setOnClickListener {
            valueIdentifier = 3
            showBottomSheetDialog(
                valueIdentifier,
                app.user.getUserDetails()?.mNumber
            )
        }
        binding.ivLangEdit.setOnClickListener {
            showMaterialAlertDialog()
        }

        if (app.user.getAppLanguage() == 1) {
            binding.tvLang.text = "Hindi"
        } else {
            binding.tvLang.text = "English"
        }

    }

    private fun showMaterialAlertDialog() {
        lateinit var lang: String
        var selectLanguage = app.user.getAppLanguage()
        val language = arrayOf("English", "Hindi")
        lang = language[selectLanguage]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(language, selectLanguage) { _, which ->
                selectLanguage = which
                lang = language[which]

                if (lang == "Hindi") {
                    app.user.setAppLanguage(1)
                    binding.tvLang.text = lang
                } else {
                    app.user.setAppLanguage(0)
                    binding.tvLang.text = lang
                }
            }
            .setPositiveButton("Ok") { dialog, which ->
                Toast.makeText(this, "$lang Changed Successfully", Toast.LENGTH_SHORT).show()

                val intent=Intent(this@UpdateProfileActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showBottomSheetDialog(title: Int, value: String?) {
        if (title == 4) {
            // show language dialog
        } else {
            modalBottomSheet = BottomSheetDialog(title, value, this)
            modalBottomSheet.show(supportFragmentManager, modalBottomSheet.tag)
        }
    }

    override fun onclick(position: Int, data: String) {
        Log.e("TAG", "onclick: " + position)
        val jsonObject = JsonObject()
        if (position == 3) {
            jsonObject.addProperty(Constant.MobileNumber, data)
            viewModel.updateMobileNumber(app.user.getUserDetails()?.userId, jsonObject)
                .observe(this@UpdateProfileActivity) {
                    modalBottomSheet.dismiss()
                    if (it != null && it.status) {
                        if (position == 3) {
                            val intent =
                                Intent(this@UpdateProfileActivity, OTPVerifyActivity::class.java)
                            intent.putExtra(Constant.MobileNumber, data)
                            startActivity(intent)
                        }
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Details updatedSuccessfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@UpdateProfileActivity, it.msg, Toast.LENGTH_LONG).show()
                    }

                }
        } else {

            if (position == 1) {
                jsonObject.addProperty("newName", data)
                jsonObject.addProperty("newEmailId", "")
                jsonObject.addProperty(Constant.OTP, "")
                jsonObject.addProperty("newMobileNumber", "")

            }
            if (position == 2) {
                jsonObject.addProperty("newName", "")
                jsonObject.addProperty("newEmailId", data)
                jsonObject.addProperty(Constant.OTP, "")
                jsonObject.addProperty("newMobileNumber", "")

            }

            viewModel.updateUserDetailRequest(app.user.getUserDetails()?.userId, jsonObject)
                .observe(this@UpdateProfileActivity) {
                    modalBottomSheet.dismiss()
                    if (it != null && it.status) {
                        if (position == 1) {
                            val mUserDetails = app.user.getUserDetails()
                            mUserDetails?.name = data
                            app.user.saveUserDetail(mUserDetails)
                            binding.tvName.text = data
                        }
                        if (position == 2) {
                            val mUserDetails = app.user.getUserDetails()
                            mUserDetails?.emailId = data
                            app.user.saveUserDetail(mUserDetails)
                            binding.tvEmail.text = data
                        }
                        if (position == 3) {
                            val intent =
                                Intent(this@UpdateProfileActivity, OTPVerifyActivity::class.java)
                            intent.putExtra(Constant.MobileNumber, data)
                            startActivity(intent)
                        }
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Details updatedSuccessfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@UpdateProfileActivity, it.msg, Toast.LENGTH_LONG).show()
                    }

                }

        }


    }
}