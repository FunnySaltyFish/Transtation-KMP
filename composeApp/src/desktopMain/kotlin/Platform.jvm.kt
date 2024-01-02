class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

enum class OperatingSystem {
    Windows, Linux, MacOS, Unknown
}

val currentOperatingSystem: OperatingSystem by lazy {
    val sys = System.getProperty("os.name").lowercase()
    if (sys.contains("win")) {
        OperatingSystem.Windows
    } else if (sys.contains("nix") || sys.contains("nux") ||
        sys.contains("aix")
    ) {
        OperatingSystem.Linux
    } else if (sys.contains("mac")) {
        OperatingSystem.MacOS
    } else {
        OperatingSystem.Unknown
    }
}