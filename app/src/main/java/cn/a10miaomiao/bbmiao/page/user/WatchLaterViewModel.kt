package cn.a10miaomiao.bbmiao.page.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewItemInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class WatchLaterViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    private var nextKey = ""
    var listAsc = false
    var listSortField = 1
    var list = PaginationInfo<ToViewItemInfo>()
    var triggered = false

    init {
        loadData("")
    }

    private fun loadData(
        startKey: String = nextKey,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi
                .videoToview(
                    sortField = listSortField,
                    asc = listAsc,
                    startKey = startKey,
                )
                .awaitCall()
                .json<ResponseData<ToViewInfo>>()
            if (res.code == 0) {
                val data = res.requireData()
                val listData = data.list
                ui.setState {
                    if (startKey.isBlank()) {
                        list.data = mutableListOf()
                    }
                    list.data.addAll(listData)
                    nextKey = data.next_key
                    list.finished = !data.has_more
                }
            } else {
                ui.setState {
                    list.fail = true
                }
                PopTip.show(res.message)
            }
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

    fun deleteVideoHistoryToview(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val item = list.data[position]
        try {
            val res = BiliApiService.userApi
                .videoToviewDels(listOf(item.aid.toString()))
                .awaitCall()
                .json<MessageInfo>()
            if (res.code == 0) {
                withContext(Dispatchers.Main) {
                    PopTip.show("已从稍后再看移出")
                }
                ui.setState {
                    list.data.removeAt(position)
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("失败:$e")
            }
        }
    }

    private fun tryAgainLoadData() {
        loadData()
    }

    fun loadMore() {
        if (!list.finished && !list.loading) {
            loadData(nextKey)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
        }
        loadData("")
    }
}