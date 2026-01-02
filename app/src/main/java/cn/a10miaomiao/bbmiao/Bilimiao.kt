package cn.a10miaomiao.bbmiao

import android.app.Application
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import cn.a10miaomiao.bbmiao.comm.delegate.theme.ThemeDelegate
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory


class Bilimiao: Application() {

    companion object {
        const val APP_NAME = "bilimiao"
        lateinit var app: Bilimiao
        lateinit var commApp: BilimiaoCommApp
    }

    init {
        app = this
        commApp = BilimiaoCommApp(this)
    }

    override fun onCreate() {
        super.onCreate()
        AppCrashHandler.getInstance(this)
        ThemeDelegate.setNightMode(this)
        Mojito.initialize(
            GlideImageLoader.with(this),
            SketchImageLoadFactory()
        )
        commApp.onCreate()
    }

}