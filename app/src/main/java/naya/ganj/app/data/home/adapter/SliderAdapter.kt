package naya.ganj.app.data.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.databinding.SliderLayoutBinding

class SliderAdapter(val context: Context, private val bannerList: List<HomePageModel.Data.PromoBanner>) :
    SliderViewAdapter<SliderAdapter.SliderViewHolder>() {

    override fun getCount(): Int {
        return bannerList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val view = SliderLayoutBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        return SliderViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        val url = bannerList[position].img
        Glide.with(context)
            .load(url) // image url
            .into(viewHolder.binding.myimage);
    }


    class SliderViewHolder(val binding: SliderLayoutBinding) :
        SliderViewAdapter.ViewHolder(binding.root)
}