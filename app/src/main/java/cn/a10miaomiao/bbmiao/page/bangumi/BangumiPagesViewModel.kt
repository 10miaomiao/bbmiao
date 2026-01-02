package cn.a10miaomiao.bbmiao.page.bangumi

import android.content.Context
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class BangumiPagesViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

}