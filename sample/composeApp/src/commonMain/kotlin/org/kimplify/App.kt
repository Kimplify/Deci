package org.kimplify

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.kimplify.screens.CoreScreen
import org.kimplify.screens.FinancialScreen
import org.kimplify.screens.FormatStatsScreen
import org.kimplify.screens.ScaleContextScreen
import org.kimplify.screens.ValidationScreen

private enum class Tab(
    val label: String,
    val icon: ImageVector,
) {
    Core("Core", Icons.Default.Calculate),
    Scale("Scale", Icons.Default.Tune),
    Financial("Financial", Icons.Default.Payments),
    FormatStats("Format", Icons.Default.QueryStats),
    Validation("Validate", Icons.Default.CheckCircle),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(Tab.Core) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    MaterialTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = "Deci Library",
                            style = MaterialTheme.typography.displaySmall,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { paddingValues ->
            when (selectedTab) {
                Tab.Core -> CoreScreen(modifier = Modifier.padding(paddingValues))
                Tab.Scale -> ScaleContextScreen(modifier = Modifier.padding(paddingValues))
                Tab.Financial -> FinancialScreen(modifier = Modifier.padding(paddingValues))
                Tab.FormatStats -> FormatStatsScreen(modifier = Modifier.padding(paddingValues))
                Tab.Validation -> ValidationScreen(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}
