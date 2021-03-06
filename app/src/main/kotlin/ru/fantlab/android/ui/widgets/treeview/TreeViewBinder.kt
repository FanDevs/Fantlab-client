package ru.fantlab.android.ui.widgets.treeview

import android.view.View
import androidx.recyclerview.widget.RecyclerView


abstract class TreeViewBinder<VH : RecyclerView.ViewHolder> : LayoutItemType {
	abstract fun provideViewHolder(itemView: View): VH

	abstract fun bindView(holder: RecyclerView.ViewHolder, position: Int, node: TreeNode<*>, onTreeNodeListener: TreeViewAdapter.OnTreeNodeListener?)

	open class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)

}
