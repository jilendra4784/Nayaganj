package naya.ganj.app.utility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.databinding.AddressVrtualAdapterBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.utility.OrderByImageVirtualAdapter.MyViewHolder

class OrderByImageVirtualAdapter(val addressList: MutableList<AddressListModel.Address>,val onitemClickListener: OnitemClickListener) :
    RecyclerView.Adapter<MyViewHolder>() {

    class MyViewHolder(val binding: AddressVrtualAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            AddressVrtualAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val m = addressList.get(position)
        holder.binding.tvTitle.text = m.address.firstName + " " + m.address.lastName
        holder.binding.tvDescription.text = m.address.nickName
        holder.binding.tvAddress.text =
            m.address.houseNo + m.address.apartName + m.address.street + m.address.city + m.address.pincode
        holder.binding.tvMobile.text = m.address.contactNumber

        holder.binding.radioButton2.setOnClickListener {
            onitemClickListener.onclick(0,m.id)
        }
    }
}