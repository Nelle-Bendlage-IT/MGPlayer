package features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.mgtvapi.api.model.MainFeedResponse
import com.mgtvapi.api.repository.MGTVApiRepository
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.launch
import massengeschmacktv.composeapp.generated.resources.Res
import massengeschmacktv.composeapp.generated.resources.mgtvlogo
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject


sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(
        val data: T
    ) : UiState<T>()

    data object Error : UiState<Nothing>()
}

class HomeScreen() : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        var uiState by remember { mutableStateOf<UiState<MainFeedResponse>>(UiState.Loading) }
        val repo: MGTVApiRepository = koinInject()
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            scope.launch {
                val result = repo.getMainFeed(offset = 0, count = 10)
                uiState = UiState.Success(result)
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.mgtvlogo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(75.dp)
                            )
                        }

                    }
                )
            },
        ) { innerPadding ->
            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(innerPadding))
                    }
                }

                is UiState.Success -> {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(count = (uiState as UiState.Success<MainFeedResponse>).data.clips.size) {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                ) {
                                Image(
                                    painter = rememberImagePainter(
                                        (uiState as UiState.Success<MainFeedResponse>).data.clips[it].image
                                    ),
                                    contentDescription = "image",
                                )
                                Text((uiState as UiState.Success<MainFeedResponse>).data.clips[it].title)

                            }

                        }
                    }

                }

                is UiState.Error -> {
                    Text("ERROR", modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }
}

