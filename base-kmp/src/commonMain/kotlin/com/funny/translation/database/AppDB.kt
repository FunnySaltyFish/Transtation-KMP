package com.funny.translation.database

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    val database = Database(
        driver,
        transHistoryAdapter = TransHistory.Adapter(
            sourceLanguageIdAdapter = IntColumnAdapter,
            targetLanguageIdAdapter = IntColumnAdapter,
            engineNamesAdapter = StringListAdapter,
        )
    )

    // Do more work with the database (see below).
    return database
}

expect val appDB: Database