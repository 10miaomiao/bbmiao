package cn.a10miaomiao.bbmiao.page.setting

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.bbmiao.comm.connectStore
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.store.WindowStore
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent
import splitties.views.textColorResource
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._progress
import cn.a10miaomiao.miao.binding.android.widget._text
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.seekBar
import splitties.views.dsl.core.verticalMargin
import splitties.views.dsl.core.view

class AutoStopTimerFragment : Fragment(), DIAware, MyPage, SeekBar.OnSeekBarChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting.autoStopTimer"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/autoStopTimer")
        }
    }

    override val pageConfig = myPageConfig {
        title = "定时关闭"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()
    private val playerStore by instance<PlayerStore>()

    private var currentDuration = 0
    private var isUserSeeking = false

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

        // 观察当前定时关闭时间
        lifecycleScope.launch {
            playerStore.autoStopDurationFlow.collectLatest { duration ->
                ui.setState {
                    currentDuration = duration
                }
            }
        }
    }

    private fun formatDuration(seconds: Int): String {
        return if (seconds == 0) {
            "已关闭"
        } else {
            val minute = seconds / 60
            val second = seconds % 60
            if (minute == 0) {
                "${second}秒"
            } else if (second == 0) {
                "${minute}分钟"
            } else {
                "${minute}分${second}秒"
            }
        }
    }

    private fun setDuration(seconds: Int) {
        playerStore.setAutoStopDuration(seconds)
    }


    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)

        frameLayout {
            _leftPadding = contentInsets.left
            _topPadding = contentInsets.top
            _rightPadding = contentInsets.right
            _bottomPadding = contentInsets.bottom

            backgroundColor = config.windowBackgroundColor

            views {
                +verticalLayout {
                    gravity = Gravity.CENTER_HORIZONTAL
                    views {
                        +textView {
                            _text = formatDuration(currentDuration)
                            textSize = 32f
                            setTextColor(config.foregroundColor)
                        }..lParams {
                            gravity = Gravity.CENTER
                            verticalMargin = dip(24)
                        }

                        // 滑块
                        +seekBar {
                            max = 3600
                            _progress = currentDuration
                            setOnSeekBarChangeListener(this@AutoStopTimerFragment)
                        }..lParams(matchParent, wrapContent) {
                            topMargin = dip(16)
                            leftMargin = dip(24)
                            rightMargin = dip(24)
                        }

                        // 快捷按钮区域
                        +horizontalLayout {
                            gravity = Gravity.CENTER

                            views {

                                +view<MaterialButton>() {
                                    text = "关闭"
                                    setOnClickListener { setDuration(0) }
                                    primaryStyle()
                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }

                                +view<MaterialButton>() {
                                    text = "15分钟"
                                    plainStyle()
                                    setOnClickListener { setDuration(15 * 60) }

                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }

                                +view<MaterialButton>() {
                                    text = "30分钟"
                                    plainStyle()
                                    setOnClickListener { setDuration(30 * 60) }
                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }
                            }
                        }..lParams(matchParent, wrapContent) {
                            topMargin = dip(24)
                        }

                        +horizontalLayout {
                            gravity = Gravity.CENTER

                            views {
                                +view<MaterialButton>() {
                                    text = "45分钟"
                                    plainStyle()
                                    setOnClickListener { setDuration(45 * 60) }
                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }

                                +view<MaterialButton>() {
                                    text = "1小时"
                                    plainStyle()
                                    setOnClickListener { setDuration(60 * 60) }
                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }

                                +view<MaterialButton>() {
                                    text = "2小时"
                                    plainStyle()
                                    setOnClickListener { setDuration(2 * 60 * 60) }
                                }..lParams(wrapContent, wrapContent) {
                                    leftMargin = dip(4)
                                    rightMargin = dip(4)
                                }
                            }

                        }..lParams(matchParent, wrapContent) {
                            topMargin = dip(8)
                        }
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    private fun MaterialButton.plainStyle() {
        cornerRadius = dip(10)
        backgroundColor = config.blockBackgroundColor
        textColorResource = config.themeColorResource
        strokeColor = ColorStateList.valueOf(config.themeColor)
        strokeWidth = dip(1.5f).toInt()
    }


    private fun MaterialButton.primaryStyle() {
        cornerRadius = dip(10)
        setTextColor(0xFFFFFFFF.toInt())
    }

    override fun onProgressChanged(
        seekBar: SeekBar?,
        progress: Int,
        fromUser: Boolean
    ) {
        if (fromUser) {
            ui.setState {
                currentDuration = progress
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        isUserSeeking = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        isUserSeeking = false
        seekBar?.let {
            setDuration(it.progress)
        }
    }
}