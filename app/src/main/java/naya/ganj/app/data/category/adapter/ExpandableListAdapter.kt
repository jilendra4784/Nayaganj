package naya.ganj.app.data.category.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import naya.ganj.app.R
import naya.ganj.app.data.category.model.CategoryDataModel


class ExpandableListAdapter internal constructor(
    private val categoryData: CategoryDataModel
) :

    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return categoryData.categoryList.size
    }

    override fun getChildrenCount(p0: Int): Int {
        return categoryData.categoryList[p0].subCategoryList.size
    }

    override fun getGroup(p0: Int): Any {
        return categoryData.categoryList[p0].category
    }

    override fun getChild(p0: Int, p1: Int): Any {

        return categoryData.categoryList[p0].subCategoryList[p1].category
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return p1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, p3: ViewGroup?): View {
        var convertView = p2
        val listTitle = getGroup(p0) as String
        val list = listTitle.split("$")

        if (convertView == null) {
            convertView = LayoutInflater.from(p3?.context).inflate(R.layout.list_group, p3, false)
            val textView = convertView!!.findViewById(R.id.listTitle) as TextView
            textView.text = list[0]
        }
        /*val indicatorImageView = convertView.findViewById(R.id.iv_arrow) as ImageView
        if (p1) {
            indicatorImageView.setImageResource(R.drawable.ic_down_arrow)
        } else {
            indicatorImageView.setImageResource(R.drawable.ic_right_arrow)
        }*/

        return convertView
    }

    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, p4: ViewGroup?): View {
        var convertView = p3
        val listItem = getChild(p0, p1) as String
        val list = listItem.split("$")
        if (convertView == null) {
            convertView = LayoutInflater.from(p4?.context).inflate(R.layout.list_item, p4, false)
            val textView = convertView!!.findViewById(R.id.expandedListItem) as TextView
            textView.text = list[0]
            //Log.e("TAG", "getChildView: "+list )
        }
        return convertView
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}