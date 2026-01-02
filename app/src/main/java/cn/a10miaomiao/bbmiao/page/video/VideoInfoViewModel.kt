package cn.a10miaomiao.bbmiao.page.video

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewReq
import cn.a10miaomiao.bbmiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import cn.a10miaomiao.bbmiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import cn.a10miaomiao.bbmiao.widget.scaffold.getScaffoldView
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserLibraryStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoInfoViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: Activity by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val basePlayerDelegate by instance<BasePlayerDelegate>()
    val scaffoldApp by lazy { activity.getScaffoldView() }

    //    val type by lazy { fragment.requireArguments().getString(MainNavArgs.type, "AV") }
    var id: String = ""

    var bvId: String = ""
    var arcInfo: bilibili.app.archive.v1.Arc? = null
    var reqUserInfo: bilibili.app.view.v1.ReqUser? = null
    var historyInfo: bilibili.app.view.v1.History? = null
    var staffs = mutableListOf<bilibili.app.view.v1.Staff>()
    var relates = mutableListOf<bilibili.app.view.v1.Relate>()
    var pages = mutableListOf<bilibili.app.archive.v1.Page>()
    var tags = mutableListOf<bilibili.app.view.v1.Tag>()
    var ugcSeason: UgcSeasonInfo? = null
    var ugcSeasonEpisodes = mutableListOf<Any>() // UgcSeasonInfo | UgcEpisodeInfo
//    var staffs = mutableListOf<VideoStaffInfo>()
//    var tags = mutableListOf<VideoTagInfo>()

    var loading = false
    var loadState = LoadMoreStatus.Loading
    // 自动连播合集
    var isAutoPlaySeason = true

    var state = ""

    val userStore: UserStore by instance()

    val filterStore: FilterStore by instance()

    val playerStore: PlayerStore by instance()

    val playListStore: PlayListStore by instance()

    init {
        val arguments = fragment.requireArguments()
        id = arguments.getString(MainNavArgs.id, "")
        loadData()
    }

    fun changeVideo(aid: String) {
        if (aid == id) {
            return
        }
        ui.setState {
            id = aid
            loadData()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                state = ""
                loading = true
            }
            val req = if (id.startsWith("BV")) {
                ViewReq(
                    bvid = id,
                )
            } else {
                ViewReq(
                    aid = id.toLong(),
                )
            }
            val res = BiliGRPCHttp.request {
                ViewGRPC.view(req)
            }.awaitCall()
            ui.setState {
                bvId = res.bvid
                arcInfo = res.arc ?: res.activitySeason?.arc
                staffs = res.staff.toMutableList()
                historyInfo = res.history
                pages = res.pages.mapNotNull {
                    it.page
                }.toMutableList()
                relates = res.relates.toMutableList()
                tags = res.tag.toMutableList()
                reqUserInfo = res.reqUser ?: res.activitySeason?.reqUser
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                state = "无法连接到御坂网络"
            }
        } finally {
            ui.setState { loading = false }
        }
    }

    private fun autoStartPlay(info: bilibili.app.archive.v1.Arc) = viewModelScope.launch(Dispatchers.Main) {
        if (basePlayerDelegate.getSourceIds().aid == info.aid.toString()) {
            // 同个视频不替换播放
            return@launch
        }
        val openMode = SettingPreferences.mapData(activity) {
            it[PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
        }
        if (scaffoldApp.showPlayer) {
            if (basePlayerDelegate.isPlaying()) {
                // 自动替换正在播放的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE != 0) {
                    playVideo(info, 0)
                }
            } else if (basePlayerDelegate.isPause()) {
                // 自动替换暂停的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE != 0) {
                    playVideo(info, 0)
                }
            } else {
                // 自动替换完成的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE != 0) {
                    playVideo(info, 0)
                }
            }
        } else {
            // 自动播放新视频
            if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_PLAY != 0) {
                playVideo(info, 0)
            }
        }
    }

    fun playVideo(info: bilibili.app.archive.v1.Arc, page: Int) {
        if (page in pages.indices){
            playVideo(info, pages[page])
        }
    }

    fun playVideo(info: bilibili.app.archive.v1.Arc, page: bilibili.app.archive.v1.Page) {
        val videoPages = pages
        val title = if (videoPages.size > 1) {
            page.part
        } else {
            info.title
        }
        val cid = page.cid.toString()
        val season = ugcSeason
        if (isAutoPlaySeason && season != null) {
            // 将合集加入播放列表
            val playListFromId = (playListStore.state.from as? PlayListFrom.Season)?.seasonId
                ?: (playListStore.state.from as? PlayListFrom.Section)?.seasonId
            if (playListFromId != season.id ||
                !playListStore.state.inListForAid(info.aid.toString())) {
                // 当前播放列表来源不是当前合集或视频不在播放列表中时，创建新播放列表
                // 以合集创建播放列表
                val index = if (season.sections.size > 1) {
                    season.sections.indexOfFirst { section ->
                        section.episodes.indexOfFirst { it.aid == info.aid.toString() } != -1
                    }
                } else { 0 }
                playListStore.setPlayList(season, index)
            }
        } else if (!playListStore.state.inListForAid(info.aid.toString())) {
            // 当前视频不在播放列表中时，如果未正在播放或播放列表为空则创建新的播放列表，否则将视频加入列表尾部
            if (playListStore.state.items.isEmpty()
                || playerStore.state.aid.isEmpty()) {
                // 以当前视频创建新的播放列表
                val playListItem = playListStore.run {
                    info.toPlayListItem(pages)
                }
                playListStore.setPlayList(
                    name = info.title,
                    from = playListItem.from,
                    items = listOf(
                        playListItem,
                    )
                )
            } else {
                // 将视频添加到播放列表末尾
                playListStore.addItem(playListStore.run {
                    info.toPlayListItem(pages)
                })
            }
        }

        // 播放视频
        basePlayerDelegate.openPlayer(
            VideoPlayerSource(
                mainTitle = info.title,
                title = title,
                coverUrl = info.pic,
                aid = info.aid.toString(),
                id = cid,
                ownerId = info.author?.mid.toString(),
                ownerName = info.author?.name ?: "",
            ).apply {
                pages = videoPages.map {
                    VideoPlayerSource.PageInfo(
                        cid = it.cid.toString(),
                        title = it.part,
                    )
                }
            }
        )
    }

    /**
     * 跳转番剧
     */
    private fun jumpSeason(info: bilibili.app.archive.v1.Arc): Boolean {
//        info.season?.let {
//            if (it.is_jump == 1) {
//                val nav = fragment.findNavController()
//                val previousId = nav.previousBackStackEntry?.destination?.id
//                nav.navigateToCompose(
//                    BangumiDetailPage(),
//                    navOptions {
//                        previousId?.let(::popUpTo)
//                    }
//                ) {
//                    id set it.season_id
//                }
//                return true
//            }
//        }
        return false
    }

    private fun updateLikeState(state: Int) {
        var videoArc = arcInfo ?: return
        var reqUser = reqUserInfo ?: return
        val stat = videoArc.stat ?: return
        if (state == 0) {
            videoArc = videoArc.copy(
                stat = stat.copy(
                    like = stat.like - 1,
                )
            )
            reqUser = reqUser.copy(
                like = state,
            )
        } else if (state == 1) {
            videoArc = videoArc.copy(
                stat = stat.copy(
                    like = stat.like + 1,
                )
            )
            reqUser = reqUser.copy(
                like = state,
            )
        }
        ui.setState {
            arcInfo = videoArc
            reqUserInfo = reqUser
        }
    }

    private fun updateCoinState(state: Int) {
        var videoArc = arcInfo ?: return
        var reqUser = reqUserInfo ?: return
        val stat = videoArc.stat ?: return
        videoArc = videoArc.copy(
            stat = stat.copy(
                coin = stat.coin + state,
            )
        )
        reqUser = reqUser.copy(
            coin = state,
        )
        ui.setState {
            arcInfo = videoArc
            reqUserInfo = reqUser
        }
    }

    private fun updateFavoriteState(state: Int) {
        var videoArc = arcInfo ?: return
        var reqUser = reqUserInfo ?: return
        val stat = videoArc.stat ?: return
        if (state == 0) {
            videoArc = videoArc.copy(
                stat = stat.copy(
                    fav = stat.fav - 1,
                )
            )
            reqUser = reqUser.copy(
                favorite = state,
            )
        } else if (state == 1) {
            videoArc = videoArc.copy(
                stat = stat.copy(
                    fav = stat.fav + 1,
                )
            )
            reqUser = reqUser.copy(
                favorite = state,
            )
        }
        ui.setState {
            arcInfo = videoArc
            reqUserInfo = reqUser
        }

        //收藏夹变动，重新加载播放列表
//        val playListFrom = playListStore.state.from
//        val playListName = playListStore.state.name
//        if (playListFrom is PlayListFrom.Favorite && playListName != null) {
//            //当前列表为收藏夹类型
//            val currentId = playListFrom.mediaId
//            if (addIds.contains(currentId) || delIds.contains(currentId)) {
//                if (delIds.contains(currentId) && videoArc.aid.toString() == playerStore.state.aid) {
//                    //从收藏夹中删除的是当前播放的视频
//                    playListStore.clearPlayList()
//                } else {
//                    playListStore.setFavoriteList(currentId, playListName)
//                }
//            }
//        }
    }

    /**
     * 点赞/取消点赞
     */
    fun requestLike() = viewModelScope.launch(Dispatchers.IO) {
        if (!userStore.isLogin()) {
            PopTip.show("请先登录")
            return@launch
        }
        try {
            val arc = arcInfo ?: return@launch
            val reqUser = reqUserInfo ?: return@launch
            val res = BiliApiService.videoAPI
                .like(
                    aid = arc.aid.toString(),
                    dislike = reqUser.dislike,
                    like = reqUser.like,
                )
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                val state = if (reqUser.like == 1) 0 else 1
                if (state == 1) {
                    PopTip.show("点赞成功")
                } else {
                    PopTip.show("已取消点赞")
                }
                updateLikeState(state)
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    /**
     * 投币
     */
    fun requestCoin(coinNum: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val arc = arcInfo ?: return@launch
            val res = BiliApiService.videoAPI
                .coin(arc.aid.toString(), coinNum)
                .awaitCall()
                .json<MessageInfo>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    updateCoinState(coinNum)
                    PopTip.show("感谢投币")
                } else {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.message ?: e.toString())
        }
    }

    /**
     * 收藏
     */
    fun requestFavorite(
        favIds: List<String>,
        addIds: List<String>,
        delIds: List<String>,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val arc = arcInfo ?: return@launch
            val res = BiliApiService.videoAPI
                .favoriteDeal(
                    aid = arc.aid.toString(),
                    addIds = addIds,
                    delIds = delIds,
                )
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                if (favIds.size - delIds.size + addIds.size == 0) {
                    updateFavoriteState(0)
                } else if (favIds.isEmpty()) {
                    updateFavoriteState(1)
                }
                PopTip.show("操作成功")
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show(e.message ?: e.toString())
        }
    }


    /**
     * 添加至稍后再看
     */
    fun addVideoHistoryToview() = viewModelScope.launch(Dispatchers.IO) {
        if (!userStore.isLogin()) {
            PopTip.show("请先登录")
            return@launch
        }
        try {
            val arcData = arcInfo ?: return@launch
            val res = BiliApiService.userApi
                .videoToviewAdd(arcData.aid.toString())
                .awaitCall()
                .json<MessageInfo>()
            if (res.code == 0) {
                PopTip.show("已添加至稍后再看")
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.toString())
        }
    }

    fun updateIsAutoPlaySeason(isChecked: Boolean) {
        ui.setState {
            isAutoPlaySeason = isChecked
        }
    }

//    data class SeasonEpisodeInfo(
//
//    )
}