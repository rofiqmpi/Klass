import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.mpi.klass.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.getElementById("ComposeTarget") ?: document.body!!
    ComposeViewport(root) {
        App()
    }
}
