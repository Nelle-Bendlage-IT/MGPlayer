package com.mgplayer.tv.features.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mgplayer.tv.common.components.PreviewCard
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeNestedScreen(
    onItemFocus: (parent: Int, child: Int) -> Unit,
    onItemClick: (parent: Int, child: Int) -> Unit,
    mainFeedClipsState: ViewState<List<Clip>>,
    mainFeedClipsPaginationState: ViewState<Boolean>,
    getMainFeedClipsPagination: (offset: Int) -> Unit,
    getMainFeedClips: (offset: Int, count: Int) -> Unit,
) {
    LaunchedEffect(Unit) {
        getMainFeedClips(0, 40)
    }
    var selectedCard by remember { mutableStateOf(immersiveListItems.first()) }

    val focusState = remember {
        mutableStateOf(FocusPosition(0, 0))
    }
    val topFade = Brush.verticalGradient(0f to Color.Transparent, 0.3f to Color.Black)
    val enableFadeEdge = remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // background image
            AsyncImage(
                model = selectedCard.image,
                contentDescription = "clip.title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            // gradient and text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .immersiveListGradient(),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 58.dp, bottom = 16.dp)
                        .width(480.dp)
                        .wrapContentHeight()
                ) {
                    Text(
                        text = selectedCard.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                    )

                    Text(
                        text = selectedCard.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = selectedCard.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                    )
                }
            }

            val firstChildFr = remember { FocusRequester() }
            var focusedIndex by remember { mutableIntStateOf(0) }

            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 20.dp)
                    .focusRestorer { firstChildFr },
                contentPadding = PaddingValues(start = 58.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(immersiveListItems) { index, card ->
                    RowItem(
                        index = index,
                        imageURL = card.image,
                        modifier = Modifier
                            .width(196.dp)
                            .aspectRatio(16f / 9)
                            .ifElse(index == 0, Modifier.focusRequester(firstChildFr))
                            .onFocusChanged {
                                if (it.isFocused) {
                                    selectedCard = card
                                    focusedIndex = index
                                }
                            },
                        focused = focusedIndex == index,
                        onSelected = { _ -> },
                        videoURL = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                        height = 120.dp
                    )
                }
            }
        }


    }
//
//            item {
//                HorizontalCarouselItem(
//                    "Zuletzt angesehen",
//                    parent = 0,
//                    onItemFocus = { p, c ->
//                        focusState.value = FocusPosition(p, c)
//                        enableFadeEdge.value = p > 0
//                    },
//                    onItemClick = onItemClick
//                )
//            }


}


typealias FocusPosition = Pair<Int, Int>

@SuppressLint("SuspiciousModifierThen")
fun Modifier.immersiveListGradient(): Modifier = composed {
    val color = MaterialTheme.colorScheme.surface

    val colorAlphaList = listOf(1.0f, 0.2f, 0.0f)
    val colorStopList = listOf(0.2f, 0.8f, 0.9f)

    val colorAlphaList2 = listOf(1.0f, 0.1f, 0.0f)
    val colorStopList2 = listOf(0.1f, 0.4f, 0.9f)
    this
        .then(
            background(
                brush = Brush.linearGradient(
                    colorStopList[0] to color.copy(alpha = colorAlphaList[0]),
                    colorStopList[1] to color.copy(alpha = colorAlphaList[1]),
                    colorStopList[2] to color.copy(alpha = colorAlphaList[2]),
                    start = Offset(0.0f, 0.0f),
                    end = Offset(Float.POSITIVE_INFINITY, 0.0f)
                )
            )
        )
        .then(
            background(
                brush = Brush.linearGradient(
                    colorStopList2[0] to color.copy(alpha = colorAlphaList2[0]),
                    colorStopList2[1] to color.copy(alpha = colorAlphaList2[1]),
                    colorStopList2[2] to color.copy(alpha = colorAlphaList2[2]),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(0f, 0f)
                )
            )
        )
}

private val immersiveListItems = listOf(
    ImmersiveListSlide(
        title = "Pressesch(l)au",
        subtitle = "Folge 182",
        description = "M\\u00e4nner mit Pieps-Stimme - Warum Migranten AfD w\\u00e4hlen - Gurkenwasser tut gut",
        image = "https://cache.massengeschmack.tv/img/mag/ps182.jpg",
    ),
    ImmersiveListSlide(
        title = "Pasch-TV",
        subtitle = "Folge 503",
        description = "Sky Team",
        image = "https://cache.massengeschmack.tv/img/mag/pasch503.jpg",
    ),
    ImmersiveListSlide(
        title = "Veto",
        subtitle = "Folge 106",
        description = "Ulrich Schneider \\u00fcber das Versagen der Republik",
        image = "https://cache.massengeschmack.tv/img/mag/veto106.jpg",
    ),
    ImmersiveListSlide(
        title = "Massengeschnack",
        subtitle = "Folge 157",
        description = "EM-Studio 5 | Nach der EM ist vor Olympia!",
        image = "https://cache.massengeschmack.tv/img/mag/wio17.jpg",
    ),
)

private data class ImmersiveListSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val image: String,
)

fun Modifier.ifElse(
    condition: Boolean,
    ifTrueModifier: Modifier,
    elseModifier: Modifier = Modifier
): Modifier = then(if (condition) ifTrueModifier else elseModifier)

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun RowItem(
    modifier: Modifier,
    imageURL: String,
    videoURL: String,
    height: Dp,
    index: Int,
    focused: Boolean,
    onSelected: (index: Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    PreviewCard(modifier = modifier
        .onFocusChanged { onSelected(index) }
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = { onSelected(index) }
        ), cardWidth = 240.dp, cardHeight = height, videoUrl = videoURL,
        hasFocus = focused, thumbnailUrl = imageURL, onClick = {})
}