package com.github.huoguangjin.multihighlight.highlight

import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil

class MultiHighlightTextHandler(
  private val project: Project,
  private val editor: Editor,
  private val psiFile: PsiFile?,
) {

  fun highlight() {
    if (tryRemoveHighlighters()) {
      return
    }

    val selectionModel = editor.selectionModel
    if (!selectionModel.hasSelection()) {
      selectionModel.selectWordAtCaret(false)
    }

    val selectedText = selectionModel.selectedText ?: return
    highlightText(selectedText)
  }

  fun tryRemoveHighlighters(): Boolean {
    // TODO: 2022/4/21 move to MultiHighlightManager
    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(editor)

    val multiHighlightManager = MultiHighlightManager.getInstance()
    val highlighter = multiHighlightManager.findHighlightAtCaret(editor) ?: return false

    multiHighlightManager.removeHighlighters(editor, highlighter)
    return true
  }

  fun highlightText(text: String) {
    if (text.isEmpty()) {
      return
    }

    val multiHighlightManager = MultiHighlightManager.getInstance()
    val textRanges = findText(text)
    val textAttr = TextAttributesFactory.getNextTextAttr()

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
