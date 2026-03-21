package cn.a10miaomiao.bbmiao.page.player.danmaku

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.bbmiao.R
import cn.a10miaomiao.bbmiao.comm._network
import cn.a10miaomiao.bbmiao.comm.delegate.helper.SupportHelper
import cn.a10miaomiao.bbmiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackageInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePanelInfo
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import cn.a10miaomiao.bbmiao.comm.recycler._miaoAdapter
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.recycler.miaoBindingItemUi
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.comm.wrapInNestedScrollView
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.store.WindowStore
import cn.a10miaomiao.miao.binding.android.widget._textColor
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.padding

class SendDanmakuFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "player.danmaku.send"

        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.params) {
                type = NavType.ParcelableType(SendDanmakuParam::class.java)
                nullable = false
            }
        }

        fun createArguments(
            params: SendDanmakuParam,
        ): Bundle {
            return bundleOf(
                MainNavArgs.params to params,
            )
        }

        internal val ID_editText = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        title = "发送弹幕"
        menus = listOf(
            MenuItemPropInfo(
                key = MenuKeys.send,
                iconResource = R.drawable.ic_baseline_send_24,
                title = "发送"
            ),
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.send -> {
                val editText = requireActivity().findViewById<TextInputEditText?>(ID_editText)
                val message = editText.text.toString()
                viewModel.sendDanmaku()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val supportHelper by instance<SupportHelper>()

    private val viewModel by diViewModel<SendDanmakuViewModel>(di)

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
        // 请求焦点到输入框
        requireActivity().findViewById<TextInputEditText?>(ID_editText)?.requestFocus()
        supportHelper.showSoftInput(requireActivity().findViewById<TextInputEditText?>(ID_editText))
    }

    private fun Spinner.onItemChanged(onChanged: (position: Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                onChanged(p2)
            }
        }
    }

    // 发送弹幕按钮点击事件
    private val handleSendClick = View.OnClickListener {
        viewModel.sendDanmaku()
    }

    // 颜色选择器item点击事件
    private val handleColorItemClick = OnItemClickListener { _, _, position ->
        val item = viewModel.danmakuColorList.getOrNull(position) ?: return@OnItemClickListener
        viewModel.setDanmakuTextColorValue(item.value)
    }

    // 颜色选择器的itemUi
    @OptIn(InternalSplittiesApi::class)
    private val colorItemUi = miaoBindingItemUi<SendDanmakuViewModel.SelectItemInfo<Int>> { item, _ ->
        val isSelected = viewModel.danmakuColor == item.value
        verticalLayout {
            gravity = Gravity.CENTER
            padding = dip(4)

            views {
                // 颜色色块
                +view<View> {
                    miaoEffect(listOf(item.value, isSelected)) {
                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = dip(4).toFloat()
                            setColor(0xFF000000.toInt() or item.value)
                            if (isSelected) {
                                setStroke(dip(2), config.themeColor)
                            } else {
                                setStroke(dip(2), config.foregroundAlpha45Color)
                            }
                        }
                        background = drawable
                    }
                }..lParams(dip(48), dip(32))

                // 颜色值文本
                +textView {
                    _text = item.label
                    textSize = 10f
                    _textColor = if (isSelected) config.themeColor else config.foregroundColor
                    gravity = Gravity.CENTER
                }
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        miaoEffect(viewModel.loading) { isLoading ->
            pageConfig.notifyConfigChanged()
        }

        verticalLayout {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom

            views {
                // 1. 输入框
                +view<TextInputLayout> {
                    hint = "弹幕内容"

                    views {
                        +view<TextInputEditText>(ID_editText) {
                            addTextChangedListener { text ->
                                viewModel.setDanmakuTextValue(text.toString())
                            }
                        }..lParams(matchParent, wrapContent)
                    }
                }..lParams(matchParent, wrapContent) {
                    horizontalMargin = config.pagePadding
                    bottomMargin = config.smallPadding
                }

                // 2. 位置选择和大小选择（同一行）
                +horizontalLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    views {
                        // 位置选择
                        +verticalLayout {
                            views {
                                +textView {
                                    text = "位置"
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                }..lParams { bottomMargin = dip(2) }

                                +spinner {
                                    val typeLabels = viewModel.danmakuTypeList.map { it.label }
                                    val adapter = ArrayAdapter(
                                        context,
                                        android.R.layout.simple_spinner_item,
                                        typeLabels
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    this.adapter = adapter
                                    miaoEffect(viewModel.danmakuType) {
                                        val index = viewModel.danmakuTypeList.indexOfFirst { it.value == viewModel.danmakuType }
                                        if (index >= 0) setSelection(index)
                                    }
                                    onItemChanged { position ->
                                        viewModel.setDanmakuTextTypeValue(viewModel.danmakuTypeList[position].value)
                                    }
                                }..lParams(width = wrapContent)
                            }
                        }..lParams(width = wrapContent) {
                            rightMargin = config.pagePadding
                        }

                        // 大小选择
                        +verticalLayout {
                            views {
                                +textView {
                                    text = "大小"
                                    textSize = 12f
                                    setTextColor(config.foregroundAlpha45Color)
                                }..lParams { bottomMargin = dip(2) }

                                +spinner {
                                    val sizeLabels = viewModel.danmakuTextSizeList.map { it.label }
                                    val adapter = ArrayAdapter(
                                        context,
                                        android.R.layout.simple_spinner_item,
                                        sizeLabels
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    this.adapter = adapter
                                    miaoEffect(viewModel.danmakuTextSize) {
                                        val index = viewModel.danmakuTextSizeList.indexOfFirst { it.value == viewModel.danmakuTextSize }
                                        if (index >= 0) setSelection(index)
                                    }
                                    onItemChanged { position ->
                                        viewModel.setDanmakuTextSizeValue(viewModel.danmakuTextSizeList[position].value)
                                    }
                                }..lParams(width = wrapContent)
                            }
                        }..lParams(width = wrapContent)
                    }
                }..lParams(matchParent, wrapContent) {
                    horizontalMargin = config.pagePadding
                    bottomMargin = config.smallPadding
                }

                // 3. 颜色选择（RecyclerView横向滚动）
                +textView {
                    text = "颜色"
                    textSize = 12f
                    setTextColor(config.foregroundAlpha45Color)
                }..lParams {
                    leftMargin = config.pagePadding
                    bottomMargin = dip(4)
                }

                +recyclerView {
                    horizontalPadding = config.pagePadding

                    _miaoLayoutManage(
                        LinearLayoutManager(requireContext()).also {
                            it.orientation = LinearLayoutManager.HORIZONTAL
                        }
                    )
                    _miaoAdapter(
                        items = viewModel.danmakuColorList,
                        itemUi = colorItemUi,
                        depsAry = arrayOf(viewModel.danmakuColor, contentInsets.bottom)
                    ) {
                        setOnItemClickListener(handleColorItemClick)
                    }
                }..lParams(matchParent, wrapContent) {
                    bottomMargin = config.smallPadding
                }

                // 发送按钮
                +view<MaterialButton> {
                    text = "发送弹幕"
                    setTextColor(0xFFFFFFFF.toInt())
                    setOnClickListener(handleSendClick)
                    textSize = 14f
                    padding = 0
                }..lParams(matchParent, wrapContent) {
                    horizontalMargin = config.pagePadding
                }

                // 5. 视频信息提示
                +view<MaterialCardView> {
                    radius = dip(5f)
                    views {
                        +verticalLayout {
                            padding = dip(10)
                            views {
                                +textView {
                                    text = "正在为 \"${viewModel.params.title}\" 发送弹幕"
                                    setTextColor(config.foregroundAlpha45Color)
                                    textSize = 12f
                                }
                            }
                        }
                    }
                }..lParams(matchParent, wrapContent) {
                    horizontalMargin = config.pagePadding
                    verticalMargin = config.smallPadding
                }
            }
        }.wrapInNestedScrollView {

        }
    }
}
