package com.adrian.dmccatalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.adrian.dmccatalog.data.AppDatabase
import com.adrian.dmccatalog.data.ThreadEntity
import com.adrian.dmccatalog.data.parseDmcChartMarkdown
import com.adrian.dmccatalog.settings.ThemeMode
import com.adrian.dmccatalog.settings.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = Room.databaseBuilder(app, AppDatabase::class.java, "dmc.db").build()
    private val dao = db.threadDao()
    private val prefs = ThemePreferences(app)

    val themeMode = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    private val allThreads = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val ownedThreads = dao.observeOwned().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hasData: StateFlow<Boolean> = allThreads.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val allUi: StateFlow<List<ThreadEntity>> = allThreads
    val ownedUi: StateFlow<List<ThreadEntity>> = ownedThreads

    init {
        viewModelScope.launch {
            if (allThreads.value.isEmpty()) {
                val md = app.assets.open("dmc_color_chart.md").bufferedReader().use { it.readText() }
                dao.insertAll(parseDmcChartMarkdown(md))
            }
        }
    }

    fun updateThread(item: ThreadEntity) {
        viewModelScope.launch { dao.update(item) }
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch { prefs.setTheme(mode) }
    }
}
