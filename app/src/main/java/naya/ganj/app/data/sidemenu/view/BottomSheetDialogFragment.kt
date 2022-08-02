package naya.ganj.app.data.sidemenu.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import naya.ganj.app.R
import naya.ganj.app.interfaces.OnitemClickListener

class BottomSheetDialogFragment(
    private val tittle: Int,
    val value: String?,
    private val onitemClickListener: OnitemClickListener
) :
    BottomSheetDialogFragment() {
    lateinit var btnUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet, container, false)
        val titleTextview = view.findViewById(R.id.tv_title) as TextView
        val editText = view.findViewById(R.id.edittext) as EditText
        btnUpdate = view.findViewById(R.id.btn_update) as Button

        disableButton()

        if (tittle == 1) {
            titleTextview.text = "Update Name"
        } else if (tittle == 2) {
            titleTextview.text = "Update Email Id"
        } else {
            titleTextview.text = "Update Mobile Number"
        }

        editText.setText(value)
        btnUpdate.setOnClickListener {
            onitemClickListener.onclick(tittle, editText.text.toString())
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().equals(value)) {
                    disableButton()
                } else {
                    enableButton()
                }
            }
        })

        return view
    }

    private fun disableButton() {
        btnUpdate.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#d3d3d3"))
        btnUpdate.isEnabled = false
    }

    private fun enableButton() {
        btnUpdate.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#3390CB"))
        btnUpdate.isEnabled = true
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }

}
