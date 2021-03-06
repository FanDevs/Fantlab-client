package ru.fantlab.android.ui.modules.author.bibliography

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.micro_grid_refresh_list.*
import kotlinx.android.synthetic.main.state_layout.*
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.ContextMenuBuilder
import ru.fantlab.android.data.dao.model.*
import ru.fantlab.android.helper.BundleConstant
import ru.fantlab.android.helper.Bundler
import ru.fantlab.android.helper.InputHelper
import ru.fantlab.android.helper.PrefGetter
import ru.fantlab.android.ui.adapter.viewholder.CycleViewHolder
import ru.fantlab.android.ui.adapter.viewholder.CycleWorkViewHolder
import ru.fantlab.android.ui.base.BaseFragment
import ru.fantlab.android.ui.modules.author.AuthorPagerMvp
import ru.fantlab.android.ui.modules.work.WorkPagerActivity
import ru.fantlab.android.ui.widgets.dialog.ContextMenuDialogView
import ru.fantlab.android.ui.widgets.dialog.RatingDialogView
import ru.fantlab.android.ui.widgets.treeview.TreeNode
import ru.fantlab.android.ui.widgets.treeview.TreeViewAdapter
import java.util.*
import kotlin.collections.ArrayList


class AuthorBibliographyFragment : BaseFragment<AuthorBibliographyMvp.View, AuthorBibliographyPresenter>(),
		AuthorBibliographyMvp.View {

	private var countCallback: AuthorPagerMvp.View? = null
	private lateinit var adapter: TreeViewAdapter

	private var cycles: WorksBlocks? = null
	private var works: WorksBlocks? = null

	override fun fragmentLayout() = R.layout.micro_grid_refresh_list

	override fun providePresenter() = AuthorBibliographyPresenter()

	override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
		stateLayout.setEmptyText(R.string.no_bibliography)
		stateLayout.setOnReloadListener(this)
		refresh.setOnRefreshListener(this)
		recycler.setEmptyView(stateLayout, refresh)
		presenter.onFragmentCreated(arguments!!)
	}

	override fun onInitViews(cycles: WorksBlocks?, works: WorksBlocks?) {
		this.cycles = cycles
		this.works = works

		val ids = ArrayList<WorksBlocks.Work>()
		val workIds = arrayListOf<Int>()
		this.cycles?.worksBlocks?.map {
			ids.addAll(it.list)
		}
		this.works?.worksBlocks?.map {
			ids.addAll(it.list)
		}

		ids.filter { it.id != null }.map { workIds.add(it.id!!) }
		if (isLoggedIn()) {
			presenter.getMarks(PrefGetter.getLoggedUser()!!.id, workIds)
		} else {
			initAdapter(this.cycles, works, null)
		}
	}

	override fun onGetMarks(marks: ArrayList<MarkMini>) {
		initAdapter(cycles, works, marks)
	}

	private fun initAdapter(bibliography: WorksBlocks?, works: WorksBlocks?, marks: ArrayList<MarkMini>?) {
		hideProgress()
		val nodes = arrayListOf<TreeNode<*>>()
		extractListData(bibliography, nodes, marks)
		extractListData(works, nodes, marks)
		adapter = TreeViewAdapter(nodes, Arrays.asList(CycleWorkViewHolder(), CycleViewHolder()))
		if (recycler.adapter != null)
			recycler.adapter!!.notifyDataSetChanged()
		else
			recycler.adapter = adapter
		adapter.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {
			override fun onSelected(extra: Int, add: Boolean) {
			}

			override fun onClick(node: TreeNode<*>, holder: RecyclerView.ViewHolder): Boolean {
				if (!node.isLeaf) {
					onToggle(!node.isExpand, holder)
				} else {
					val cycleWork = (node.content as CycleWork)
					val title = if (cycleWork.name.isNotEmpty()) {
						if (cycleWork.nameOrig.isNotEmpty()) {
							String.format("%s / %s", cycleWork.name, cycleWork.nameOrig)
						} else {
							cycleWork.name
						}
					} else {
						cycleWork.nameOrig
					}
					if (cycleWork.id ?: 0 != 0 ) WorkPagerActivity.startActivity(context!!, cycleWork.id!!, title, 0)
				}
				return false
			}

			override fun onToggle(isExpand: Boolean, holder: RecyclerView.ViewHolder) {
				val dirViewHolder = holder as CycleViewHolder.ViewHolder
				val ivArrow = dirViewHolder.ivArrow
				val rotateDegree = if (isExpand) 90.0f else -90.0f
				ivArrow.animate().rotationBy(rotateDegree)
						.start()
			}
		})
		adapter.setListener(presenter)
		fastScroller.attachRecyclerView(recycler)
	}

	private fun extractListData(bibliography: WorksBlocks?, nodes: ArrayList<TreeNode<*>>, marks: ArrayList<MarkMini>?) {
		bibliography?.worksBlocks?.sortedWith(compareBy { it.id })?.forEach { worksBlock ->
			val app = TreeNode(Cycle(worksBlock.title))
			nodes.add(app)

			worksBlock.list.forEachIndexed { subIndex, work ->

				val name = if (!work.name.isNullOrEmpty()) {
					if (!work.nameOrig.isNullOrEmpty()) {
						String.format("%s / %s", work.name, work.nameOrig)
					} else {
						work.name
					}
				} else {
					work.nameOrig
				}

				if (work.children != null) {
					val apps = TreeNode(Cycle(name ?: ""))
					app.addChild(apps)

					work.children.forEach { item ->
						val mark = marks?.map { it }?.filter { it.work_id == item.id }
						app.childList[subIndex].addChild(TreeNode(CycleWork(
								item.id,
								item.authors.asSequence().map { it.name }.toList(),
								item.name,
								item.nameOrig,
								item.description,
								item.year,
								item.responses,
								item.votersCount,
								item.rating,
								if (mark != null && mark.isNotEmpty()) mark.first().mark else null,
								if (mark != null && mark.isNotEmpty()) mark.first().user_work_classif_flag else 0
								))
						)
					}
				} else {
					val mark = marks?.map { it }?.filter { it.work_id == work.id }
					app.addChild(TreeNode(CycleWork(
							work.id,
							work.authors.asSequence().map { it.name }.toList(),
							work.name ?: "",
							work.nameOrig ?: "",
							work.description,
							work.year,
							work.responseCount,
							work.votersCount,
							work.rating,
							if (mark != null && mark.isNotEmpty()) mark.first().mark else null,
							if (mark != null && mark.isNotEmpty()) mark.first().user_work_classif_flag else 0
							))
					)
				}
			}
		}
	}

	override fun onItemLongClicked(item: TreeNode<*>, position: Int) {
		if (isLoggedIn() && item.content is CycleWork) {
			val dialogView = ContextMenuDialogView()
			dialogView.initArguments("main", ContextMenuBuilder.buildForMarks(recycler.context), item.content as CycleWork, position)
			dialogView.show(childFragmentManager, "ContextMenuDialogView")
		}
	}

	override fun onItemSelected(parent: String, item: ContextMenus.MenuItem, position: Int, listItem: Any) {
		listItem as CycleWork
		when (item.id) {
			"revote" -> {
				RatingDialogView.newInstance(10, listItem.mark?.toFloat() ?: 0.0f,
						listItem,
						"${listItem.authors[0]} - ${if (!InputHelper.isEmpty(listItem.name)) listItem.name else listItem.nameOrig}",
						position
				).show(childFragmentManager, RatingDialogView.TAG)
			}
			"delete" -> {
				presenter.onSendMark(listItem.id!!, 0, position)
			}
		}
	}

	override fun onRated(rating: Float, listItem: Any, position: Int) {
		presenter.onSendMark((listItem as CycleWork).id!!, rating.toInt(), position)
	}

	override fun onSetMark(position: Int, mark: Int) {
		hideProgress()
		(adapter.getItem(position) as CycleWork).mark = mark
		adapter.notifyItemChanged(position)
	}

	override fun showProgress(@StringRes resId: Int, cancelable: Boolean) {
		refresh.isRefreshing = true
		stateLayout.showProgress()
	}

	override fun hideProgress() {
		refresh.isRefreshing = false
		stateLayout.showReload(recycler.adapter?.itemCount ?: 0)
	}

	override fun showErrorMessage(msgRes: String?) {
		hideProgress()
		super.showErrorMessage(msgRes)
	}

	override fun showMessage(titleRes: Int, msgRes: Int) {
		hideProgress()
		super.showMessage(titleRes, msgRes)
	}

	companion object {

		fun newInstance(authorId: Int): AuthorBibliographyFragment {
			val view = AuthorBibliographyFragment()
			view.arguments = Bundler.start().put(BundleConstant.EXTRA, authorId).end()
			return view
		}
	}

	override fun onRefresh() {
		presenter.onFragmentCreated(arguments!!)
	}

	override fun onClick(v: View?) {
		onRefresh()
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is AuthorPagerMvp.View) {
			countCallback = context
		}
	}

	override fun onDetach() {
		countCallback = null
		super.onDetach()
	}
}