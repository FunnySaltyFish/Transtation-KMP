
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.theme.TransTheme
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import moe.tlaster.precompose.PreComposeApp
import org.intellij.lang.annotations.Language
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Language("Markdown")
const val markdown =  """
# 标题
## 二级
### 三级
#### 四级
##### 五级


这是一个段落。

## 列表

- 无序列表项 1
- 无序列表项 2
- 无序列表项 3

1. 有序列表项 1
2. 有序列表项 2
3. 有序列表项 3

## 引用

> 这是一段引用文本。

## 链接与图片

[Markdown 教程](https://markdown.com.cn/cheat-sheet.html#%E6%89%A9%E5%B1%95%E8%AF%AD%E6%B3%95)

![Markdown Logo](https://tse2-mm.cn.bing.net/th/id/OIP-C.LH5NQfwpWSFoFF77TCf4JQHaF3?rs=1&pid=ImgDetMain)

## 代码块

```python
print("Hello, World!")
```

## 表格

| 姓名 | 年龄 |
|------|------|
| 张三 | 25   |
| 李四 | 30   |

## 加粗与斜体

这是**加粗**的文本，这是*斜体*的文本。

## 分割线

---

## 注释

[^1]: 这是一个注释。

## 公式

行内公式：$ E=mc^2 $

块状公式：

$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$
"""

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    CompositionLocalProvider(
        LocalDataSaver provides DataSaverUtils,
        LocalImageLoader provides remember { generateImageLoader() },
    ) {
        PreComposeApp {
            TransTheme {
                Box(Modifier.fillMaxSize()) {
                    var greetingText by remember { mutableStateOf("Hello World!") }
                    var showImage by remember { mutableStateOf(false) }
                    val context = LocalKMPContext.current
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            greetingText = "Compose: ${Greeting().greet()}"
                            showImage = !showImage
                            context.toastOnUi("测试toast")
                        }) {
                            Text(greetingText)
                        }

                        var switch by rememberDataSaverState<Boolean>(
                            key = "switch",
                            initialValue = false
                        )
                        Switch(checked = switch, onCheckedChange = { switch = it })

                        MarkdownText(markdown)
                    }

                    Toast(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
expect fun Toast(modifier: Modifier = Modifier)

expect fun generateImageLoader(): ImageLoader