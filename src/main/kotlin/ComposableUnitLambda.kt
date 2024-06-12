import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import java.lang.reflect.Method
import kotlin.jvm.functions.Function2
import kotlin.jvm.internal.Intrinsics
import kotlin.jvm.internal.Lambda

class ComposableUnitLambda(val method: Method) : Lambda<Unit>(2), Function2<Composer, Int, Unit>, @Composable ()->Unit {
    @Composable
    override operator fun invoke() {}

    override operator fun invoke(composer: Composer, bitmask: Int) {
        Intrinsics.checkNotNullParameter(composer, "composer")
        composer.startMovableGroup(4, method)
        val arrayOfObject = arrayOfNulls<Any>(2)
        arrayOfObject[0] = composer
        arrayOfObject[1] = Integer.valueOf(bitmask)
        method.invoke(null, *arrayOfObject)
        composer.endMovableGroup()
    }
}
