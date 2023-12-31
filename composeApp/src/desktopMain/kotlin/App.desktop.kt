
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funny.translation.helper.toastState
import com.funny.translation.ui.toast.ToastUI


@Composable
actual fun Toast(modifier: Modifier) {
    ToastUI(toastState, modifier)
}