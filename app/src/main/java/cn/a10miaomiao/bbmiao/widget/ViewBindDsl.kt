package cn.a10miaomiao.bbmiao.widget

import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.bbmiao.widget.expandabletext.ExpandableTextView


inline fun ExpandableTextView._setContent(value: String) = miaoEffect(value) {
    setContent(it)
}