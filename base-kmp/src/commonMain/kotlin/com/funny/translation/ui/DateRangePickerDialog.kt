package com.funny.translation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.getLocale
import com.funny.translation.kmp.base.strings.ResStrings
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState = rememberDateRangePickerState(),
    onDismissRequest: () -> Unit,
    onDateSelected: (startDate: Long?, endDate: Long?) -> Unit
) {
    AnyPopDialog(
        modifier = Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ).padding(12.dp),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
                onDismissRequest()
            }) {
                Text(ResStrings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(ResStrings.cancel)
            }
        },
        text = {
            val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
            DateRangePicker(
                state = state,
                modifier = Modifier.height(520.dp),
                title = {

                },
                headline = {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        val style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                        val locale = remember { LocaleUtils.getLocale() }
                        val pleaseSelect = ResStrings.please_select
                        Text(
                            text = dateFormatter.formatDate(state.selectedStartDateMillis, locale) ?: pleaseSelect,
                            style = style
                        )
                        Text(text = " - ", style = style)
                        Text(
                            text = dateFormatter.formatDate(state.selectedEndDateMillis, locale) ?: pleaseSelect,
                            style = style
                        )
                    }
                },
                dateFormatter = dateFormatter,
                showModeToggle = true,
                colors = DatePickerDefaults.colors()
            )
        }
    )
}

private fun Long?.toDate() = if (this == null) Date() else Date(this)