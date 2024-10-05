package com.funny.translation.translate.ui.long_text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.Log
import com.funny.translation.translate.database.LongTextTransTaskMini
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.longTextTransDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "LongTextTransListVM"
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
            Log.d(TAG, "updateRemark: taskId = $taskId, newRemark = $newRemark")
            try {
                appDB.longTextTransTasksQueries.updateRemark(id = taskId, remark = newRemark)
            } catch (e: Exception) {
                Log.e(TAG, "updateRemark: ", e)
            }
        }
    }
}