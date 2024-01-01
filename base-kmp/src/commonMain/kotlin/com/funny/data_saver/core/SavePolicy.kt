package com.funny.data_saver.core

/**
 * Controls whether and when to do data persistence. Includes [IMMEDIATELY], [DISPOSED] and [NEVER] by default.
 *
 * 控制是否做、什么时候做数据持久化
 */
open class SavePolicy {
    /**
     * Default mode, do data persistence every time you assign a new value to the state.
     *
     * 默认模式，每次给state的value赋新值时就做持久化
     */
    object IMMEDIATELY : SavePolicy()

    /**
     * do data persistence when the Composable enters `onDispose`. NOTE: USE THIS MODE CAREFULLY, BECAUSE SOMETIME
     * `onDispose` WILL NOT BE CALLED
     *
     * Composable `onDispose` 时做数据持久化，适合数据变动比较频繁、且此Composable会进入onDispose的情况。
     * **慎用此模式，因为有些情况下onDispose不会被回调**
     */
    object DISPOSED: SavePolicy()

    /**
     * NEVER do data persistence automatically. Please call `state.saveData()` manually.
     *
     * 不会自动做持久化操作，请自行调用`state.saveData()`。
     *
     * Example: `onClick = { state.saveData() }`
     */
    object NEVER : SavePolicy()
}