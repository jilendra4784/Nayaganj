package naya.ganj.app.data.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.databinding.AdapterHomeLayoutBinding

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: AdapterHomeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.MyViewHolder {
        val view =
            AdapterHomeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.MyViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 20
    }
}