package cn.a10miaomiao.bbmiao.widget.scaffold.ui

import cn.a10miaomiao.bbmiao.widget.scaffold.AppBarView
import splitties.views.dsl.core.Ui

interface AppBarUi : Ui {
    fun setProp(prop: AppBarView.PropInfo?)

    fun updateTheme()
}