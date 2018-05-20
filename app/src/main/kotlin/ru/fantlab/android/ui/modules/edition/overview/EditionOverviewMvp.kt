package ru.fantlab.android.ui.modules.edition.overview

import android.os.Bundle
import ru.fantlab.android.data.dao.newmodel.Edition
import ru.fantlab.android.ui.base.mvp.BaseMvp

interface EditionOverviewMvp {

	interface View : BaseMvp.View {

		fun onInitViews(edition: Edition)
	}

	interface Presenter : BaseMvp.Presenter {

		fun onFragmentCreated(bundle: Bundle?)

		fun onWorkOffline(id: Int)
	}
}