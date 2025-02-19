package com.vitorpamplona.amethyst.ui.actions

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Colors
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.model.Account
import java.lang.Math.round

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewRelayListView(onClose: () -> Unit, account: Account, relayToAdd: String = "") {
    val postViewModel: NewRelayListViewModel = viewModel()

    val feedState by postViewModel.relays.collectAsState()

    LaunchedEffect(Unit) {
        postViewModel.load(account)
    }

    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CloseButton(onCancel = {
                        postViewModel.clear()
                        onClose()
                    })

                    PostButton(
                        onPost = {
                            postViewModel.create()
                            onClose()
                        },
                        true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(1f), verticalAlignment = Alignment.CenterVertically) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 10.dp,
                            bottom = 10.dp
                        ),
                    ) {
                        itemsIndexed(feedState, key = { _, item -> item.url }) { index, item ->
                            if (index == 0)
                                ServerConfigHeader()
                            ServerConfig(item,
                                onToggleDownload = {
                                    postViewModel.toggleDownload(it)
                                },
                                onToggleUpload = {
                                    postViewModel.toggleUpload(it)
                                },
                                onDelete = {
                                    postViewModel.deleteRelay(it)
                                }
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                EditableServerConfig(relayToAdd) {
                    postViewModel.addRelay(it)
                }
            }
        }
    }
}

@Composable
fun ServerConfigHeader() {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Relay Address",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.size(20.dp))

                    Text(
                        text = "Posts",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = "Posts",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = "Errors",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }

        Divider(
            thickness = 0.25.dp
        )
    }
}

@Composable
fun ServerConfig(
    item: NewRelayListViewModel.Relay,
    onToggleDownload: (NewRelayListViewModel.Relay) -> Unit,
    onToggleUpload: (NewRelayListViewModel.Relay) -> Unit,
    onDelete: (NewRelayListViewModel.Relay) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.url.removePrefix("wss://"),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { onToggleDownload(item) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            null,
                            modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                            tint = if (item.read) Color.Green else Color.Red
                        )
                    }

                    Text(
                        text = "${countToHumanReadable(item.downloadCount)}",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { onToggleUpload(item) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            null,
                            modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                            tint = if (item.write) Color.Green else Color.Red
                        )
                    }

                    Text(
                        text = "${countToHumanReadable(item.uploadCount)}",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        null,
                        modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                        tint = if (item.errorCount > 0) Color.Yellow else Color.Green
                    )

                    Text(
                        text = "${countToHumanReadable(item.errorCount)}",
                        maxLines = 1,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    )

                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { onDelete(item) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            null,
                            modifier = Modifier.padding(horizontal = 5.dp).size(15.dp),
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        Divider(
            thickness = 0.25.dp
        )
    }
}

@Composable
fun EditableServerConfig(relayToAdd: String, onNewRelay: (NewRelayListViewModel.Relay) -> Unit) {
    var url by remember { mutableStateOf<String>(relayToAdd) }
    var read by remember { mutableStateOf(true) }
    var write by remember { mutableStateOf(true) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            label = { Text(text = "Add a Relay") },
            modifier =  Modifier.weight(1f),
            value = url,
            onValueChange = { url = it },
            placeholder = {
                Text(
                    text = "server.com",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    maxLines = 1
                )
            },
            singleLine = true
        )

        IconButton(onClick = { read = !read }) {
            Icon(
                imageVector = Icons.Default.Download,
                null,
                modifier = Modifier
                    .size(35.dp)
                    .padding(horizontal = 5.dp),
                tint = if (read) Color.Green else Color.Red
            )
        }

        IconButton(onClick = { write = !write }) {
            Icon(
                imageVector = Icons.Default.Upload,
                null,
                modifier = Modifier
                    .size(35.dp)
                    .padding(horizontal = 5.dp),
                tint = if (write) Color.Green else Color.Red
            )
        }

        Button(
            onClick = {
                if (url.isNotBlank()) {
                    val addedWSS = if (!url.startsWith("wss://")) "wss://$url" else url
                    onNewRelay(NewRelayListViewModel.Relay(addedWSS, read, write))
                    url = ""
                    write = true
                    read = true
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = if (url.isNotBlank()) MaterialTheme.colors.primary else Color.Gray
                )
        ) {
            Text(text = "Add", color = Color.White)
        }

    }
}


fun countToHumanReadable(counter: Int) = when {
    counter >= 1000000000 -> "${round(counter/1000000000f)}G"
    counter >= 1000000 -> "${round(counter/1000000f)}M"
    counter >= 1000 -> "${round(counter/1000f)}k"
    else -> "$counter"
}