package cn.a10miaomiao.bbmiao.comm.player

import cn.a10miaomiao.bbmiao.widget.player.media3.Media3ExoPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory

object BilimiaoPlayerManager {

    fun initConfig() {
        PlayerFactory.setPlayManager(Media3ExoPlayerManager::class.java)
    }

}