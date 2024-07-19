@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.mgplayer.tv.features.home.carousel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun HorizontalCarouselItem(
    rowTitle: String,
    parent: Int,
    onItemFocus: (parent: Int, child: Int) -> Unit,
    onItemClick: (parent: Int, child: Int) -> Unit,
) {
    Column(
        Modifier
            .height(150.dp)
            .background(Color.Transparent)) {
        Text(
            text = rowTitle,
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium
        )
        LazyRow(
            modifier = Modifier,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
                end = 100.dp,
            ),
            userScrollEnabled = true
        ) {
            items(15) { child ->
                CarouselItem(
                    Modifier,
                    parent,
                    child,
                    onItemClick = onItemClick,
                    onItemFocus = onItemFocus,
                )
            }
        }
    }
}

@Preview
@Composable
fun HorizontalCarouselItemPrev() {
    HorizontalCarouselItem(
        "Zuletzt angesehen",
        parent = 0,
        onItemFocus = { _, _ -> },
        onItemClick = { _, _ -> },
    )
}
