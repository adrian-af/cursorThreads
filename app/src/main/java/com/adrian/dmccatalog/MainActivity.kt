package com.adrian.dmccatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adrian.dmccatalog.data.ThreadEntity
import com.adrian.dmccatalog.settings.ThemeMode
import com.adrian.dmccatalog.ui.bestContrastTextColor
import com.adrian.dmccatalog.ui.hexToColor
import com.adrian.dmccatalog.ui.theme.DMCManagerTheme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            DMCManagerTheme(themeMode = themeMode) {
                DmcApp(vm)
            }
        }
    }
}

@Composable
private fun DmcApp(vm: MainViewModel) {
    val all by vm.allUi.collectAsStateWithLifecycle()
    val owned by vm.ownedUi.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()

    val filteredAll = all.filter { it.code.contains(search, true) || it.name.contains(search, true) }
    val filteredOwned = owned.filter { it.code.contains(search, true) || it.name.contains(search, true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.theme_menu))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.tab_my_collection)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.tab_all_colors)) })
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.search_hint)) }
            )

            val items = if (selectedTab == 0) filteredOwned else filteredAll
            if (selectedTab == 0 && items.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_collection),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.code }) { item ->
                        ThreadCard(item = item, onChanged = vm::updateThread)
                        Divider()
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            current = themeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = {
                vm.updateTheme(it)
                showThemeDialog = false
            }
        )
    }
}

@Composable
private fun ThreadCard(item: ThreadEntity, onChanged: (ThreadEntity) -> Unit) {
    var notes by remember(item.code) { mutableStateOf(item.notes) }
    var skeins by remember(item.code) { mutableStateOf(item.skeins.toFloat()) }

    Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val swatchText = bestContrastTextColor(item.hex)
            Text(
                text = item.code,
                color = swatchText,
                modifier = Modifier
                    .background(hexToColor(item.hex), MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics {
                        contentDescription = "swatch ${item.code}"
                    }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.hex, style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(
                checked = item.owned,
                onCheckedChange = {
                    val updated = item.copy(owned = it, skeins = skeins.toInt(), notes = notes)
                    onChanged(updated)
                }
            )
        }

        if (item.owned) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.label_skeins, skeins.toInt()))
            Slider(
                value = skeins,
                onValueChange = {
                    skeins = it
                    onChanged(item.copy(skeins = it.toInt(), notes = notes))
                },
                valueRange = 1f..3f,
                steps = 1
            )
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    onChanged(item.copy(notes = it, skeins = skeins.toInt()))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_notes)) }
            )
        }
    }
}

@Composable
private fun ThemeDialog(
    current: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_menu)) },
        text = {
            Column {
                ThemeOption(stringResource(R.string.theme_system), current == ThemeMode.SYSTEM) { onThemeSelected(ThemeMode.SYSTEM) }
                ThemeOption(stringResource(R.string.theme_light), current == ThemeMode.LIGHT) { onThemeSelected(ThemeMode.LIGHT) }
                ThemeOption(stringResource(R.string.theme_dark), current == ThemeMode.DARK) { onThemeSelected(ThemeMode.DARK) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(if (selected) "• $label" else label, modifier = Modifier.fillMaxWidth())
    }
}
