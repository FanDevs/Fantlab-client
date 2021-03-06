package ru.fantlab.android.ui.modules.profile.responses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.evernote.android.state.State
import kotlinx.android.synthetic.main.micro_grid_refresh_list.*
import kotlinx.android.synthetic.main.state_layout.*
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.ContextMenuBuilder
import ru.fantlab.android.data.dao.model.ContextMenus
import ru.fantlab.android.data.dao.model.Response
import ru.fantlab.android.helper.BundleConstant
import ru.fantlab.android.helper.Bundler
import ru.fantlab.android.helper.PrefGetter
import ru.fantlab.android.provider.rest.loadmore.OnLoadMore
import ru.fantlab.android.ui.adapter.ResponsesAdapter
import ru.fantlab.android.ui.base.BaseFragment
import ru.fantlab.android.ui.modules.editor.EditorActivity
import ru.fantlab.android.ui.modules.user.UserPagerActivity
import ru.fantlab.android.ui.modules.user.UserPagerMvp
import ru.fantlab.android.ui.modules.work.responses.overview.ResponseOverviewActivity
import ru.fantlab.android.ui.widgets.dialog.ContextMenuDialogView

class ProfileResponsesFragment : BaseFragment<ProfileResponsesMvp.View, ProfileResponsesPresenter>(),
		ProfileResponsesMvp.View {

	@State var userId: Int = -1
	private val onLoadMore: OnLoadMore<Int> by lazy { OnLoadMore(presenter, userId) }
	private val adapter: ResponsesAdapter by lazy { ResponsesAdapter(arrayListOf()) }
	private var countCallback: UserPagerMvp.View? = null

	override fun fragmentLayout(): Int = R.layout.micro_grid_refresh_list

	override fun providePresenter(): ProfileResponsesPresenter = ProfileResponsesPresenter()

	override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
		if (savedInstanceState == null) {
			stateLayout.hideProgress()
		}
		stateLayout.setEmptyText(R.string.no_responses)
		stateLayout.setOnReloadListener(this)
		refresh.setOnRefreshListener(this)
		recycler.setEmptyView(stateLayout, refresh)
		adapter.listener = presenter
		recycler.adapter = adapter
		userId = arguments!!.getInt(BundleConstant.EXTRA)
		getLoadMore().initialize(presenter.getCurrentPage() - 1, presenter.getPreviousTotal())
		recycler.addOnScrollListener(getLoadMore())
		presenter.getResponses(userId, false)
		fastScroller.attachRecyclerView(recycler)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is UserPagerMvp.View) {
			countCallback = context
		}
	}

	override fun onDetach() {
		countCallback = null
		super.onDetach()
	}

	override fun onDestroyView() {
		recycler.removeOnScrollListener(getLoadMore())
		super.onDestroyView()
	}

	override fun onNotifyAdapter(items: ArrayList<Response>, page: Int) {
		hideProgress()
		if (items.isEmpty()) {
			adapter.clear()
			stateLayout.showEmptyState()
			return
		}
		if (page <= 1) {
			adapter.insertItems(items)
		} else {
			adapter.addItems(items)
		}
	}

	override fun getLoadMore() = onLoadMore

	override fun onSetTabCount(count: Int) {
		countCallback?.onSetBadge(2, count)
	}

	override fun onItemClicked(item: Response) {
		ResponseOverviewActivity.startActivity(context!!, item)
	}

	override fun onItemLongClicked(item: Response, position: Int) {
		if (isLoggedIn() && PrefGetter.getLoggedUser()?.id == userId) {
			val dialogView = ContextMenuDialogView()
			dialogView.initArguments("main", ContextMenuBuilder.buildForUserResponse(recycler.context), item, position)
			dialogView.show(childFragmentManager, "ContextMenuDialogView")
		}
	}

	override fun onRefresh() {
		presenter.getResponses(userId, true)
	}

	override fun onClick(v: View?) {
		onRefresh()
	}

	override fun hideProgress() {
		refresh.isRefreshing = false
		stateLayout.hideProgress()
	}

	override fun showProgress(@StringRes resId: Int, cancelable: Boolean) {
		refresh.isRefreshing = true
		stateLayout.showProgress()
	}

	override fun showErrorMessage(msgRes: String?) {
		callback?.showErrorMessage(msgRes)
	}

	override fun showMessage(titleRes: Int, msgRes: Int) {
		showReload()
		super.showMessage(titleRes, msgRes)
	}

	private fun showReload() {
		hideProgress()
		stateLayout.showReload(adapter.itemCount)
	}

	companion object {

		fun newInstance(userId: Int): ProfileResponsesFragment {
			val view = ProfileResponsesFragment()
			view.arguments = Bundler.start().put(BundleConstant.EXTRA, userId).end()
			return view
		}
	}

	override fun onSetVote(position: Int, votesCount: String) {
		hideProgress()
		adapter.getItem(position).voteCount = votesCount.toInt()
		adapter.notifyItemChanged(position)

	}

	override fun onOpenContextMenu(userItem: Response) {
		val dialogView = ContextMenuDialogView()
		dialogView.initArguments("main", ContextMenuBuilder.buildForProfile(recycler.context), userItem, 0)
		dialogView.show(childFragmentManager, "ContextMenuDialogView")
	}

	override fun onItemSelected(parent: String, item: ContextMenus.MenuItem, position: Int, listItem: Any) {
		if (listItem is Response) when (item.id) {
			"vote" -> {
				presenter.onSendVote(listItem, position, if (item.title.contains("+")) "plus" else "minus")
			}
			"profile" -> {
				UserPagerActivity.startActivity(context!!, listItem.userName, listItem.userId, 0)
			}
			"message" -> {
				startActivity(Intent(activity, EditorActivity::class.java)
						.putExtra(BundleConstant.EXTRA_TYPE, BundleConstant.EDITOR_NEW_MESSAGE)
						.putExtra(BundleConstant.ID, listItem.userId)
				)
			}
			"edit" -> {
				startActivityForResult(Intent(activity, EditorActivity::class.java)
						.putExtra(BundleConstant.EXTRA_TYPE, BundleConstant.EDITOR_EDIT_RESPONSE)
						.putExtra(BundleConstant.EXTRA, listItem.text)
						.putExtra(BundleConstant.EXTRA_TWO, listItem.id)
						.putExtra(BundleConstant.EXTRA_THREE, position)
						.putExtra(BundleConstant.ID, listItem.workId),
						BundleConstant.REFRESH_RESPONSE_CODE)
			}
			"delete" -> {
				presenter.onDeleteResponse(listItem.workId, listItem.id, position)
			}
		} else {
			presenter.setCurrentSort(item.id)
		}
	}

	override fun onResponseDelete(position: Int) {
		hideProgress()
		adapter.removeItem(position)
		onSetTabCount(adapter.itemCount)
	}

	fun showSortDialog() {
		val dialogView = ContextMenuDialogView()
		val sort = presenter.getCurrentSort()
		dialogView.initArguments("main", ContextMenuBuilder.buildForResponseSorting(recycler.context, sort))
		dialogView.show(childFragmentManager, "ContextMenuDialogView")
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			BundleConstant.REFRESH_RESPONSE_CODE -> {
				if (data != null) {
					val position = data.extras?.getInt(BundleConstant.ID)
					if (position != null && position != -1) {
						val responseNewText = data.extras?.getCharSequence(BundleConstant.EXTRA)
						adapter.data[position].text = responseNewText.toString()
						adapter.notifyItemChanged(position)
					}
				}
			}
		}
	}

	override fun onStart() {
		if (presenter != null) adapter.setOnContextMenuListener(this)
		super.onStart()
	}
}