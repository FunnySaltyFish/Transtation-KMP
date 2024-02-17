package com.funny.translation.buildlogic

import app.cash.sqldelight.gradle.SqlDelightExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.setupSqlDelight() {
    /**
     * sqldelight {
     *     databases {
     *         create("Database") {
     *             packageName.set("com.funny.translation.database")
     *         }
     *     }
     * }
     *
     */
    pluginManager.apply("app.cash.sqldelight")

    configure<SqlDelightExtension> {
        databases.create("Database") {
            packageName.set("com.funny.translation.database")
        }
    }
}