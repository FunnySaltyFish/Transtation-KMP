package com.funny.translation.codeeditor.ui.editor

import io.github.rosemoe.editor.interfaces.AutoCompleteProvider
import io.github.rosemoe.editor.langs.IdentifierAutoComplete
import io.github.rosemoe.editor.langs.IdentifierAutoComplete.Identifiers
import io.github.rosemoe.editor.langs.universal.LanguageDescription
import io.github.rosemoe.editor.langs.universal.UniversalLanguage
import io.github.rosemoe.editor.langs.universal.UniversalTokens
import io.github.rosemoe.editor.struct.BlockLine
import io.github.rosemoe.editor.text.LineNumberCalculator
import io.github.rosemoe.editor.text.TextAnalyzeResult
import io.github.rosemoe.editor.text.TextAnalyzer
import io.github.rosemoe.editor.widget.EditorColorScheme
import java.util.Stack

class PluginLanguage(languageDescription: LanguageDescription?) :
    UniversalLanguage(languageDescription) {
    var helper: LineNumberCalculator? = null
    var autoComplete: IdentifierAutoComplete

    init {
        autoComplete = IdentifierAutoComplete()
        autoComplete.keywords = mLanguage.keywords
        autoComplete.setKeywordsAreLowCase(false)
        //        identifiers.begin();
//        for (Language language : Language.values()){
//            identifiers.addIdentifier("LANGUAGE_"+language.name());
//        }
    }

    override fun getAutoCompleteProvider(): AutoCompleteProvider {
        return autoComplete
    }

    override fun analyze(
        content: CharSequence,
        colors: TextAnalyzeResult,
        delegate: TextAnalyzer.AnalyzeThread.Delegate
    ) {
        val tokenizer = getTokenizer()
        tokenizer.setInput(content)
        helper = LineNumberCalculator(content)
        val identifiers = Identifiers()
        identifiers.begin()
        var maxSwitch = 0
        var layer = 0
        var currSwitch = 0
        try {
            var token: UniversalTokens?
            val stack = Stack<BlockLine>()
            while (tokenizer.nextToken().also { token = it } != UniversalTokens.EOF) {
                val index = tokenizer.getOffset()
                val line = helper!!.line
                val column = helper!!.column
                when (token) {
                    UniversalTokens.KEYWORD -> colors.addIfNeeded(
                        line,
                        column,
                        EditorColorScheme.KEYWORD
                    )

                    UniversalTokens.IDENTIFIER -> {
                        identifiers.addIdentifier(
                            content.substring(
                                index,
                                index + tokenizer.getTokenLength()
                            )
                        )
                        colors.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL)
                    }

                    UniversalTokens.LITERAL -> colors.addIfNeeded(
                        line,
                        column,
                        EditorColorScheme.LITERAL
                    )

                    UniversalTokens.LINE_COMMENT, UniversalTokens.LONG_COMMENT -> colors.addIfNeeded(
                        line,
                        column,
                        EditorColorScheme.COMMENT
                    )

                    UniversalTokens.OPERATOR -> {
                        colors.addIfNeeded(line, column, EditorColorScheme.OPERATOR)
                        if (mLanguage.isSupportBlockLine) {
                            val op = content.substring(index, index + tokenizer.getTokenLength())
                            if (mLanguage.isBlockStart(op)) {
                                val blockLine = colors.obtainNewBlock()
                                blockLine.startLine = line
                                blockLine.startColumn = column
                                stack.add(blockLine)
                                if (layer == 0) {
                                    currSwitch = 1
                                } else {
                                    currSwitch++
                                }
                                layer++
                            } else if (mLanguage.isBlockEnd(op)) {
                                if (!stack.isEmpty()) {
                                    val blockLine = stack.pop()
                                    blockLine.endLine = line
                                    blockLine.endColumn = column
                                    colors.addBlockLine(blockLine)
                                    if (layer == 1) {
                                        if (currSwitch > maxSwitch) {
                                            maxSwitch = currSwitch
                                        }
                                    }
                                    layer--
                                }
                            }
                        }
                    }

                    UniversalTokens.WHITESPACE, UniversalTokens.NEWLINE -> colors.addNormalIfNull()
                    UniversalTokens.UNKNOWN -> colors.addIfNeeded(
                        line,
                        column,
                        EditorColorScheme.ANNOTATION
                    )

                    else -> {}
                }
                helper!!.update(tokenizer.getTokenLength())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        colors.determine(helper!!.line)
        identifiers.finish()
        colors.mExtra = identifiers
        tokenizer.setInput(null)
        if (currSwitch > maxSwitch) {
            maxSwitch = currSwitch
        }
        colors.suppressSwitch = maxSwitch + 50
    }
}
