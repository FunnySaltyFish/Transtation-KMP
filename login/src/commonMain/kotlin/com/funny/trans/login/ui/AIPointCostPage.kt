package com.funny.trans.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.bean.showWithUnit
import com.funny.translation.helper.Log
import com.funny.translation.helper.buildSearchAnnotatedString
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.isCommonBuild
import com.funny.translation.kmp.paging.items
import com.funny.translation.kmp.viewModel
import com.funny.translation.login.strings.ResStrings
import com.funny.translation.network.service.AICostType
import com.funny.translation.network.service.AIPointCost
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.DateRangePickerDialog
import com.funny.translation.ui.HintText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AIPointCostPage"

enum class AICostSortType(val value: String) {
    Date("time"),
    Cost("cost"),
    Tokens("input_tokens")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPointCostPage() {
    val vm: AIPointCostViewModel = viewModel()
    val costs = vm.costs.collectAsLazyPagingItems()

    var sortType by rememberDataSaverState(key = "ai_cost_page_sort_type", initialValue = AICostSortType.Date)
    var sortOrder by rememberDataSaverState(key = "ai_cost_page_sort_order", initialValue = 1)
    var showDatePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val now = remember { Date().also {
        Log.d(TAG, "year: ${it.year}, ${it.year + 1900}")
    } }

    val dateRangePickerState = rememberDateRangePickerState(
        initialDisplayMode = DisplayMode.Picker,
        initialSelectedStartDateMillis = defaultStartTime,
        initialSelectedEndDateMillis = now.time,
        yearRange = 2023..(now.year + 1900),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= now.time
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= 2023 && year <= now.year + 1900
            }
        }
    )

    var startDate: Long? by rememberStateOf(
        dateRangePickerState.selectedStartDateMillis
    )

    var endDate: Long? by rememberStateOf(
        dateRangePickerState.selectedEndDateMillis
    )

    LaunchedEffect(sortType, sortOrder, searchQuery, startDate, endDate) {
        vm.updateFilters(
            sortType = sortType,
            sortOrder = sortOrder,
            modelName = searchQuery,
            startDate = startDate,
            endDate = endDate
        )
        costs.refresh()
    }

    CommonPage(
        title = ResStrings.ai_cost_title,
        actions = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, ResStrings.select_date)
            }
            SortMenu(
                currentSort = sortType,
                currentOrder = sortOrder,
                onSortChange = { type, order ->
                    sortType = type
                    sortOrder = order
                }
            )
        }
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(ResStrings.search_model_name) },
            leadingIcon = { Icon(Icons.Default.Search, ResStrings.search) }
        )

        if (showDatePicker) {
            DateRangePickerDialog(
                state = dateRangePickerState,
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { start, end ->
                    showDatePicker = false
                    startDate = start ?: defaultStartTime
                    endDate = end ?: now.time
                    // dateRangePickerState.setSelection(start, end)
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                HintText(ResStrings.ai_cost_hint, modifier = Modifier.fillParentMaxWidth())
            }
            items(
                costs,
                contentType = { it.cost_type }
            ) { cost: AIPointCost ->
                AIPointCostItem(cost = cost, filter = searchQuery)
            }

            when {
                costs.loadState.refresh is LoadState.Loading -> {
                    item { LoadingItem() }
                }
                costs.loadState.refresh is LoadState.Error -> {
                    item { ErrorItem { costs.refresh() } }
                }
                costs.loadState.append is LoadState.Loading -> {
                    item { LoadingItem() }
                }
                costs.loadState.append is LoadState.Error -> {
                    item { ErrorItem { costs.retry() } }
                }
            }
        }
    }
}

@Composable
fun AIPointCostItem(
    cost: AIPointCost,
    filter: String = "",
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
            Text(buildSearchAnnotatedString(content = cost.model_name, search = filter))
       },
        supportingContent = {
            Text(ResStrings.token_count.format(
                input = cost.input_tokens.toString(),
                output = cost.output_tokens.toString()
            ))
        },
        leadingContent = {
            Icon(
                modifier = Modifier.fillMaxHeight().wrapContentHeight(Alignment.CenterVertically),
                imageVector = when (cost.cost_type) {
                    AICostType.AskOrTranslate -> Icons.AutoMirrored.Filled.Chat
                    AICostType.ImageTranslate -> Icons.Default.Image
                    AICostType.TTS -> Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = null
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = cost.cost.showWithUnit(6),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(cost.time),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
    )
}

@Composable
private fun SortMenu(
    currentSort: AICostSortType,
    currentOrder: Int,
    onSortChange: (AICostSortType, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Sort, ResStrings.sort)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (type in AICostSortType.entries) {
                for (order in listOf(1, -1)) {
                    val sortText = when (type) {
                        AICostSortType.Date -> ResStrings.sort_by_date
                        AICostSortType.Cost -> ResStrings.sort_by_cost
                        AICostSortType.Tokens -> ResStrings.sort_by_tokens
                    }
                    val orderText = if (order > 0) ResStrings.sort_ascending else ResStrings.sort_descending

                    DropdownMenuItem(
                        text = {
                            Text(ResStrings.sort_pattern.format(
                                sortType = sortText,
                                order = orderText
                            ))
                        },
                        onClick = {
                            onSortChange(type, order)
                            expanded = false
                        },
                        trailingIcon = {
                            if (currentSort == type && currentOrder == order) {
                                Icon(Icons.Default.Check, null)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorItem(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ResStrings.load_failed)
        Button(onClick = onRetry) {
            Text(ResStrings.retry)
        }
    }
}

// Common: 2024-07-17 15:57:05.381
// Google: 2024-10-01 00:00:00
private val defaultStartTime = if (isCommonBuild) 1721203025381 else 1727712000000