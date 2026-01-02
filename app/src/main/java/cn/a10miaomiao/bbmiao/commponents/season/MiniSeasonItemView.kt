package cn.a10miaomiao.bbmiao.commponents.season

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.bbmiao.comm.MiaoUI
import cn.a10miaomiao.bbmiao.comm._network
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.style.config
import cn.a10miaomiao.bbmiao.widget.rcImageView
import splitties.dimensions.dip
import splitties.views.dsl.constraintlayout.constraintLayout
import splitties.views.dsl.constraintlayout.lParams
import splitties.views.dsl.core.*
import splitties.views.padding

fun MiaoUI.miniSeasonItemView(
    title: String? = null,
    cover: String? = null
): View {
    return verticalLayout {
        padding = dip(5)
        setBackgroundResource(config.selectableItemBackground)

        views {
            // 固定宽高比例 560:746
            +constraintLayout {

                views {
                    +rcImageView {
                        radius = dip(5)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        _network(cover, "@560w_746h")
                    }..lParams {
                        width = 0
                        height = 0
                        leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                        rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                        dimensionRatio = "560:746"
                    }

                }

            }..lParams(matchParent, wrapContent)

            +textView {
                textSize = 16f
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
                setTextColor(config.foregroundColor)
                _text = title ?: ""
            }..lParams {
                verticalMargin = dip(5)
            }
        }


    }
}