package ru.fantlab.android.ui.modules.authors

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.micro_grid_refresh_list.*
import kotlinx.android.synthetic.main.state_layout.*
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.ContextMenuBuilder
import ru.fantlab.android.data.dao.model.AuthorInList
import ru.fantlab.android.data.dao.model.ContextMenus
import ru.fantlab.android.helper.BundleConstant.EXTRA
import ru.fantlab.android.ui.adapter.AuthorsAdapter
import ru.fantlab.android.ui.base.BaseActivity
import ru.fantlab.android.ui.modules.author.AuthorPagerActivity
import ru.fantlab.android.ui.widgets.dialog.ContextMenuDialogView

class AuthorsActivity : BaseActivity<AuthorsMvp.View, AuthorsPresenter>(), AuthorsMvp.View {

	private val adapter: AuthorsAdapter by lazy { AuthorsAdapter(presenter.authors) }

	override fun layout(): Int = R.layout.authors_layout

	override fun isTransparent(): Boolean = false

	override fun canBack(): Boolean = true

	override fun providePresenter(): AuthorsPresenter = AuthorsPresenter()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		title = getString(R.string.authors)
		hideShowShadow(true)
		selectMenuItem(R.id.authors, true)
		if (savedInstanceState == null) {
			stateLayout.hideProgress()
		}
		stateLayout.setEmptyText(R.string.no_results)
		stateLayout.setOnReloadListener(this)
		refresh.setOnRefreshListener(this)
		recycler.setEmptyView(stateLayout, refresh)
		adapter.listener = presenter
		recycler.adapter = adapter
		recycler.addNormalSpacingDivider()
		val letter = intent.extras?.getInt(EXTRA, 192) ?: 192
		if (presenter.authors.isEmpty() && !presenter.isApiCalled()) {
			presenter.setCurrentSort(letter)
		}
		fastScroller.attachRecyclerView(recycler)
		fastScroller.setHidden(true)
	}

	override fun onNotifyAdapter(items: ArrayList<AuthorInList>) {
		hideProgress()
		if (items.isEmpty()) {
			adapter.clear()
			stateLayout.showEmptyState()
			return
		}
		adapter.insertItems(items)
	}

	override fun onItemClicked(item: AuthorInList) {
		AuthorPagerActivity.startActivity(this, item.id, item.name, 0)
	}

	override fun onShowErrorView(msgRes: String?) {
		recycler.visibility = View.GONE
		stateLayout.setEmptyText(R.string.network_error)
		stateLayout.showErrorState()
	}

	override fun onRefresh() {
		presenter.onReload()
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

	override fun showMessage(titleRes: Int, msgRes: Int) {
		showReload()
		super.showMessage(titleRes, msgRes)
	}

	private fun showReload() {
		hideProgress()
		stateLayout.showReload(adapter.itemCount)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.authors_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.sort -> {
				showSortDialog()
			}
		}
		return super.onOptionsItemSelected(item)
	}

	private fun showSortDialog() {
		val dialogView = ContextMenuDialogView()
		val sort = presenter.getCurrentSort()
		dialogView.initArguments("main", ContextMenuBuilder.buildForAuthorsSorting(recycler.context, sort))
		dialogView.show(supportFragmentManager, "ContextMenuDialogView")
	}

	override fun onItemSelected(parent: String, item: ContextMenus.MenuItem, position: Int, listItem: Any) {
		recycler?.scrollToPosition(0)
		presenter.setCurrentSort(item.id.toInt())
	}
}