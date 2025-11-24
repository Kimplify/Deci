import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import org.kimplify.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }