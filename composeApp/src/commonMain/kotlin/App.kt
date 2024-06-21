import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.common.mgtvApiSharedModule
import features.home.HomeLayout
import features.login.LoginScreen
import okio.FileSystem
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import theme.AppTheme

@OptIn(ExperimentalCoilApi::class)
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(mgtvApiSharedModule)
    }) {
        AppTheme {
            setSingletonImageLoaderFactory { context ->
                getAsyncImageLoader(context)
            }
            val tokenManager: TokenManager = koinInject()
            val repo: MGTVApiRepository = koinInject()
            val isLoggedIn by repo.isLoggedIn().collectAsState(tokenManager.email != null)
            if (isLoggedIn) {
                Navigator(HomeLayout())
            } else {
                Navigator(LoginScreen())
            }
        }
    }
}

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context).diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED).diskCache {
            newDiskCache()
        }.crossfade(true).build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(512L * 1024 * 1024) // 512MB
        .build()
}