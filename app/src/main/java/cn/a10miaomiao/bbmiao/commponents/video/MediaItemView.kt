package cn.a10miaomiao.bbmiao.commponents.video

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.bbmiao.comm.MiaoUI
import cn.a10miaomiao.bbmiao.comm.glide.GlideBlurTransformation
import cn.a10miaomiao.bbmiao.comm.loadImageUrl
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.config.ViewStyle
import cn.a10miaomiao.bbmiao.style.config
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.padding

fun MiaoUI.mediaItemView (
    title: String,
    subtitle: String,
    cover: String? = null,
): View {
    return frameLayout {
        padding = dip(5)
        setBackgroundResource(config.selectableItemBackground)

        views {
            +imageView {
                //            radius = dip(5)
                apply(ViewStyle.roundRect(dip(5)))
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundColor = 0xFF999999.toInt()
                miaoEffect(cover) {
                    if (it != null && it.isNotEmpty()) {
                        Glide.with(context)
                            .loadImageUrl(it, "@672w_378h_1c_")
                            .apply(RequestOptions().transform(GlideBlurTransformation(context, 25f)))
                            .into(this)
                    }
                }
            }..lParams(matchParent, dip(100))

            +verticalLayout {

                gravity = Gravity.CENTER

                views {
                    +textView {
                        setTextColor(Color.WHITE)
                        paint.isFakeBoldText = true
                        gravity = Gravity.CENTER
                        textSize = 16f
                        _text = title
                    }..lParams {
                        bottomMargin = dip(5)
                    }
                    +textView {
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 14f
                        _text = subtitle
                    }
                }

            }..lParams(matchParent, matchParent)
        }



    }
}