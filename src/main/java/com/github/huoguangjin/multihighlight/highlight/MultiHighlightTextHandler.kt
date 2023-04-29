package com.github.huoguangjin.multihighlight.highlight

import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.WindowManager
import java.text.MessageFormat

class MultiHighlightTextHandler(
  private val project: Project,
  private val editor: Editor,
  private val textAttr: TextAttributes,
) {
  constructor(
    project: Project,
    editor: Editor,
  ) : this(project, editor, TextAttributesFactory.getNextTextAttr())

  fun highlight() {
    val selectionModel = editor.selectionModel
    if (!selectionModel.hasSelection()) {
      selectionModel.selectWordAtCaret(false)
    }

    val selectedText = selectionModel.selectedText ?: return
    highlightText(selectedText)
  }

  fun highlightText(text: String) {
    if (text.isEmpty()) {
      return
    }

    val multiHighlightManager = MultiHighlightManager.getInstance()
    val textRanges = findText(text)

    if (editor is EditorWindow) {
      // The text ranges are found in the injected editor which is an EditorWindow. Different EditorWindows created from
      // the same injected document (DocumentWindow) are not referenced in the EditorFactory, but their host editors can
      // be referenced by the same host document. Highlight the text ranges in the host editors so that we can also
      // add/remove highlighters across editors.
      // see: [com.intellij.openapi.editor.impl.EditorFactoryImpl.createEditor]
      val hostEditor = editor.delegate
      val injectedDocument = editor.document
      val hostTextRanges = textRanges.map(injectedDocument::injectedToHost)
      multiHighlightManager.addHighlighters(hostEditor, textAttr, hostTextRanges)
    } else {
      multiHighlightManager.addHighlighters(editor, textAttr, textRanges)
    }

    val highlightCount = textRanges.size
    WindowManager.getInstance().getStatusBar(project).info = if (highlightCount > 0) {
      MessageFormat.format("{0} {0, choice, 1#text|2#texts} highlighted", highlightCount)
    } else {
      "No texts highlighted"
    }
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
