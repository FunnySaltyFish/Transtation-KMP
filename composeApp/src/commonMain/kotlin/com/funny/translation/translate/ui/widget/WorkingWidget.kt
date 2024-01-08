package com.funny.translation.translate.ui.widget

//@Composable
//fun ComingSoon() {
//    var supportDialog by remember {
//        mutableStateOf(false)
//    }
//    Surface {
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(50.dp)
//        ) {
//            LottieView(R.raw.working, modifier = Modifier.height(360.dp))
//            Text(
//                text = ResStrings.comming_soon,
//                style = typography.h5,
//                modifier = Modifier
//                    .padding(12.dp)
//                    .fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//            Text(
//                text = "软件仍在建设中\n点击此处给勤劳的开发者点个支持吧",
//                style = typography.subtitle2,
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        supportDialog = true
//                    }
//            )
//        }
//    }
//    if(supportDialog){
//        AlertDialog(
//            onDismissRequest = {  },
//            title = {
//                Text("赞助")
//            },
//            text = {
//                Image(
//                    painter = painterResource(id = R.mipmap.sponser),
//                    ResStrings.sponser,
//                    modifier = Modifier.fillMaxWidth(),
//                    alignment = Alignment.Center,
//                )
//            },
//            buttons = {
//                Button(onClick = { supportDialog = false }, modifier = Modifier.fillMaxWidth(), colors = buttonColors(backgroundColor = MaterialTheme.colorScheme.surface)) {
//                    Text("关闭", color = MaterialTheme.colorScheme.onSurface)
//                }
//            }
//        )
//    }
//}