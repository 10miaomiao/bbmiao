package cn.a10miaomiao.bbmiao.page.search.result

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.pagination.Pagination
import bilibili.polymer.app.search.v1.Item
import bilibili.polymer.app.search.v1.SearchByTypeRequest
import bilibili.polymer.app.search.v1.SearchGRPC
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class BangumiResultViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text, "") }

    var list = PaginationInfo<Item>()
    var triggered = false
    private var _next = ""

    companion object {
        private const val SEARCH_TYPE_BANGUMI = 7
    }

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

            val req = SearchByTypeRequest(
                keyword = keyword,
                type = SEARCH_TYPE_BANGUMI,
                pagination = Pagination(
                    pageSize = list.pageSize,
                    next = next
                )
            )
            val result = BiliGRPCHttp.request {
                SearchGRPC.searchByType(req)
            }.awaitCall()

            val itemList = result.items
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