package ru.fantlab.android.ui.modules.search

import android.widget.AutoCompleteTextView
import androidx.annotation.IntRange
import androidx.viewpager.widget.ViewPager
import ru.fantlab.android.ui.base.mvp.BaseMvp
import java.util.*

/**
 * Created by Kosh on 08 Dec 2016, 8:19 PM
 */

interface SearchMvp {

	interface View : BaseMvp.View {

		fun onNotifyAdapter(query: String?)

		fun onSetCount(count: Int, @IntRange(from = 0, to = 3) index: Int)
	}

	interface Presenter : BaseMvp.Presenter {

		fun getHints(): ArrayList<String>

		fun onSearchClicked(
				viewPager: ViewPager,
				editText: AutoCompleteTextView,
				isIsbn: Boolean = false
		)
	}
}
