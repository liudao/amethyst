package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.NotificationCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.model.ChannelCreateEvent
import com.vitorpamplona.amethyst.service.model.ChannelMetadataEvent
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

@Composable
fun ChatroomCompose(baseNote: Note, accountViewModel: AccountViewModel, navController: NavController) {
    val noteState by baseNote.live.observeAsState()
    val note = noteState?.note

    val accountState by accountViewModel.accountLiveData.observeAsState()
    val account = accountState?.account ?: return

    val accountUserState by account.userProfile().live.observeAsState()
    val accountUser = accountUserState?.user ?: return

    val notificationCacheState = NotificationCache.live.observeAsState()
    val notificationCache = notificationCacheState.value ?: return

    val context = LocalContext.current.applicationContext

    if (note?.event == null) {
        BlankNote(Modifier)
    } else if (note.channel != null) {
        val authorState by note.author!!.live.observeAsState()
        val author = authorState?.user

        val channelState by note.channel!!.live.observeAsState()
        val channel = channelState?.channel

        val noteEvent = note.event

        val description = if (noteEvent is ChannelCreateEvent) {
            "Channel created"
        } else if (noteEvent is ChannelMetadataEvent) {
            "Channel Information changed to "
        } else {
            noteEvent?.content
        }
        channel?.let { channel ->
            val hasNewMessages =
                if (noteEvent != null)
                    noteEvent.createdAt > notificationCache.cache.load("Channel/${channel.idHex}", context)
                else
                    false

            ChannelName(
                channelPicture = channel.profilePicture(),
                channelPicturePlaceholder = null,
                channelTitle = {
                    Text(
                        "${channel.info.name}",
                        fontWeight = FontWeight.Bold,
                        modifier = it,
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Content)
                    )
                    Text(
                        " Public Chat",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
                    )
                },
                channelLastTime = note.event?.createdAt,
                channelLastContent = "${author?.toBestDisplayName()}: " + description,
                hasNewMessages = hasNewMessages,
                onClick = { navController.navigate("Channel/${channel.idHex}") })
        }

    } else {
        val authorState by note.author!!.live.observeAsState()
        val author = authorState?.user

        val replyAuthorBase = note.mentions?.first()

        var userToComposeOn = author

        if ( replyAuthorBase != null ) {
            val replyAuthorState by replyAuthorBase.live.observeAsState()
            val replyAuthor = replyAuthorState?.user

            if (author == accountUser) {
                userToComposeOn = replyAuthor
            }
        }

        val noteEvent = note.event

        userToComposeOn?.let { user ->
            val hasNewMessages =
                if (noteEvent != null)
                    noteEvent.createdAt > notificationCache.cache.load("Room/${userToComposeOn.pubkeyHex}", context)
                else
                    false

            ChannelName(
                channelPicture = { UserPicture(user, accountUser, size = 55.dp) },
                channelTitle = { UsernameDisplay(user, it) },
                channelLastTime = noteEvent?.createdAt,
                channelLastContent = accountViewModel.decrypt(note),
                hasNewMessages = hasNewMessages,
                onClick = { navController.navigate("Room/${user.pubkeyHex}") })
        }
    }

}

@Composable
fun ChannelName(
    channelPicture: String,
    channelPicturePlaceholder: Painter?,
    channelTitle: @Composable (Modifier) -> Unit,
    channelLastTime: Long?,
    channelLastContent: String?,
    hasNewMessages: Boolean,
    onClick: () -> Unit
) {
    ChannelName(
        channelPicture = {
            AsyncImage(
                model = channelPicture,
                placeholder = channelPicturePlaceholder,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .width(55.dp)
                    .height(55.dp)
                    .clip(shape = CircleShape)
            )
        },
        channelTitle,
        channelLastTime,
        channelLastContent,
        hasNewMessages,
        onClick
    )
}

@Composable
fun ChannelName(
    channelPicture: @Composable () -> Unit,
    channelTitle: @Composable (Modifier) -> Unit,
    channelLastTime: Long?,
    channelLastContent: String?,
    hasNewMessages: Boolean,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.clickable(onClick = onClick) ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp)
        ) {
            channelPicture()

            Column(modifier = Modifier.padding(start = 10.dp),
            verticalArrangement = Arrangement.SpaceAround) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    channelTitle(Modifier.weight(1f))

                    channelLastTime?.let {
                        Text(
                            timeAgo(channelLastTime),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
                        )
                    }

                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (channelLastContent != null)
                        Text(
                            channelLastContent,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.52f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                            modifier = Modifier.weight(1f)
                        )
                    else
                        Text(
                            "Referenced event not found",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.52f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                    if (hasNewMessages) {
                        NewItemsBubble()
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = 0.25.dp
        )
    }
}

@Composable
fun NewItemsBubble() {
    Box(
        modifier = Modifier
            .padding(start = 3.dp)
            .width(10.dp)
            .height(10.dp)
            .clip(shape = CircleShape)
            .background(MaterialTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
        )
    }
}