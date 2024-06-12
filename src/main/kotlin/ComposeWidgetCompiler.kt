import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.validateArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import kotlin.collections.listOf
import kotlin.io.walkTopDown
import kotlin.io.readBytes
import kotlin.io.deleteRecursively
import kotlin.text.capitalize
import java.nio.file.attribute.FileAttribute
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PlainTextMessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.cli.jvm.compiler.CompileEnvironmentException
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files

object ComposeWidgetCompiler {
    fun compileWidget(source: String, classpath: Collection<File>): WidgetCompilationResult {

        val collector = DiagnosticCollector()

        val tmpSourceFile = Files.createTempFile(
            "build",
            ".kt",
            *arrayOfNulls<FileAttribute<*>?>(0)
        )
        val tmpOutputDir = Files.createTempDirectory(
            "build",
            *arrayOfNulls<FileAttribute<*>?>(0)
        )

        tmpSourceFile.toFile().writeText(source)

        try {
            val compiler = K2JVMCompiler()
            val errStream = System.err
            val messageRenderer = MessageRenderer.PLAIN_RELATIVE_PATHS
            val arguments = compiler.createArguments()
            arguments.noStdlib = true
            arguments.includeRuntime = false
            arguments.classpath = classpath.map { it.absolutePath }.joinToString(":")
            arguments.destination = tmpOutputDir.toFile().absolutePath
            arguments.freeArgs = listOf(tmpSourceFile.toFile().absolutePath)
            arguments.optIn = arrayOf("kotlin.RequiresOptIn")
            arguments.pluginClasspaths = listOf("./compiler-1.4.5.jar").toTypedArray()
     //       val collector = PrintingMessageCollector(errStream, messageRenderer, arguments.verbose)
            try {
                errStream.print(messageRenderer.renderPreamble())
                val errorMessage = validateArguments(arguments.errors)
                if (errorMessage != null) {
                    collector.report(CompilerMessageSeverity.ERROR, errorMessage, null)
                    collector.report(CompilerMessageSeverity.INFO, "Use -help for more information", null)
                    return WidgetCompilationResult(null, collector.getDiagnostics())
                }
                val code = compiler.exec(collector, Services.EMPTY, arguments)
                if (code != ExitCode.OK) {
                    collector.report(CompilerMessageSeverity.ERROR, "Compile failure: Non-zero exit code: $code", null)
                    return WidgetCompilationResult(null, collector.getDiagnostics())
                }
            } finally {
                errStream.print(messageRenderer.renderConclusion())
            }
        } catch (e: CompileEnvironmentException) {
            System.err.println(e.message)
            collector.report(CompilerMessageSeverity.ERROR, e.message!!, null)
            return WidgetCompilationResult(null, collector.getDiagnostics())
        }



        val files = mutableMapOf<String, ByteArray>()
        tmpOutputDir.toFile()!!.walkTopDown().forEach {
            if (it.isFile && it.name.endsWith(".class")) {
                    files[it.relativeTo(tmpOutputDir.toFile()).path.removeSuffix(".class")] = it.readBytes()
            }
        }

        tmpOutputDir.toFile()!!.deleteRecursively()

        val classLoader = ComposableUnitLambda::class.java.classLoader
        val loader = object : URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), classLoader) {
            override fun findClass(name: String): Class<*> {
                val b = files.get(name.replace('.', File.separatorChar))
                return if (b == null) super.findClass(name) else defineClass(name, b, 0, b.size)
            }
        }
        val fileName = tmpSourceFile.toFile().name
        val className = fileName.capitalize().removeSuffix(".kt")+"Kt"
        val cls: Class<*> = loader.loadClass(className)
        val method = cls.methods.single {it.name == "Sample" }!!
        return WidgetCompilationResult(ComposableUnitLambda(method), collector.getDiagnostics())
    }
}

