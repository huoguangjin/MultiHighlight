package com.github.huoguangjin.multihighlight.highlight

import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.range

class MultiHighlightTextHandler(
  private val project: Project,
  private val editor: Editor,
  private val psiFile: PsiFile?,
) {

  fun highlight(text: String) {
    val multiHighlightManager = MultiHighlightManager.getInstance(project)

    val highlighter = multiHighlightManager.findHighlightAtCaret(editor)
    highlighter?.range?.let { highlightedRange ->
      if (!highlightedRange.isEmpty) {
        val highlightedText = editor.document.getText(highlightedRange)
        if (highlightedText.isNotEmpty()) {
          val textRanges = findText(highlightedText)
          multiHighlightManager.removeHighlighters(editor, textRanges)
          return
        }
      }
    }

    if (text.isEmpty()) {
      return
    }

    val textRanges = findText(text)
    val textAttr = TextAttributesFactory.getNextTextAttr()
    multiHighlightManager.addHighlighters(editor, textAttr, textRanges)
  }

  private fun findText(text: String): MutableList<TextRange> {
    val findManager = FindManager.getInstance(project)
    val charSequence = editor.document.immutableCharSequence
    val maxOffset = charSequence.length

    val findModel = FindModel().apply {
      copyFrom(findManager.findInFileModel)
      stringToFind = text
    }
    val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)

    var offset = 0
    val textRanges = mutableListOf<TextRange>()
    while (true) {
      val result = findManager.findString(charSequence, offset, findModel, virtualFile)
      if (!result.isStringFound) {
        break
      }

      val newOffset = result.endOffset
      if (newOffset > maxOffset) {
        break
      }

      if (offset == newOffset) {
        if (offset < maxOffset - 1) {
          offset += 1
        } else {
          // reach document end
          textRanges.add(result)
          break
        }
      } else {
        offset = newOffset
        if (offset == result.startOffset) {
          // result.startOffset == result.endOffset, skip zero width range
          offset += 1
        }
      }

      textRanges.add(result)
    }

    return textRanges
  }
}
