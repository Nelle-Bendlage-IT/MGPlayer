package features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.mgtvapi.api.repository.MGTVApiRepository
import common.components.MGCircularProgressIndicator
import kotlinx.coroutines.launch
import massengeschmacktv.composeapp.generated.resources.Res
import massengeschmacktv.composeapp.generated.resources.email
import massengeschmacktv.composeapp.generated.resources.login_submit
import massengeschmacktv.composeapp.generated.resources.mgtvlogo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

class LoginScreen() : Screen {
    @Composable
    override fun Content() {
        var loading by rememberSaveable { mutableStateOf(false) }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        val isSubmitEnabled =
            remember(email, password, loading) {
                !loading && (email.isNotBlank() && password.isNotBlank())
            }
        val repo: MGTVApiRepository = koinInject()
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) {
            LoginContent(
                onPasswordChange = { value: String -> password = value },
                onEmailChange = { value: String -> email = value },
                email = email,
                password = password,
                loading = loading,
                isSubmitEnabled = isSubmitEnabled,
                onSubmit = {
                    scope.launch {
                        loading = true
                            repo.login(email = email, password = password)
                        loading = false
                    }
                },
            )
        }
    }
}

@Preview
@Composable
fun LoginContent(
    onEmailChange: (value: String) -> Unit,
    onPasswordChange: (value: String) -> Unit,
    onSubmit: () -> Unit,
    email: String,
    password: String,
    loading: Boolean,
    isSubmitEnabled: Boolean,
) {
    val focusManager by rememberUpdatedState(LocalFocusManager.current)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.mgtvlogo),
            contentDescription = "logo",
            modifier = Modifier.size(200.dp, 150.dp)
        )

        Spacer(modifier = Modifier.padding(16.dp))
        EmailTextField(
            email = email,
            onEmailChange = onEmailChange,
            loading = loading,
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        )
        Spacer(modifier = Modifier.padding(4.dp))
        PasswordTextField(
            password = password,
            onPasswordChange = onPasswordChange,
            loading = loading,
            onDone = { focusManager.clearFocus() },
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isSubmitEnabled,
        ) {
            if (!loading){
                Text(text = stringResource(resource = Res.string.login_submit))
            } else {
                MGCircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailTextField(
    email: String,
    onEmailChange: (value: String) -> Unit,
    loading: Boolean,
    onNext: () -> Unit = {},
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(text = stringResource(Res.string.email)) },
        modifier =
            Modifier
                .fillMaxWidth()
                .autofill(
                    autofillTypes = listOf(AutofillType.EmailAddress),
                    onFill = onEmailChange,
                ),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
                onNext = { onNext() },
            ),
        enabled = !loading,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (value: String) -> Unit,
    loading: Boolean,
    onDone: () -> Unit = {},
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(text = "Password") },
        modifier =
            Modifier
                .fillMaxWidth()
                .autofill(
                    autofillTypes = listOf(AutofillType.Password),
                    onFill = onPasswordChange,
                ),
        singleLine = true,
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { onDone() },
            ),
        trailingIcon = {
            IconButton(onClick = {
                if (!loading) passwordVisibility = !passwordVisibility
            }) {
                Icon(
                    imageVector = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                )
            }
        },
        enabled = !loading,
    )
}
