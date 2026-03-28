package cn.a10miaomiao.miao.binding.android.widget

import android.widget.SeekBar
import cn.a10miaomiao.miao.binding.exception.BindingOnlySetException
import cn.a10miaomiao.miao.binding.miaoEffect

inline var SeekBar._progress: Int
    get() { throw BindingOnlySetException() }
    set(value) = miaoEffect(value) {
        progress = it
    }