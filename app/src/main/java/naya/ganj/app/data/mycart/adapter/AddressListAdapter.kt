package naya.ganj.app.data.mycart.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.view.AddAddressActivity
import naya.ganj.app.databinding.AdapterAddressListRowBinding
import naya.ganj.app.interfaces.OnitemClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddressListAdapter(
    private val addressList: MutableList<AddressListModel.Address>,
    val context: Context,
    private val onitemClickListener: OnitemClickListener,
    var addressId: String
) :
    RecyclerView.Adapter<AddressListAdapter.MyViewHolder>() {
    var radioSelection = 0

    class MyViewHolder(val binding: AdapterAddressListRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            AdapterAddressListRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val m = addressList.get(position)
        holder.binding.tvTitle.text = m.address.firstName + " " + m.address.lastName
        holder.binding.tvDescription.text = m.address.nickName
        holder.binding.tvAddress.text =
            m.address.houseNo + m.address.apartName + m.address.street + m.address.city + m.address.pincode
        holder.binding.tvMobile.text = m.address.contactNumber

        holder.binding.radioButton2.isChecked = m.id == addressId

        holder.binding.ivDelete.setOnClickListener {
            showDialog(holder.adapterPosition, m)
        }

        holder.binding.ivEdit.setOnClickListener {

            val intent = Intent(context, AddAddressActivity::class.java)
            intent.putExtra("addressId", m.id)
            intent.putExtra("firstName", m.address.firstName)
            intent.putExtra("lastName", m.address.lastName)
            intent.putExtra("contactNumber", m.address.contactNumber)
            intent.putExtra("houseNo", m.address.houseNo)
            intent.putExtra("ApartName", m.address.apartName)
            intent.putExtra("street", m.address.street)
            intent.putExtra("landmark", m.address.landmark)
            intent.putExtra("city", m.address.city)
            intent.putExtra("pincode", m.address.pincode)
            intent.putExtra("nickName", m.address.nickName)
            context.startActivity(intent)
        }
        holder.binding.radioButton2.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                addressId = m.id
                notifyDataSetChanged()
                onitemClickListener.onclick(holder.adapterPosition, m.id + "@SET_ADDRESS")
            }
        }

    }

    private fun showDialog(adapterPosition: Int, m: AddressListModel.Address) {

        MaterialAlertDialogBuilder(context)
            .setTitle("Remove Address")
            .setMessage("Are you sure, you want to remove it ?")
            .setNegativeButton("NO") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("YES") { dialog, which ->
                deleteAddreesRequest(adapterPosition, m)
                dialog.cancel()
            }
            .show()

    }

    private fun deleteAddreesRequest(adapterPosition: Int, m: AddressListModel.Address) {
        addressList.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
        notifyItemRangeChanged(adapterPosition, addressList.size)
        onitemClickListener.onclick(adapterPosition, m.id + "@DELETE")
    }

    override fun getItemCount(): Int {
        return addressList.size
    }
}