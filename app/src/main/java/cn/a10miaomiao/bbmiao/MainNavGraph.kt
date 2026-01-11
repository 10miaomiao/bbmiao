package cn.a10miaomiao.bbmiao

import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.page.MainFragment
//import cn.a10miaomiao.bbmiao.page.TestFragment
import cn.a10miaomiao.bbmiao.page.web.WebFragment
import cn.a10miaomiao.bbmiao.page.auth.H5LoginFragment
import cn.a10miaomiao.bbmiao.page.bangumi.BangumiDetailFragment
import cn.a10miaomiao.bbmiao.page.bangumi.BangumiPagesFragment
import cn.a10miaomiao.bbmiao.page.download.DownloadVideoCreateFragment
import cn.a10miaomiao.bbmiao.page.rank.RankFragment
import cn.a10miaomiao.bbmiao.page.region.RegionFragment
import cn.a10miaomiao.bbmiao.page.search.SearchResultFragment
import cn.a10miaomiao.bbmiao.page.search.SearchStartFragment
import cn.a10miaomiao.bbmiao.page.search.result.VideoRegionFragment
import cn.a10miaomiao.bbmiao.page.setting.*
import cn.a10miaomiao.bbmiao.template.TemplateFragment
import cn.a10miaomiao.bbmiao.page.user.*
import cn.a10miaomiao.bbmiao.page.user.archive.UserArchiveListFragment
import cn.a10miaomiao.bbmiao.page.user.archive.UserSearchArchiveListFragment
import cn.a10miaomiao.bbmiao.page.user.archive.UserSeriesDetailFragment
import cn.a10miaomiao.bbmiao.page.user.bangumi.MyBangumiFragment
import cn.a10miaomiao.bbmiao.page.user.bangumi.UserBangumiFragment
import cn.a10miaomiao.bbmiao.page.user.favourite.UserFavouriteDetailFragment
import cn.a10miaomiao.bbmiao.page.user.favourite.UserFavouriteListFragment
import cn.a10miaomiao.bbmiao.page.video.*
import cn.a10miaomiao.bbmiao.page.video.comment.*
import cn.a10miaomiao.bbmiao.page.setting.AboutFragment
import cn.a10miaomiao.bbmiao.page.setting.SettingFragment
import cn.a10miaomiao.bbmiao.page.setting.DanmakuSettingFragment
import cn.a10miaomiao.bbmiao.page.setting.FlagsSeetingFragment
import cn.a10miaomiao.bbmiao.page.setting.HomeSettingFragment
import kotlin.reflect.KClass


object MainNavGraph {
    // Counter for id's. First ID will be 1.
    private var id_counter = 100

    object dest {
        val main = id_counter++
        val template = id_counter++
        val compose = id_counter++
        val web = id_counter++
    }

    val defaultNavOptions get() = NavOptions.Builder()
        .setEnterAnim(R.anim.miao_fragment_open_enter)
        .setExitAnim(R.anim.miao_fragment_open_exit)
        .setPopEnterAnim(R.anim.miao_fragment_close_enter)
        .setPopExitAnim(R.anim.miao_fragment_close_exit)
        .build()

    private val defaultNavOptionsBuilder: NavOptionsBuilder.() -> Unit = {
        anim {
            enter = R.anim.miao_fragment_open_enter
            exit = R.anim.miao_fragment_open_exit
            popEnter = R.anim.miao_fragment_close_enter
            popExit = R.anim.miao_fragment_close_exit
        }
    }

    fun createGraph(navController: NavController, startDestination: Int) {
        navController.graph = navController.createGraph(0, startDestination) {
            addFragment(MainFragment::class, MainFragment.Companion, dest.main)
            addFragment(TemplateFragment::class, TemplateFragment.Companion, dest.template)
            addFragment(WebFragment::class, WebFragment.Companion, dest.web)

            addFragment(RegionFragment::class, RegionFragment.Companion)
            addFragment(RankFragment::class, RankFragment.Companion)

            addFragment(VideoInfoFragment::class, VideoInfoFragment.Companion)
            addFragment(VideoCoinFragment::class, VideoCoinFragment.Companion)
            addFragment(VideoPagesFragment::class, VideoPagesFragment.Companion)
            addFragment(VideoAddFavoriteFragment::class, VideoAddFavoriteFragment.Companion)

            addFragment(VideoCommentListFragment::class, VideoCommentListFragment.Companion)
            addFragment(VideoCommentDetailFragment::class, VideoCommentDetailFragment.Companion)
            addFragment(ReplyDetailFragment::class, ReplyDetailFragment.Companion)
            addFragment(SendCommentFragment::class, SendCommentFragment.Companion)

            addFragment(BangumiDetailFragment::class, BangumiDetailFragment.Companion)
            addFragment(BangumiPagesFragment::class, BangumiPagesFragment.Companion)

            addFragment(H5LoginFragment::class, H5LoginFragment.Companion)

            addFragment(UserFragment::class, UserFragment.Companion)
            addFragment(MyBangumiFragment::class, MyBangumiFragment.Companion)
            addFragment(UserBangumiFragment::class, UserBangumiFragment.Companion)
            addFragment(UserFavouriteListFragment::class, UserFavouriteListFragment.Companion)
            addFragment(UserFavouriteDetailFragment::class, UserFavouriteDetailFragment.Companion)
            addFragment(UserArchiveListFragment::class, UserArchiveListFragment.Companion)
            addFragment(UserSearchArchiveListFragment::class, UserSearchArchiveListFragment.Companion)
            addFragment(UserSeriesDetailFragment::class, UserSeriesDetailFragment.Companion)
            addFragment(UserFollowFragment::class, UserFollowFragment.Companion)
            addFragment(HistoryFragment::class, HistoryFragment.Companion)
            addFragment(WatchLaterFragment::class, WatchLaterFragment.Companion)


            addFragment(SearchStartFragment::class, SearchStartFragment.Companion)
            addFragment(SearchResultFragment::class, SearchResultFragment.Companion)
            addFragment(VideoRegionFragment::class, VideoRegionFragment.Companion)

            addFragment(DownloadVideoCreateFragment::class, DownloadVideoCreateFragment.Companion)

            addFragment(AboutFragment::class, AboutFragment.Companion)
            addFragment(DanmakuSettingFragment::class, DanmakuSettingFragment.Companion)
            addFragment(HomeSettingFragment::class, HomeSettingFragment.Companion)
            addFragment(SettingFragment::class, SettingFragment.Companion)
            addFragment(ThemeSettingFragment::class, ThemeSettingFragment.Companion)
            addFragment(VideoSettingFragment::class, VideoSettingFragment.Companion)
            addFragment(FlagsSeetingFragment::class, FlagsSeetingFragment.Companion)
        }
    }

    fun NavGraphBuilder.addFragment(
        kClass: KClass<out Fragment>,
        builder: FragmentNavigatorBuilder,
        _id: Int = id_counter++,
    ) {
        val id = if (builder.id == 0) _id else builder.id
        val actionId = if (builder.actionId == 0) id_counter++ else builder.actionId
        destination(
            FragmentNavigatorDestinationBuilder(
                provider[FragmentNavigator::class],
                id,
                kClass,
            ).apply {
                builder.run { build(id, actionId) }
            }
        )
        action(actionId) {
            destinationId = id
            navOptions(defaultNavOptionsBuilder)
        }
    }

}