package ru.fantlab.android.ui.modules.award.overview

import android.os.Bundle
import ru.fantlab.android.data.dao.model.Author
import ru.fantlab.android.data.dao.model.Award
import ru.fantlab.android.data.dao.model.Biography
import ru.fantlab.android.ui.base.mvp.BaseMvp

interface AwardOverviewMvp {

	interface View : BaseMvp.View {

		fun onInitViews(award: Award)
	}

	interface Presenter : BaseMvp.Presenter {

		fun onFragmentCreated(bundle: Bundle?)

		fun onWorkOffline(id: Int)
	}
}