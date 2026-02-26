package cn.a10miaomiao.bbmiao.page.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.interfaces.v1.CursorItem
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.bbmiao.R
import cn.a10miaomiao.bbmiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.*
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import cn.a10miaomiao.bbmiao.comm.navigation.openSearch
import cn.a10miaomiao.bbmiao.comm.recycler.GridAutofitLayoutManager
import cn.a10miaomiao.bbmiao.comm.recycler._miaoAdapter
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import cn.a10miaomiao.bbmiao.commponents.loading.ListState
import cn.a10miaomiao.bbmiao.commponents.loading.listStateView
import cn.a10miaomiao.bbmiao.commponents.video.videoItem
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.page.video.VideoInfoFragment
import cn.a10miaomiao.bbmiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class HistoryFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "history"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://history")
            argument(MainNavArgs.text) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            }
        }

        fun createArguments(
            keyword: String,
        ): Bundle {
            return bundleOf(
                MainNavArgs.text to keyword,
            )
        }
    }

    override val pageConfig = myPageConfig {
        var searchTitle = "搜索"
        if (viewModel.keyword?.isBlank() == true) {
            title = "历史记录"
        } else {
            title = "搜索\n-\n历史记录\n-\n${viewModel.keyword}"
            searchTitle = "继续搜索"
        }
        search = SearchConfigInfo(
            name = "搜索历史记录",
            keyword = viewModel.keyword ?: "",
        )
        menus = listOf(
            myMenuItem {
                key = MenuKeys.search
                title = searchTitle
                iconResource = R.drawable.ic_search_gray
            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.search -> {
                requireActivity().openSearch(view)
            }
        }
    }

    override fun onSearchSelfPage(context: Context, keyword: String) {
        if (viewModel.keyword.isBlank()) {
            findNavController().navigate(
                HistoryFragment.actionId,
                HistoryFragment.createArguments(keyword,)
            )
        } else {
            viewModel.keyword = keyword
            pageConfig.notifyConfigChanged()
            viewModel.refreshList()
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<HistoryViewModel>(di)

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val nav = Navigation.findNavController(view)
        when(item.business) {
            "archive" -> {
                val args = VideoInfoFragment.createArguments(item.oid.toString())
                Navigation.findNavController(view)
                    .navigate(VideoInfoFragment.actionId, args)
            }
            "pgc" -> {
//                nav.navigateToCompose(BangumiDetailPage()) {
//                    id set item.kid.toString()
//                }
            }
            else -> {
                toast("未知跳转类型")
            }
        }
    }

    private val handleItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("确认删除，喵？")
            setMessage("将历史记录“${item.title}”")
            setNegativeButton("确定") { dialog, which ->
                viewModel.deleteHistory(position)
            }
            setPositiveButton("取消", null)
        }.show()
        true
    }

    val itemUi = miaoBindingItemUi<CursorItem> { item, index ->
        videoItem (
            title = item.title,
            pic = item.cardOgv?.cover
                ?: item.cardUgc?.cover,
            upperName = item.cardUgc?.name,
            remark = NumberUtil.converCTime(item.viewAt),
            duration = NumberUtil.converDuration(
                item.cardOgv?.duration ?: item.cardUgc?.duration ?: 0
            ),
            isHtml = true,
        )
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            backgroundColor = config.windowBackgroundColor
            _miaoLayoutManage(
                GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
            )

            val headerView = frameLayout {
                _topPadding = contentInsets.top
            }
            val footerView = listStateView(
                when {
                    viewModel.triggered -> ListState.NORMAL
                    viewModel.list.loading -> ListState.LOADING
                    viewModel.list.fail -> ListState.FAIL
                    viewModel.list.finished -> ListState.NOMORE
                    else -> ListState.NORMAL
                }
            ) {
                _bottomPadding = contentInsets.bottom
            }
            footerView.layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

            _miaoAdapter(
                items = viewModel.list.data,
                itemUi = itemUi,
            ) {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                setOnItemLongClickListener(handleItemLongClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMore()
                }
                addHeaderView(headerView)
                addFooterView(footerView)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }


}