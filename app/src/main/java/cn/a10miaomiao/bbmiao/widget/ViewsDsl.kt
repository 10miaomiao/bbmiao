package cn.a10miaomiao.bbmiao.widget

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.viewpager2.widget.ViewPager2
import cn.a10miaomiao.bbmiao.widget.expandabletext.ExpandableTextView
import cn.a10miaomiao.bbmiao.widget.gridimage.NineGridImageView
import cn.a10miaomiao.bbmiao.widget.image.RCImageView
import cn.a10miaomiao.bbmiao.widget.layout.LimitedFrameLayout
import cn.a10miaomiao.bbmiao.widget.recycler.ViewPager2Container
import cn.a10miaomiao.bbmiao.widget.picker.DatePickerView
import cn.a10miaomiao.bbmiao.widget.picker.MonthPickerView
import cn.a10miaomiao.bbmiao.widget.recycler.RecyclerviewAtViewPager2
import cn.a10miaomiao.bbmiao.widget.text.BadgeTextView
import splitties.views.dsl.core.*

inline fun View.rcImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: RCImageView.() -> Unit = {}
): RCImageView {
    return view({ RCImageView(it) }, id).apply(initView)
}

inline fun Ui.expandableTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ExpandableTextView.() -> Unit = {}
): ExpandableTextView {
    return view({ ExpandableTextView(it) }, id).apply(initView)
}

inline fun View.expandableTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: ExpandableTextView.() -> Unit = {}
): ExpandableTextView {
    return view({ ExpandableTextView(it) }, id).apply(initView)
}

inline fun View.limitedFrameLayout(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: LimitedFrameLayout.() -> Unit = {}
): LimitedFrameLayout {
    return view({ LimitedFrameLayout(it) }, id).apply(initView)
}

inline fun View.wrapInLimitedFrameLayout(
    maxWidth: Int = 0,
    maxHeight: Int = 0,
    @IdRes id: Int = View.NO_ID,
    initView: LimitedFrameLayout.() -> Unit = {}
): LimitedFrameLayout {
    return view({ LimitedFrameLayout(it) }, id).apply {
        this.maxWidth = maxWidth
        this.maxHeight = maxHeight
        initView()
        addView(this@wrapInLimitedFrameLayout, lParams(matchParent, matchParent))
    }
}

inline fun ViewPager2.wrapInViewPager2Container(
    @IdRes id: Int = View.NO_ID,
    initView: ViewPager2Container.() -> Unit = {}
): ViewPager2Container {
    return view({ ViewPager2Container(it) }, id).apply {
        initView()
        addView(this@wrapInViewPager2Container, lParams(matchParent, matchParent))
    }
}

inline fun View.recyclerviewAtViewPager2(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: RecyclerviewAtViewPager2.() -> Unit = {}
): RecyclerviewAtViewPager2 {
    return view({ RecyclerviewAtViewPager2(it) }, id).apply(initView)
}


inline fun View.datePickerView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: DatePickerView.() -> Unit = {}
): DatePickerView {
    return view({ DatePickerView(it) }, id).apply(initView)
}

inline fun View.monthPickerView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: MonthPickerView.() -> Unit = {}
): MonthPickerView {
    return view({ MonthPickerView(it) }, id).apply(initView)
}

inline fun View.nineGridImageView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: NineGridImageView.() -> Unit = {}
): NineGridImageView {
    return view({ NineGridImageView(it) }, id).apply(initView)
}

inline fun View.badgeTextView(
    @IdRes id: Int = View.NO_ID,
    @StyleRes theme: Int = NO_THEME,
    initView: BadgeTextView.() -> Unit = {}
): BadgeTextView {
    return view({ BadgeTextView(it) }, id).apply(initView)
}

