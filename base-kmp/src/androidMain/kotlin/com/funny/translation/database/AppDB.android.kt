package com.funny.translation.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.funny.translation.helper.Context
import com.funny.translation.kmp.appCtx

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(Database.Schema, context, "app_db_2.db")
    }
}

actual val appDB: Database by lazy {
    createDatabase(DriverFactory(appCtx))
}