package naya.ganj.app.data.category.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.utility.Utility


class NewExpandableListAdapter(private var context: Context? = null,
        private var expandableListTitle: List<String>? = null,
        private var expandableListDetail: HashMap<String, List<String>>? = null,val app: Nayaganj
) : BaseExpandableListAdapter() {


    override fun getChild(listPosition: Int, expandedListPosition: Int): Any? {
        return expandableListDetail!![expandableListTitle!![listPosition]]?.get(expandedListPosition)
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View? {
        var convertView: View? = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String?
        if (convertView == null) {
            val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, null)
        }
        val expandedListTextView = convertView?.findViewById(R.id.expandedListItem) as TextView

        val childTitle=Utility.convertLanguage(expandedListText,app)
        expandedListTextView.text = childTitle

        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail!![expandableListTitle!![listPosition]]?.size ?: 0
    }

    override fun getGroup(listPosition: Int): Any {
        return expandableListTitle!![listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle!!.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View {
        var convertView: View? = convertView
        val listTitle = getGroup(listPosition) as String?
        if (convertView == null) {
            convertView=    LayoutInflater.from(parent?.context).inflate(R.layout.list_group, parent, false)
        }
        val listTitleTextView = convertView!!.findViewById(R.id.listTitle) as TextView

        val title=Utility.convertLanguage(listTitle,app)
        listTitleTextView.text = title

        val indicatorImageView = convertView.findViewById(R.id.iv_arrow_right) as ImageView
        if (isExpanded) {
            indicatorImageView.setImageResource(R.drawable.ic_down_arrow)
            listTitleTextView.setTypeface(null, Typeface.BOLD)

        } else {
            indicatorImageView.setImageResource(R.drawable.ic_right_arrow)
            listTitleTextView.setTypeface(null, Typeface.NORMAL)
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}