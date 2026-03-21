package cn.a10miaomiao.bbmiao.page.player.danmaku

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SendDanmakuViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val params by lazy {
        fragment.requireArguments().getParcelable<SendDanmakuParam>(MainNavArgs.params)!!
    }

    var loading = false
        private set
    var danmakuType = 1
        private set
    var danmakuText = ""
        private set
    var danmakuColor = 0xFFFFFF
        private set
    var danmakuTextSize = 25f
        private set

    val danmakuTypeList = listOf(
        SelectItemInfo("滚动", 1),
        SelectItemInfo("顶部", 5),
        SelectItemInfo("底部", 4)
    )

    val danmakuColorList = mutableListOf(
        SelectItemInfo("#FFFFFF", 0xFFFFFF),
        SelectItemInfo("#FE0302", 0xFE0302),
        SelectItemInfo("#FF7204", 0xFF7204),
        SelectItemInfo("#FFAA02", 0xFFAA02),
        SelectItemInfo("#FFD302", 0xFFD302),
        SelectItemInfo("#FFFF00", 0xFFFF00),
        SelectItemInfo("#A0EE00", 0xA0EE00),
        SelectItemInfo("#00CD00", 0x00CD00),
        SelectItemInfo("#019899", 0x019899),
        SelectItemInfo("#4266BE", 0x4266BE),
        SelectItemInfo("#89D5FF", 0x89D5FF),
        SelectItemInfo("#CC0273", 0xCC0273),
        SelectItemInfo("#222222", 0x222222),
        SelectItemInfo("#9B9B9B", 0x9B9B9B),
    )

    val danmakuTextSizeList = listOf(
        SelectItemInfo("默认", 25f),
        SelectItemInfo("较小", 18f),
    )

    fun setDanmakuTextTypeValue(value: Int) {
        ui.setState {
            danmakuType = value
        }
    }

    fun setDanmakuTextValue(value: String) {
        ui.setState {
            danmakuText = value
        }
    }

    fun setDanmakuTextColorValue(value: Int) {
        ui.setState {
            danmakuColor = value
        }
    }

    fun setDanmakuTextSizeValue(value: Float) {
        ui.setState {
            danmakuTextSize = value
        }
    }

    fun sendDanmaku() {
        val text = danmakuText.replace("\n", " ")
        if (text.isBlank()) {
            TipDialog.show("请输入弹幕内容", WaitDialog.TYPE.WARNING)
            return
        }
        if (text.length > 50) {
            TipDialog.show("弹幕内容字数过多", WaitDialog.TYPE.WARNING)
            return
        }

        val type = danmakuType
        val color = danmakuColor
        val textSize = danmakuTextSize

        val currentPosition = basePlayerDelegate.currentPosition()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    ui.setState {
                        loading = true
                    }
                    WaitDialog.show("发送中")
                }
                val res = BiliApiService.playerAPI.sendDamaku(
                    aid = params.aid,
                    oid = params.oid,
                    msg = text,
                    mode = type,
                    fontsize = textSize.toInt(),
                    color = color,
                    progress = currentPosition,
                ).awaitCall().json<MessageInfo>()
                withContext(Dispatchers.Main) {
                    if (res.isSuccess) {
                        TipDialog.show("发送成功", WaitDialog.TYPE.SUCCESS)
                        basePlayerDelegate.sendDanmaku(
                            type,
                            text,
                            textSize,
                            color,
                            currentPosition
                        )
                        fragment.findNavController().popBackStack()
                    } else {
                        TipDialog.show(res.message, WaitDialog.TYPE.WARNING)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    TipDialog.show(e.message ?: e.toString(), WaitDialog.TYPE.ERROR)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    ui.setState {
                        loading = false
                    }
                    WaitDialog.dismiss()
                }
            }
        }
    }

    data class SelectItemInfo<T>(
        val label: String,
        val value: T,
    )
}
