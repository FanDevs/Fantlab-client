package ru.fantlab.android.old.authors

import com.arellomobile.mvp.MvpView

interface IAuthorsView : MvpView {

	fun showAuthors(authors: List<AuthorInList>, scrollToTop: Boolean)

	fun showError(message: String)
}