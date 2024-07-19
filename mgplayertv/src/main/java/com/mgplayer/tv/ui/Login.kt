package com.mgplayer.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.util.Exceptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repo: MGTVApiRepository
) {
    val scope = rememberCoroutineScope()
    var loading by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isSubmitEnabled = remember(email, password, loading) {
        !loading && (email.isNotBlank() && password.isNotBlank())
    }
    val snackbarHostState = remember { SnackbarHostState() }

    fun onSignIn(username: String, password: String) {
        loading = true
        scope.launch(Dispatchers.IO) {
            try {
                val isLoggedIn = repo.login(username, password)
                if (!isLoggedIn) {
                    throw Exceptions.LoginException("Login failed!")
                }
            } catch (e: Exceptions.LoginException) {
                snackbarHostState.showSnackbar("${e.message}")
            }
            loading = false
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            LoginScreenContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.DarkGray),
                email = email,
                password = password,
                loading = loading,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onSubmit = {
                    onSignIn(email, password)
                },
                isSubmitEnabled = isSubmitEnabled
            )
        }
    )


}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    loading: Boolean,
    isSubmitEnabled: Boolean,
    onEmailChange: (value: String) -> Unit,
    onPasswordChange: (value: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager by rememberUpdatedState(LocalFocusManager.current)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login", style = TextStyle(
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        EmailTextField(
            email = email,
            onEmailChange = onEmailChange,
            loading = loading,
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
        Spacer(modifier = Modifier.padding(4.dp))
        PasswordTextField(
            password = password,
            onPasswordChange = onPasswordChange,
            loading = loading,
            onDone = { focusManager.clearFocus() }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                onSubmit()
            },
            shape = ButtonDefaults.shape(),
            enabled = isSubmitEnabled,

            ) {
            Text(
                text = "Anmelden", style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
            )
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (value: String) -> Unit,
    loading: Boolean,
    onDone: () -> Unit = {}
) {
    val passwordVisibility by remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(text = "Passwort", color = MaterialTheme.colorScheme.secondary) },
        modifier = Modifier
            .autofill(
                autofillTypes = listOf(AutofillType.Password),
                onFill = onPasswordChange
            )
            .width(300.dp),
        singleLine = true,
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() },
        ),

        enabled = !loading
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailTextField(
    email: String,
    onEmailChange: (value: String) -> Unit,
    loading: Boolean,
    onNext: () -> Unit = {}
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(text = "E-Mail", color = MaterialTheme.colorScheme.secondary) },
        modifier = Modifier
            .autofill(
                autofillTypes = listOf(AutofillType.Username),
                onFill = onEmailChange
            )
            .width(300.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() },
        ),
        enabled = !loading
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