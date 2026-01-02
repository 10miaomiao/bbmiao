package cn.a10miaomiao.bbmiao.page.video.comment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.store.UserStore
import cn.a10miaomiao.bbmiao.commponents.comment.VideoCommentViewInfo
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class ReplyDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val reply by lazy { fragment.requireArguments().getParcelable<VideoCommentViewInfo>(MainNavArgs.reply)!! }
}