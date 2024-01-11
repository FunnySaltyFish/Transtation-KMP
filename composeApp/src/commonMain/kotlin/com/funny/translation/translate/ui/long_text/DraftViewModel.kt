package com.funny.translation.translate.ui.long_text

import com.funny.translation.translate.database.Draft
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.draftDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class DraftViewModel: ViewModel() {
    private val dao = appDB.draftDao
    val draftList = appDB.draftDao.getAll()

    fun deleteDraft(draft: Draft) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(draft.id)
        }
    }
}