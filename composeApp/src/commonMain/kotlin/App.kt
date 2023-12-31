
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            var greetingText by remember { mutableStateOf("Hello World!") }
            var showImage by remember { mutableStateOf(false) }
            val context = LocalKMPContext.current
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    greetingText = "Compose: ${Greeting().greet()}"
                    showImage = !showImage
                    context.toastOnUi("测试toast")
                }) {
                    Text(greetingText)
                }
                AnimatedVisibility(showImage) {
                    Image(
                        painterResource("compose-multiplatform.xml"),
                        null
                    )
                }
            }

            Toast(
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }


}

@Composable
expect fun Toast(modifier: Modifier = Modifier)