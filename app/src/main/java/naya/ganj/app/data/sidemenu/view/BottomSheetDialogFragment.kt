package naya.ganj.app.data.sidemenu.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import naya.ganj.app.R
import naya.ganj.app.interfaces.OnitemClickListener

class BottomSheetDialogFragment(
    private val tittle: Int,
    val value: String?,
    private val onitemClickListener: OnitemClickListener
) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet, container, false)
        val titleTextview = view.findViewById(R.id.tv_title) as TextView
        val editText = view.findViewById(R.id.edittext) as EditText
        val btnUpdate = view.findViewById(R.id.btn_update) as Button

        if (tittle == 0) {
            titleTextview.text = "Update Name"
        } else if (tittle == 1) {
            titleTextview.text = "Update Email Id"
        } else {
            titleTextview.text = "Update Mobile Number"
        }

        editText.setText(value)
        btnUpdate.setOnClickListener {
            onitemClickListener.onclick(0, value.toString())
        }
        return view
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }

}
