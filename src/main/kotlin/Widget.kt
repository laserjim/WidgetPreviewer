import androidx.compose.foundation.layout.Box
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Image
import java.io.File
import java.util.*
import kotlin.jvm.internal.Intrinsics
class Widget(val source: String) {
    val compileResult: WidgetCompilationResult
    val preview: ImageBitmap


    val classpath: List<File> = emptyList() // TODO: Dependencies can go here.

    init {
        compileResult = ComposeWidgetCompiler.compileWidget(source, classpath)
        preview = createScreenshot(compileResult.composable!!)
    }

}

fun createScreenshot(composable: ComposableUnitLambda): ImageBitmap {
    val size = getPreviewSize(composable)
    val window = ImageComposeScene(size.component1(), size.component2())
    window.setContent { composable() }
    return Image.makeFromEncoded(window.render(0).encodeToData()!!.bytes).toComposeImageBitmap()
}

fun getPreviewSize(composable: ComposableUnitLambda): IntSize {
    Intrinsics.checkNotNullParameter(composable, "composable")
    val desiredSize = getDesiredSize(composable, 1024, 768)
    return if (desiredSize.width != 1024 && desiredSize.height != 768) desiredSize else getDesiredSize(
        composable,
        350,
        400
    )
}

fun getDesiredSize(
    composable: ComposableUnitLambda,
    widthConstraint: Int,
    heightConstraint: Int
): IntSize {
    val window = ImageComposeScene(widthConstraint, heightConstraint)
    var size: IntSize? = null
    window.setContent({ Box(Modifier.onGloballyPositioned { size = it.size }) { composable() } })
    window.render()
    return size!!
}
