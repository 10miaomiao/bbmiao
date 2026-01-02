package cn.a10miaomiao.bbmiao.comm.delegate.player

import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.a10miaomiao.bbmiao.R
import cn.a10miaomiao.bbmiao.widget.player.DanmakuVideoPlayer
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

class PlayerViews(
    private var activity: AppCompatActivity,
) {

    val videoPlayer = activity.findViewById<DanmakuVideoPlayer>(R.id.video_player)
}