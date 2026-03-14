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
import cn.a10miaomiao.bbmiao.comm.recycler.*
import cn.a10miaomiao.bbmiao.commponents.bangumi.bangumiItem
import cn.a10miaomiao.bbmiao.page.bangumi.BangumiDetailFragment
import cn.a10miaomiao.bbmiao.commponents.loading.ListState
import cn.a10miaomiao.bbmiao.commponents.loading.listStateView
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

class BangumiResultFragment : BaseResultFragment(), DIAware {

    companion object {
        fun newInstance(text: String?): BangumiResultFragment {
            val fragment = BangumiResultFragment()
            val bundle = Bundle()
            bundle.putString(MainNavArgs.text, text)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val title = "番剧"

    override val menus get() = listOf<MenuItemPropInfo>(

    )

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {

    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<BangumiResultViewModel>(di)

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
        // 从 URI 中提取番剧 ID，如 https://www.bilibili.com/bangumi/play/ssxxxx 或 epxxxx
        val uri = Uri.parse(item.uri)
        val pathSegments = uri.pathSegments
        val bangumiId = if (pathSegments.isNotEmpty()) pathSegments.last() else ""
        val args = BangumiDetailFragment.createArguments(bangumiId)
        Navigation.findNavController(view)
            .navigate(BangumiDetailFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<Item> { item, index ->
        val cardItem = item.cardItem
        if (cardItem is CardItem.Bangumi) {
            val bangumiItem = cardItem.value
            bangumiItem (
                title = bangumiItem.title,
                cover = bangumiItem.cover,
                desc = bangumiItem.label,
                statusText = bangumiItem.styles,
                isHtml = true,
            ).apply {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            }
        } else {
            bangumiItem(title = "", cover = "", desc = "", statusText = "", isHtml = true)
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