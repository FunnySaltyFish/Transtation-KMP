package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.funny.compose.loading.DefaultFailure
import com.funny.compose.loading.DefaultLoading
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.Log
import com.funny.translation.kmp.paging.items
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.bean.Sponsor
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.HeadingText
import com.funny.translation.ui.touchToScale
import kotlinx.collections.immutable.toImmutableList

enum class SponsorSortType(val value: String){
    Date("date"), Money("money")
}

data class SpecialThanksBean(
    val name: String,
    val desc: String,
)

private val specialThanksList = arrayListOf(
    SpecialThanksBean("松川吖", ResStrings.ui_bug_advice),
    SpecialThanksBean("随风而行lulu", ResStrings.bug_advice),
    SpecialThanksBean("Vul_Ghost", ResStrings.bug_advice),
    SpecialThanksBean(ResStrings.all_sponsors, ResStrings.thanks_for_support),
).toImmutableList()

@OptIn(ExperimentalFoundationApi::class
)
@Composable
fun ThanksScreen() {
    val vm: ThanksViewModel = viewModel()
    val sponsors = vm.sponsors.collectAsLazyPagingItems()
    var sponsorSortType : SponsorSortType by rememberDataSaverState<SponsorSortType>(
        key = "KEY_SPONSOR_SORT_TYPE",
        initialValue = SponsorSortType.Money
    )
    var sponsorSortOrder by rememberDataSaverState<Int>(
        key = "KEY_SPONSOR_SORT_ORDER",
        initialValue = -1
    ) // 1 升序; -1 降序

    LaunchedEffect(sponsorSortType, sponsorSortOrder){
        vm.updateSort(sponsorSortType, sponsorSortOrder)
        sponsors.refresh()
    }

    CommonPage(title = ResStrings.thanks, enableOverScroll = false) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            stickyHeader {
                HeadingText(text = ResStrings.special_thanks, modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background))
            }
            items(specialThanksList){ bean ->
                SpecialThanksItem(bean = bean)
            }
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeadingText(text = ResStrings.hornor_sponsor)
                    SortSponsor(
                        sortType = sponsorSortType,
                        updateSortType = { sponsorSortType = it },
                        sortOrder = sponsorSortOrder,
                        updateSortOrder = { sponsorSortOrder = it })
                }

            }
            items(sponsors, key = { it.key }) { sponsor ->
                SponsorItem(sponsor = sponsor)
            }
            val loadStates = sponsors.loadState
            when {
                loadStates.refresh is LoadState.Loading -> {
                    item { Box { DefaultLoading() } }
                }
                loadStates.append is LoadState.Loading -> {
                    item { Box { DefaultLoading() } }
                }
                loadStates.refresh is LoadState.Error -> {
                    val e = sponsors.loadState.refresh as LoadState.Error
                    Log.e("refresh error: %s", e.toString())
                    item {
                        DefaultFailure(modifier = Modifier.fillParentMaxSize()) {
                            sponsors.refresh()
                        }
                    }
                }
                loadStates.append is LoadState.Error -> {
                    val e = sponsors.loadState.append as LoadState.Error
                    Log.e("append error: %s", e.toString())
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            DefaultFailure(retry = {
                                sponsors.retry()
                            })
                        }
                    }
                }
            }

            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = ResStrings.sponsor_tip,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(0.5f),
                    lineHeight = 15.sp
                )
            }
        }

    }
}

@Composable
private fun SortSponsor(
    sortType: SponsorSortType,
    updateSortType: (SponsorSortType) -> Unit,
    sortOrder: Int,
    updateSortOrder: (Int) -> Unit
) {
    // Menu
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = {
        expanded = true
    }) {
        FixedSizeIcon(Icons.Default.Sort, contentDescription = ResStrings.sort)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        ) {
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Date)
                updateSortOrder(1)
                expanded = false
            } ,text = {
                Text(text = ResStrings.sort_by_date_asc)
            }, trailingIcon = {
                if(sortType == SponsorSortType.Date && sortOrder == 1){
                    FixedSizeIcon(Icons.Default.Check, contentDescription = ResStrings.sort_by_date_asc)
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Date)
                updateSortOrder(-1)
                expanded = false
            } ,text = {
                Text(text = ResStrings.sort_by_date_desc)
            }, trailingIcon = {
                if(sortType == SponsorSortType.Date && sortOrder == -1){
                    FixedSizeIcon(Icons.Default.Check, contentDescription = ResStrings.sort_by_date_desc)
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Money)
                updateSortOrder(1)
                expanded = false
            } ,text = {
                Text(text = ResStrings.sort_by_money_asc)
            }, trailingIcon = {
                if(sortType == SponsorSortType.Money && sortOrder == 1){
                    FixedSizeIcon(Icons.Default.Check, contentDescription = ResStrings.sort_by_money_asc)
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Money)
                updateSortOrder(-1)
                expanded = false
            } ,text = {
                Text(text = ResStrings.sort_by_money_desc)
            }, trailingIcon = {
                if(sortType == SponsorSortType.Money && sortOrder == -1){
                    FixedSizeIcon(Icons.Default.Check, contentDescription = ResStrings.sort_by_money_desc)
                }
            })
        }
    }
}


@Composable
private fun SpecialThanksItem(
    bean: SpecialThanksBean,
) {
    Row(
        modifier = Modifier
            .touchToScale()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(bean.name, fontSize = 18.sp, fontWeight = FontWeight.W600)
            Text(
                bean.desc,
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(bottom = 8.dp, top = 2.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun SponsorItem(
    sponsor: Sponsor,
) {
    Row(
        modifier = Modifier
            .touchToScale()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(sponsor.name, fontSize = 18.sp, fontWeight = FontWeight.W600)
            sponsor.message?.let {
                Text(
                    it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(bottom = 8.dp, top = 2.dp, end = 4.dp),
                    color = LocalContentColor.current.copy(0.7f)
                )
            }
        }

        val fontSize = rememberSaveable {
            when (sponsor.money.moneyText.length) {
                in 1..5 -> 28
                else -> 24
            }
        }

        Text(
            text = sponsor.money.moneyText,
            Modifier.padding(end = 4.dp),
            fontWeight = W800,
            fontSize = fontSize.sp
        )
    }
}


private val Int.moneyText: String
    get() = String.format("%.2f元", this / 100f)
