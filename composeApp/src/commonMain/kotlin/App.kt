import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.common.mgtvApiSharedModule
import features.home.HomeScreen
import features.login.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import theme.AppTheme

@Composable
@Preview
fun App() {

    KoinApplication(application = {
        modules(mgtvApiSharedModule)
    }) {
        AppTheme {
            val tokenManager: TokenManager = koinInject()
            val authRepo: MGTVApiRepository = koinInject()

            val isLoggedIn by authRepo.isLoggedIn().collectAsState(tokenManager.email != null)
            if (isLoggedIn) {
                Navigator(HomeScreen())
            } else {
                Navigator(LoginScreen())
            }
        }
    }

}