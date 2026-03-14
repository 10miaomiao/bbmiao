package cn.a10miaomiao.bbmiao.page.search.result

import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.pagination.Pagination
import bilibili.polymer.app.search.v1.Item
import bilibili.polymer.app.search.v1.Item.CardItem
import bilibili.polymer.app.search.v1.SearchAllRequest
import bilibili.polymer.app.search.v1.SearchGRPC
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import cn.a10miaomiao.bbmiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoResultViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text, "") }

    var list = PaginationInfo<Item>()
    var triggered = false
    private var _next = ""


    val rankOrdersMenus = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
        CheckPopupMenu.MenuItemInfo("默认排序", 0),
        CheckPopupMenu.MenuItemInfo("新发布", 2),
        CheckPopupMenu.MenuItemInfo("播放多", 1),
        CheckPopupMenu.MenuItemInfo("弹幕多", 3),
    )
    var rankOrder = rankOrdersMenus[0]

    val durationMenus = listOf<CheckPopupMenu.MenuItemInfo<Int>>(
        CheckPopupMenu.MenuItemInfo("全部时长", 0),
        CheckPopupMenu.MenuItemInfo("0-10分钟", 1),
        CheckPopupMenu.MenuItemInfo("10-30分钟", 2),
        CheckPopupMenu.MenuItemInfo("30-60分钟", 3),
        CheckPopupMenu.MenuItemInfo("60分钟+", 4),
    )
    var duration = durationMenus[0]

    var regionId = 0
    var regionName = "全部分区"

    init {
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum,
        next: String = _next
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }

            val durationList = when (duration.value) {
                1 -> listOf(1)
                2 -> listOf(2)
                3 -> listOf(3)
                4 -> listOf(4)
                else -> listOf()
            }.joinToString(",")

            val req = SearchAllRequest(
                keyword = keyword,
                order = rankOrder.value,
                durationList = durationList,
                tidList = if (regionId > 0) regionId.toString() else "",
                pagination = Pagination(
                    pageSize = list.pageSize,
                    next = next
                )
            )
            val result = BiliGRPCHttp.request {
                SearchGRPC.searchAll(req)
            }.awaitCall()

            // 过滤只保留视频(稿件)类型
            val itemList = result.item.filter { it.cardItem is CardItem.Av }
            _next = result.pagination?.next ?: ""
            val isFinished = itemList.isEmpty() || _next.isBlank()

            ui.setState {
                list.finished = isFinished
                if (pageNum == 1) {
                    list.data = arrayListOf()
                }
                list.data.addAll(itemList)
            }
            list.pageNum = pageNum
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
        } finally {
            ui.setState {
                list.loading = false
                triggered = false
            }
        }
    }

    fun loadMore () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(
                pageNum = pageNum + 1
            )
        }
    }

    fun refreshList() {
        _next = ""
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData()
        }
    }


}