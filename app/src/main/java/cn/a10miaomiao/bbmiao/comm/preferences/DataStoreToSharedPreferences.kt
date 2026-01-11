package cn.a10miaomiao.bbmiao.comm.preferences

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArraySet

class DataStoreToSharedPreferences(
    private val scope: CoroutineScope,
    private val keysMap: Map<String, Preferences.Key<*>>,
    private val dataStore: DataStore<Preferences>,
    initialPreferences: Preferences,
) : SharedPreferences {

    @Volatile
    private var preferences = initialPreferences

    private val listeners =
        CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()

    init {
        scope.launch {
            dataStore.data.collect {
                preferences = it
                // 通知监听器（模拟 SP 行为）
                for (listener in listeners) {
                    keysMap.keys.forEach { key ->
                        listener.onSharedPreferenceChanged(this@DataStoreToSharedPreferences, key)
                    }
                }
            }
        }
    }

    private fun keyOf(name: String?): Preferences.Key<*>? =
        name?.let { keysMap[it] }

    override fun contains(key: String?): Boolean {
        val k = keyOf(key) ?: return false
        return preferences.contains(k)
    }

    override fun getAll(): Map<String, *> =
        keysMap.mapNotNull { (name, key) ->
            preferences[key]?.let { name to it }
        }.toMap()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<Boolean>] ?: defValue
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<Int>] ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<Long>] ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<Float>] ?: defValue
    }

    override fun getString(key: String?, defValue: String?): String? {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<String>] ?: defValue
    }

    override fun getStringSet(
        key: String?,
        defValue: Set<String?>?
    ): Set<String?>? {
        val k = keyOf(key) ?: return defValue
        return preferences[k as Preferences.Key<Set<String>>] ?: defValue
    }

    override fun edit(): SharedPreferences.Editor =
        Editor(scope, dataStore, keysMap)

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.add(it) }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        listener?.let { listeners.remove(it) }
    }

    class Editor(
        private val scope: CoroutineScope,
        private val dataStore: DataStore<Preferences>,
        private val keysMap: Map<String, Preferences.Key<*>>
    ) : SharedPreferences.Editor {

        private val updates = mutableMapOf<Preferences.Key<*>, Any?>()
        private var clearAll = false

        private fun keyOf(name: String?): Preferences.Key<*>? =
            name?.let { keysMap[it] }

        override fun putBoolean(key: String?, value: Boolean) =
            applyPut(key, value)

        override fun putFloat(key: String?, value: Float) =
            applyPut(key, value)

        override fun putInt(key: String?, value: Int) =
            applyPut(key, value)

        override fun putLong(key: String?, value: Long) =
            applyPut(key, value)

        override fun putString(key: String?, value: String?) =
            applyPut(key, value)

        override fun putStringSet(key: String?, value: Set<String?>?) =
            applyPut(key, value)

        override fun remove(key: String?): SharedPreferences.Editor {
            keyOf(key)?.let { updates[it] = null }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }

        override fun apply() {
            scope.launch {
                dataStore.edit { prefs ->
                    if (clearAll) prefs.clear()
                    updates.forEach { (key, value) ->
                        prefs.setValue(key, value)
                    }
                }
            }
        }

        override fun commit(): Boolean {
            apply()
            return true // DataStore 没有同步 commit
        }

        private fun applyPut(key: String?, value: Any?): SharedPreferences.Editor {
            keyOf(key)?.let { updates[it] = value }
            return this
        }

        private fun MutablePreferences.setValue(key: Preferences.Key<*>, value: Any?) {
            when (value) {
                is Boolean -> this[key as Preferences.Key<Boolean>] = value
                is Float -> this[key as Preferences.Key<Float>] = value
                is Int -> this[key as Preferences.Key<Int>] = value
                is Long -> this[key as Preferences.Key<Long>] = value
                is String -> this[key as Preferences.Key<String>] = value
                is Set<*> -> this[key as Preferences.Key<Set<String>>] = value.map { it.toString() }.toSet()
                else -> this.remove(key)
            }
        }
    }
}