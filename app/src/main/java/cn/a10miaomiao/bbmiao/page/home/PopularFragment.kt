package cn.a10miaomiao.bbmiao.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.card.v1.SmallCoverV5
import bilibili.app.show.v1.EntranceShow
import cn.a10miaomiao.miao.binding.android.view._bottomMargin
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.bbmiao.comm.*
import cn.a10miaomiao.bbmiao.comm.recycler.*
import cn.a10miaomiao.bbmiao.commponents.loading.ListState
import cn.a10miaomiao.bbmiao.commponents.loading.listStateView
import cn.a10miaomiao.bbmiao.commponents.video.videoItem
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.page.web.WebFragment
import cn.a10miaomiao.bbmiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import cn.a10miaomiao.bbmiao.widget.recyclerviewAtViewPager2
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityCenter
import splitties.views.verticalPadding

class PopularFragment: RecyclerViewFragment(), DIAware {

    companion object {
        fun newFragmentInstance(): PopularFragment {
            val fragment = PopularFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<PopularViewModel>(di)

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

    private val handleTopEntranceItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.topEntranceList[position]
        val url = item.uri
        if (url.indexOf("bilibili://") == 0) {
            BiliNavigation.navigationTo(view, url)
        } else {
            val args = WebFragment.createArguments(url)
            findNavController().navigate(
                WebFragment.actionId,
                args,
            )
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = VideoInfoFragment.createArguments(item.base!!.param)
        Navigation.findNavController(view)
            .navigate(VideoInfoFragment.actionId, args)
    }

    val topEntranceItemUi = miaoBindingItemUi<EntranceShow> { item, index ->
        verticalLayout {
            layoutParams = ViewGroup.MarginLayoutParams(
                dip(100), wrapContent
            )
            verticalPadding = config.pagePadding
            gravity = gravityCenter
            setBackgroundResource(config.selectableItemBackground)
            views {
                +imageView {
                    _network(item.icon)
                }..lParams {
                    width = dip(40)
                    height = dip(40)
                    bottomMargin = config.dividerSize
                }
                +textView {
                    _text = item.title
                    gravity = gravityCenter
                }
            }
        }
    }

    val itemUi = miaoBindingItemUi<SmallCoverV5> { item, index ->
        videoItem (
            title = item.base?.title,
            pic =item.base?.cover,
            upperName = item.rightDesc1,
            remark = item.rightDesc2,
            duration = item.coverRightText1,
        )
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
//            _leftPadding = contentInsets.left
//            _rightPadding = contentInsets.right
//            _topPadding = config.pagePadding
//            _bottomPadding = contentInsets.bottom

            views {
                +recyclerviewAtViewPager2 {
                    backgroundColor = config.windowBackgroundColor
                    scrollBarSize = 0
                    mLayoutManager = _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
                    )

                    val mAdapter = _miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMode()
                        }
                    }
                    headerViews(mAdapter) {
                        +recyclerView {
                            layoutManager = LinearLayoutManager(requireContext()).apply {
                                orientation = LinearLayoutManager.HORIZONTAL
                            }
                            scrollBarSize = 0
                            _miaoAdapter(
                                items = viewModel.topEntranceList,
                                itemUi = topEntranceItemUi,
                            ) {
                                setOnItemClickListener(handleTopEntranceItemClick)
                            }
                        }..lParams(matchParent, wrapContent)
                    }
                    footerViews(mAdapter) {
                        +listStateView(
                            when {
                                viewModel.triggered -> ListState.NORMAL
                                viewModel.list.loading -> ListState.LOADING
                                viewModel.list.fail -> ListState.FAIL
                                viewModel.list.finished -> ListState.NOMORE
                                else -> ListState.NORMAL
                            },
                            viewModel::tryAgainLoadData
                        )..lParams(matchParent, wrapContent) {
                            _bottomMargin = contentInsets.bottom
                        }
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