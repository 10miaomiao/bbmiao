package cn.a10miaomiao.bbmiao.page.search.result

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.bbmiao.comm.*
import bilibili.polymer.app.search.v1.Item
import bilibili.polymer.app.search.v1.Item.CardItem
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import cn.a10miaomiao.bbmiao.comm.recycler.GridAutofitLayoutManager
import cn.a10miaomiao.bbmiao.comm.recycler._miaoAdapter
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import cn.a10miaomiao.bbmiao.commponents.loading.ListState
import cn.a10miaomiao.bbmiao.commponents.loading.listStateView
import cn.a10miaomiao.bbmiao.commponents.upper.upperItem
import cn.a10miaomiao.bbmiao.page.user.UserFragment
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class UpperResultFragment : BaseResultFragment(), DIAware {

    companion object {
        fun newInstance(text: String?): UpperResultFragment {
            val fragment = UpperResultFragment()
            val bundle = Bundle()
            bundle.putString(MainNavArgs.text, text)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val title = "UP主"

    override val menus get() = listOf<MenuItemPropInfo>(

    )

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {

    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<UpperResultViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun refreshList() {
        if (!viewModel.list.loading) {
            viewModel.refreshList()
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        // 从 URI 中提取用户 ID，如 https://space.bilibili.com/xxxxx
        val uri = Uri.parse(item.uri)
        val pathSegments = uri.pathSegments
        val userId = if (pathSegments.isNotEmpty()) pathSegments.last() else ""
        val args = UserFragment.createArguments(userId)
        Navigation.findNavController(view)
            .navigate(UserFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<Item> { item, index ->
        val cardItem = item.cardItem
        if (cardItem is CardItem.Author) {
            val authorItem = cardItem.value
            upperItem (
                name = authorItem.title,
                face = authorItem.cover,
                remarks = "粉丝：${NumberUtil.converString(authorItem.fans)}   视频数：${NumberUtil.converString(authorItem.archives)}",
                sign = authorItem.sign,
            ).apply {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            }
        } else {
            upperItem(name = "", face = "", remarks = "", sign = "")
        }
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            views {
                +recyclerView {
                    backgroundColor = config.windowBackgroundColor
                    mLayoutManager = _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
                    )

                    val footerView = listStateView(
                        when {
                            viewModel.triggered -> ListState.NORMAL
                            viewModel.list.loading -> ListState.LOADING
                            viewModel.list.fail -> ListState.FAIL
                            viewModel.list.finished -> ListState.NOMORE
                            else -> ListState.NORMAL
                        }
                    ).apply {
                        _bottomPadding = contentInsets.bottom
                    }

                    _miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMore()
                        }
                        addFooterView(footerView)
                    }
                }.wrapInSwipeRefreshLayout {
                    setColorSchemeResources(config.themeColorResource)
                    setOnRefreshListener(handleRefresh)
                    _isRefreshing = viewModel.triggered
                }..lParams(matchParent, matchParent)
            }
        }
    }

}