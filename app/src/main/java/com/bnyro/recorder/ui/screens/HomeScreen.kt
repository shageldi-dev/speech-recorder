package com.bnyro.recorder.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.BuildConfig
import com.bnyro.recorder.R
import com.bnyro.recorder.db.AppDatabase
import com.bnyro.recorder.db.Words
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.Destination
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.models.RecorderModel
import com.bnyro.recorder.util.Preferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.ceil


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    DelicateCoroutinesApi::class
)
@Composable
fun HomeScreen(
    initialRecorder: RecorderType,
    onNavigate: (Destination) -> Unit,
    recorderModel: RecorderModel = viewModel(LocalContext.current as ComponentActivity)
) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current

    var isFirst by rememberSaveable {
        mutableStateOf(true)
    }
    LaunchedEffect(true){
        if(isFirst){
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                )
            }
            isFirst = false
        }
    }
    var words by remember {
        mutableStateOf<List<Words>>(emptyList())
    }

    var text by remember {
        mutableStateOf("")
    }

    var username by remember {
        mutableStateOf("")
    }
    var index by rememberSaveable {
        mutableIntStateOf(0)
    }


    var step by rememberSaveable {
        mutableIntStateOf(0)
    }

    val limit = 6


    val db = AppDatabase.getDatabase(LocalContext.current)
    db.wordDao().getAll().observe(LocalLifecycleOwner.current) {
        words = it
    }

    LaunchedEffect(step, words) {
        val wrdStr=try {
            words.subList(step * limit, step * limit + limit).map { it.word }.joinToString("*")
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
        Preferences.edit { putString(Preferences.step, "${step+1}") }
        Preferences.edit { putString(Preferences.words, wrdStr)}
    }

    if (index == 0) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            TextField(
                value = username,
                onValueChange = { username = it }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(text = "Username")
                }
            )
            TextField(
                value = text,
                onValueChange = { text = it }, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp)
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            )
            Button(onClick = {
                Preferences.edit { putString(Preferences.username, username) }
                val wrds: Array<String> =
                    text
                        .lowercase()
                        .replace("[^\\p{L}0-9 ]".toRegex(), "")
                        .replace("\n", "")
                        .replace("\t", "")
                        .trim()
                        .split(" ")
                        .toTypedArray()

                for (i in wrds.indices) {
                    GlobalScope.launch {
//                        if(words[i].word.trim().isNotEmpty()){
                            db.wordDao().insert(
                                Words(wrds[i], date = System.currentTimeMillis(), i)
                            )
//                        }
                    }
                }
                index = 1
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Next")
            }
        }
    } else {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
                ClickableIcon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                ) {
                    onNavigate(Destination.Settings)
                }
                ClickableIcon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = stringResource(R.string.recordings)
                ) {
                    onNavigate(Destination.RecordingPlayer)
                }
            })
        }, bottomBar = {
            Column {
                AnimatedVisibility(recorderModel.recorderState == RecorderState.IDLE) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(
                                        id = R.string.record_sound
                                    )
                                )
                            },
                            label = { Text(stringResource(R.string.record_sound)) },
                            selected = (pagerState.currentPage == 0),
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                scope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = stringResource(
                                        id = R.string.record_screen
                                    )
                                )
                            },
                            label = { Text(stringResource(R.string.record_screen)) },
                            selected = (pagerState.currentPage == 1),
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        )
                    }
                }
            }
        }) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                WordsScreen(
                    words = try {
                    words.subList(step * limit, step * limit + limit)
                } catch (ex: Exception) {
                    emptyList()
                }, step = step+1, size = ceil((words.size/limit).toDouble()).toInt(), onNext = { step = step.inc() }) {
                    step = step.dec()
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    RecorderView(initialRecorder = initialRecorder, recordScreenMode = (index == 1))
                }
            }
        }
    }
}



