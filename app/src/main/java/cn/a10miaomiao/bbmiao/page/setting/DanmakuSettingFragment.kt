package cn.a10miaomiao.bbmiao.page.setting

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.a10miaomiao.bbmiao.comm.connectStore
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import cn.a10miaomiao.bbmiao.comm.tabLayout
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.widget.wrapInViewPager2Container
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._isChecked
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.appcompat.switch
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent

class DanmakuSettingFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        const val UPDATE_ACTION =
            "com.a10miaomiao.bilimiao.ui.setting.DanmakuSettingFragment.UPDATE"

        override val name = "setting.danmaku"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/danmaku")
            argument(MainNavArgs.index) {
                type = NavType.IntType
                defaultValue = 0
            }
        }

        fun createArguments(
            index: Int,
        ): Bundle {
            return bundleOf(
                MainNavArgs.index to index,
            )
        }

        private val ID_viewPager = View.generateViewId()
        private val ID_tabLayout = View.generateViewId()

        const val MODE_SMALL = 1
        const val MODE_FULL = 2
        const val MODE_PIC_IN_PIC = 3

        const val KEY_DANMAKU_SHOW = "danmaku_show"
        const val KEY_DANMAKU_R2L_SHOW = "danmaku_r2l_show"
        const val KEY_DANMAKU_FT_SHOW = "danmaku_ft_show"
        const val KEY_DANMAKU_FB_SHOW = "danmaku_fb_show"
        const val KEY_DANMAKU_SPECIAL_SHOW = "danmaku_special_show"
        const val KEY_DANMAKU_FONTSIZE = "danmaku_fontsize"
        const val KEY_DANMAKU_TRANSPARENT = "danmaku_transparent"
        const val KEY_DANMAKU_SPEED = "danmaku_speed"
        const val KEY_DANMAKU_TIME_SYNC = "danmaku_time_sync"
        const val KEY_DANMAKU_MAX_LINES = "danmaku_max_lines"
        const val KEY_DANMAKU_R2L_MAX_LINE = "danmaku_r2l_max_line"
        const val KEY_DANMAKU_FT_MAX_LINE = "danmaku_ft_smax_line"
        const val KEY_DANMAKU_FB_MAX_LINE = "danmaku_fb_max_line"
        const val KEY_DANMAKU_SYS_FONT = "danmaku_sys_font"

        fun getPreferences(mode: Int): SettingPreferences.Danmaku {
            return when (mode) {
                MODE_SMALL -> SettingPreferences.DanmakuSmallMode
                MODE_FULL -> SettingPreferences.DanmakuFullMode
                MODE_PIC_IN_PIC -> SettingPreferences.DanmakuPipMode
                else -> SettingPreferences.DanmakuDefault
            }
        }
    }

    override val pageConfig = myPageConfig {
        title = "弹幕设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private var danmakuEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        lifecycleScope.launch {
            requireContext().dataStore.data.map {
                it[SettingPreferences.DanmakuEnable]
            }.collect {
                setDanmakuEnabled(it ?: true)
            }
        }
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(ID_tabLayout)
        val viewPager = view.findViewById<ViewPager2>(ID_viewPager)
        val modeList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                Triple(
                    MODE_SMALL,
                    "小屏模式",
                    DanmakuSettingDetailFragment.newInstance(MODE_SMALL)
                ),
                Triple(
                    MODE_FULL,
                    "全屏模式",
                    DanmakuSettingDetailFragment.newInstance(MODE_FULL)
                ),
                Triple(
                    MODE_PIC_IN_PIC,
                    "小窗(画中画)模式",
                    DanmakuSettingDetailFragment.newInstance(MODE_PIC_IN_PIC)
                )
            )
        } else {
            listOf(
                Triple(
                    MODE_SMALL,
                    "小屏模式",
                    DanmakuSettingDetailFragment.newInstance(MODE_SMALL)
                ),
                Triple(
                    MODE_FULL,
                    "全屏模式",
                    DanmakuSettingDetailFragment.newInstance(MODE_FULL)
                ),
            )
        }

        if (viewPager.adapter == null) {
            val mAdapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun getItemCount() = modeList.size
                override fun createFragment(position: Int): Fragment {
                    return modeList[position].third
                }
            }
            viewPager.adapter = mAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = modeList[position].second
            }.attach()

            val tabIndex = requireArguments().getInt(MainNavArgs.index)
            viewPager.setCurrentItem(tabIndex, false)
            tabLayout.getTabAt(tabIndex)?.select()
        }
    }

    private fun setDanmakuEnabled(enabled: Boolean) {
        ui.setState {
            danmakuEnabled = enabled
        }
    }

    private val handleCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            setDanmakuEnabled(isChecked)
        }

    @OptIn(InternalSplittiesApi::class)
    @SuppressLint("ResourceType")
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout {
            _bottomPadding = contentInsets.bottom

            views {
                +verticalLayout {
                    views {
                        +tabLayout(ID_tabLayout) {
                            _topPadding = contentInsets.top
                            _leftPadding = contentInsets.left
                            _rightPadding = contentInsets.right
                            tabMode = TabLayout.MODE_SCROLLABLE
                        }..lParams(matchParent, wrapContent)
                        +view<ViewPager2>(ID_viewPager) {
                            _leftPadding = contentInsets.left
                            _rightPadding = contentInsets.right
                            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                            isSaveEnabled = false
                        }.wrapInViewPager2Container {
                        }..lParams(matchParent, matchParent) {
                            weight = 1f
                        }
                    }
                }

                +frameLayout {
                    _show = !danmakuEnabled
                    backgroundColor = config.blockBackgroundAlpha45Color
                    setOnClickListener { }

                    views {
                        +switch {
                            text = "弹幕功能已关闭"
                            _isChecked = danmakuEnabled
                            setOnCheckedChangeListener(handleCheckedChangeListener)
                        }..lParams {
                            gravity = Gravity.CENTER
                        }
                    }
                }..lParams(matchParent, matchParent)
            }
        }

    }

}