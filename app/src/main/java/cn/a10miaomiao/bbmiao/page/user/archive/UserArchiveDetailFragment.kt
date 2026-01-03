package cn.a10miaomiao.bbmiao.page.user.archive

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.bbmiao.R
import cn.a10miaomiao.bbmiao.comm._isRefreshing
import cn.a10miaomiao.bbmiao.comm.connectUi
import cn.a10miaomiao.bbmiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import cn.a10miaomiao.bbmiao.comm.navigation.openSearch
import cn.a10miaomiao.bbmiao.comm.recycler.GridAutofitLayoutManager
import cn.a10miaomiao.bbmiao.comm.recycler._miaoAdapter
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import cn.a10miaomiao.bbmiao.comm.wrapInSwipeRefreshLayout
import cn.a10miaomiao.bbmiao.commponents.loading.ListState
import cn.a10miaomiao.bbmiao.commponents.loading.listStateView
import cn.a10miaomiao.bbmiao.commponents.video.videoItem
import cn.a10miaomiao.bbmiao.page.search.result.BaseResultFragment
import cn.a10miaomiao.bbmiao.page.video.VideoInfoFragment
import cn.a10miaomiao.bbmiao.style.config
import com.a10miaomiao.bilimiao.store.WindowStore
import cn.a10miaomiao.bbmiao.widget.menu.CheckPopupMenu
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class UserArchiveDetailFragment : BaseResultFragment(), DIAware {

    companion object {
        fun newInstance(
            id: String,
            name: String,
        ): UserArchiveDetailFragment {
            val fragment = UserArchiveDetailFragment()
            val bundle = bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
            )
            fragment.arguments = bundle
            return fragment
        }
    }
    override val title get() = "全部投稿"
    override val menus get() = listOf(
        myMenuItem {
            key = MenuKeys.filter
            title = viewModel.rankOrder.title
            iconResource = R.drawable.ic_baseline_filter_list_grey_24
        },
    )

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when (menuItem.key) {
            MenuKeys.region -> {
                val pm = CheckPopupMenu(
                    context = requireActivity(),
                    anchor = view,
                    menus = viewModel.regionList,
                    value = viewModel.region.value,
                )
                pm.onMenuItemClick = {
                    viewModel.region = it
                    viewModel.refreshList()
                    notifyConfigChanged()
                }
                pm.show()
            }

            MenuKeys.filter -> {
                val pm = CheckPopupMenu(
                    context = requireActivity(),
                    anchor = view,
                    menus = viewModel.rankOrderList,
                    value = viewModel.rankOrder.value,
                )
                pm.onMenuItemClick = {
                    viewModel.rankOrder = it
                    viewModel.refreshList()
                    notifyConfigChanged()
                }
                pm.show()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<UserArchiveDetailViewModel>(di)

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
        val args = VideoInfoFragment.createArguments(item.param)
        Navigation.findNavController(view)
            .navigate(VideoInfoFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<ArchiveInfo> { item, index ->
        videoItem(
            title = item.title,
            pic = item.cover,
            remark = NumberUtil.converCTime(item.ctime),
            playNum = item.play,
            damukuNum = item.danmaku,
            duration = NumberUtil.converDuration(item.duration),
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

//            val headerView = frameLayout {
//                _topPadding = contentInsets.top
//            }
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
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMore()
                }
//                addHeaderView(headerView)
                addFooterView(footerView)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }

}