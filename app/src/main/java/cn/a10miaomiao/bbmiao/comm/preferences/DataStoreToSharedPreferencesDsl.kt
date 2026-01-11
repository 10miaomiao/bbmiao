package cn.a10miaomiao.bbmiao.comm.preferences

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first


suspend fun DataStore<Preferences>.toSharedPreferences(
    scope: CoroutineScope,
    keysMap: Map<String, Preferences.Key<*>>,
): SharedPreferences {
    val initialPreferences = data.first()
    return DataStoreToSharedPreferences(scope, keysMap, this, initialPreferences)
}