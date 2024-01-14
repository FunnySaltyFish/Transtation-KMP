package com.funny.translation.translate.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.funny.translation.database.Database
import com.funny.translation.helper.CacheManager

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dir = CacheManager.baseDir.absolutePath
        val driver: SqlDriver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${dir}/app_db.db",
        )
        Database.Schema.create(driver)
        return driver
    }
}

actual val appDB: Database by lazy {
    createDatabase(DriverFactory())
}