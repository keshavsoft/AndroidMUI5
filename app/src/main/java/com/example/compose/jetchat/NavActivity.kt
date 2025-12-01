/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.jetchat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.compose.jetchat.components.JetchatDrawer
import com.example.compose.jetchat.core.navigation.DrawerDestination
import com.example.compose.jetchat.databinding.ContentMainBinding
import com.example.compose.jetchat.feature.sms.SmsDetailScreen
import com.example.compose.jetchat.feature.sms.SmsListScreen
import kotlinx.coroutines.launch

/**
 * Main activity for the app.
 */
class NavActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {

                    val drawerState = rememberDrawerState(initialValue = Closed)
                    val drawerOpen by viewModel.drawerShouldBeOpened.collectAsStateWithLifecycle()

                    // Which drawer item is currently selected
                    var selectedDestination by remember {
                        mutableStateOf<DrawerDestination>(DrawerDestination.Composers)
                    }

                    // Which SMS thread is open in detail (null = show list)
                    var selectedSmsAddress by remember { mutableStateOf<String?>(null) }

                    val scope = rememberCoroutineScope()

                    if (drawerOpen) {
                        LaunchedEffect(Unit) {
                            try {
                                drawerState.open()
                            } finally {
                                viewModel.resetOpenDrawerAction()
                            }
                        }
                    }

                    JetchatDrawer(
                        drawerState = drawerState,
                        selectedMenu = selectedDestination.key,
                        onChatClicked = { key ->
                            scope.launch { drawerState.close() }

                            val destination = DrawerDestination.fromKey(key)

                            when (destination) {
                                DrawerDestination.TestByKeshav -> {
                                    findNavController().popBackStack(R.id.nav_newchat, false)
                                    findNavController().navigate(R.id.nav_newchat)
                                }

                                DrawerDestination.Gps -> {
                                    findNavController().popBackStack(R.id.nav_gps, false)
                                    findNavController().navigate(R.id.nav_gps)
                                }

                                DrawerDestination.Sms -> {
                                    // SMS handled in the composable content below
                                }

                                else -> {
                                    // Default home / composers
                                    findNavController().popBackStack(R.id.nav_home, false)
                                    findNavController().navigate(R.id.nav_home)
                                }
                            }

                            selectedDestination = destination

                            // If we left SMS section, clear SMS detail selection
                            if (destination != DrawerDestination.Sms) {
                                selectedSmsAddress = null
                            }
                        },
                        onProfileClicked = { userId ->
                            val bundle = bundleOf("userId" to userId)
                            findNavController().navigate(R.id.nav_profile, bundle)
                            scope.launch { drawerState.close() }
                        }
                    ) {
                        // 🔥 Screen switching section
                        if (selectedDestination is DrawerDestination.Sms) {
                            if (selectedSmsAddress == null) {
                                // 📋 SMS list screen
                                SmsListScreen(
                                    onBack = {
                                        // Back from SMS list → go to main chat
                                        selectedDestination = DrawerDestination.Composers
                                    },
                                    onSmsClick = { group ->
                                        // Open detail for this mobile
                                        selectedSmsAddress = group.mobile
                                    }
                                )
                            } else {
                                // 💬 SMS detail screen (conversation)
                                SmsDetailScreen(
                                    mobile = selectedSmsAddress!!,
                                    onBack = {
                                        // Back from detail → go to list
                                        selectedSmsAddress = null
                                    }
                                )
                            }
                        } else {
                            // Default behavior: show Fragment NavHost content
                            AndroidViewBinding(ContentMainBinding::inflate)
                        }
                    }
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}
