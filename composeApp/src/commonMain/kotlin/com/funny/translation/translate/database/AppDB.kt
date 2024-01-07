package com.funny.translation.translate.database

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.funny.translation.database.Database
import com.funny.translation.database.StringListAdapter
import com.funny.translation.database.TransHistory
import com.funny.translation.helper.now

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


private fun test() {
    appDB.transHistoryQueries.queryAllBetween(0, now()).executeAsList()
}