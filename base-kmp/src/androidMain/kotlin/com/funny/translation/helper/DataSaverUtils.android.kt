package com.funny.translation.helper

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.mmkv.DefaultDataSaverMMKV

actual val DataSaverUtils: DataSaverInterface = DefaultDataSaverMMKV