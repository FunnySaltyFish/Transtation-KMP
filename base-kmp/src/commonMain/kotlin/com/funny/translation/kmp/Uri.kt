package com.funny.translation.kmp

import com.eygraber.uri.Uri as KMPUri

expect fun KMPUri.writeText(text: String)

expect fun KMPUri.readText(): String