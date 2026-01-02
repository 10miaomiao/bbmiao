package cn.a10miaomiao.bbmiao.comm.recycler

import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.bbmiao.comm.MiaoUI

class RecyclerViews(
    private val recyclerView: RecyclerView,
    private val adapter: MiaoBindingAdapter<*>,
    private val type: Int,
    private val isRecord: Boolean,
): MiaoUI.ViewsInfo(recyclerView, isRecord) {
    override fun bindViews() {
        if (type == 0) {
            views.forEach {
                adapter.addHeaderView(it)
            }
        } else if (type == 1) {
            views.forEach {
                adapter.addFooterView(it)
            }
        }
    }
}

