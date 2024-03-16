package com.funny.translation.translate.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.ui.widget.ExpandMoreButton

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.expandableStickyRow(
    title: String,
    expand: Boolean,
    updateExpand: (Boolean) -> Unit,
    contentList: LazyListScope.() -> Unit
) {
    stickyHeader {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { updateExpand(!expand) }
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp
            )
            ExpandMoreButton(expand = expand, onClick = {
                updateExpand(!expand)
            })
        }
    }

    if (expand) contentList()
}