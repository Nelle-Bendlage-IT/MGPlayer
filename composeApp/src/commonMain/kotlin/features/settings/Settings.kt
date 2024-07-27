package features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Settings(onLogOutPressed: () -> Unit, innerPadding: PaddingValues) {

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(
            modifier = Modifier.fillMaxWidth().padding(innerPadding),
            shape = RoundedCornerShape(30.dp),
            onClick = { onLogOutPressed() }
        ) {
            Text("Logout")
        }
    }
}

