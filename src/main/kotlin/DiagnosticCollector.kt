import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import java.util.ArrayList
import kotlin.jvm.internal.Intrinsics

class DiagnosticCollector : MessageCollector {
    private val diagnostics: MutableList<Diagnostic>
    fun getDiagnostics(): List<Diagnostic> {
        return diagnostics
    }

    override fun clear() {
        diagnostics.clear()
    }

    override fun hasErrors(): Boolean {
        return diagnostics.filter { it.severity == CompilerMessageSeverity.ERROR }.isNotEmpty()
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        Intrinsics.checkNotNullParameter(severity, "severity")
        Intrinsics.checkNotNullParameter(message, "message")
        diagnostics.add(Diagnostic(severity, message, location))
    }

    init {
        val bool = false
        diagnostics = ArrayList()
    }
}

