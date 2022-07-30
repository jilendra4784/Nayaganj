package naya.ganj.app.data.sidemenu.view

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.databinding.ActivityUpdateProfileBinding
import naya.ganj.app.interfaces.OnitemClickListener


class UpdateProfileActivity : AppCompatActivity(), OnitemClickListener {
    lateinit var binding: ActivityUpdateProfileBinding
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj

        binding.tvName.text = app.user.getUserDetails()?.name
        binding.tvEmail.text = app.user.getUserDetails()?.emailId
        binding.tvMobile.text = app.user.getUserDetails()?.mNumber
        binding.tvLang.text = "English"


        binding.include11.ivBackArrow.setOnClickListener { finish() }
        binding.include11.toolbarTitle.text = "Update Profile"

        binding.ivNameEdit.setOnClickListener {
            showBottomSheetDialog(
                0,
                app.user.getUserDetails()?.name
            )
        }
        binding.ivEmailEdit.setOnClickListener {
            showBottomSheetDialog(
                1,
                app.user.getUserDetails()?.emailId
            )
        }
        binding.ivEditMobile.setOnClickListener {
            showBottomSheetDialog(
                3,
                app.user.getUserDetails()?.mNumber
            )
        }
        binding.ivLangEdit.setOnClickListener { showBottomSheetDialog(4, "") }

    }

    private fun showBottomSheetDialog(title: Int, value: String?) {
        if (title == 4) {
            // show language dialog
        } else {
            val modalBottomSheet = BottomSheetDialogFragment(title, value, this)
            modalBottomSheet.show(supportFragmentManager, modalBottomSheet.tag)

        }
    }

    override fun onclick(position: Int, data: String) {
        // hit api
        Log.e(TAG, "onclick: " + data)
    }
}