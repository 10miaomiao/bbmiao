package cn.a10miaomiao.bbmiao.page.player.danmaku

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SendDanmakuParam(
    val aid: String,
    val oid: String,
    val title: String,
) : Parcelable
