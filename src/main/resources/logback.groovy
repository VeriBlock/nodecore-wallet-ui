import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.core.spi.FilterReply
import static ch.qos.logback.classic.Level.INFO

println("started logback")
println("user.home=" + System.getProperty("user.home"))
println("file.separator=" + System.getProperty("file.separator"))
def logRootPath = System.getProperty("logging.path", System.getenv('NODECORE_UI_LOG_PATH')) ?:
(
    System.getProperty("user.home") + System.getProperty("file.separator")
    + 'nodecore-wallet-ui-0.4' + System.getProperty("file.separator")
    + 'logs' + System.getProperty("file.separator")
)

def logLevel = System.getProperty("logging.level", System.getenv('NODECORE_UI_LOG_LEVEL')) ?: ''

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date{YYYY-MM-dd HH:mm:ss.SSSXX} [%thread] %level %logger{36} - %msg%n"
    }
}
appender("FILE", RollingFileAppender) {
    file = "${logRootPath}veriblock.wallet.log"

    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "${logRootPath}veriblock.wallet-%d{yyyy-MM-dd}.%i.log"
        maxHistory = 30
        maxFileSize = "10MB"
        totalSizeCap = "1GB"
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%date{YYYY-MM-dd HH:mm:ss.SSSXX} %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}

appender("FILE-ERROR", FileAppender) {
    file = "${logRootPath}veriblock.wallet-error.log"
    filter(LevelFilter) {
        level = ERROR
        onMatch = FilterReply.ACCEPT
        onMismatch = FilterReply.DENY
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%date{YYYY-MM-dd HH:mm:ss.SSSXX} %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}

def level = toLevel(logLevel, INFO)

logger("com.j256.ormlite.table.TableUtils", WARN)
//logger("veriblock", INFO, ["STDOUT", "FILE", "FILE-ERROR"])
root(INFO, ["STDOUT", "FILE", "FILE-ERROR"])

println("Log path set to ${logRootPath} using level ${level}")