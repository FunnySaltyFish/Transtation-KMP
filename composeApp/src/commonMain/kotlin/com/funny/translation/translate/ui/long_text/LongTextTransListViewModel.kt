package com.funny.translation.translate.ui.long_text

import com.funny.translation.translate.database.LongTextTransTaskMini
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.longTextTransDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class LongTextTransListViewModel: ViewModel() {
    private val dao = appDB.longTextTransDao

    val taskList = appDB.longTextTransDao.getAllMini()

    fun deleteTask(task: LongTextTransTaskMini) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(task.id)
        }
    }

    fun updateRemark(taskId: String, newRemark: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateRemark(taskId, newRemark)
        }
    }
}