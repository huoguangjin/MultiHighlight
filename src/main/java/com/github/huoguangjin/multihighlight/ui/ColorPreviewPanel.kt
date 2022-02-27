package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorMarkupModel
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

/**
 * [com.intellij.application.options.colors.SimpleEditorPreview]
 */
class ColorPreviewPanel : PreviewPanel {

  private val editor: Editor

  init {
    val editorFactory = EditorFactory.getInstance()
    editor = editorFactory.createViewer(editorFactory.createDocument(""))

    editor.settings.apply {
      isLineMarkerAreaShown = false
      isIndentGuidesShown = false
    }

    (editor.markupModel as EditorMarkupModel).isErrorStripeVisible = true
  }

  override val panel: JComponent = editor.component

  override fun updateView(textAttrs: List<NamedTextAttr>) {
    UIUtil.invokeLaterIfNeeded {
      if (editor.isDisposed) {
        return@invokeLaterIfNeeded
      }

      editor.markupModel.removeAllHighlighters()
      editor.selectionModel.removeSelection()

      val text = textAttrs.joinToString("\n") { it.name }
      ApplicationManager.getApplication().runWriteAction {
        editor.document.setText(text)
      }

      textAttrs.forEachIndexed(::highlightLine)
    }
  }

  private fun highlightLine(index: Int, textAttr: NamedTextAttr) {
    UIUtil.invokeAndWaitIfNeeded(Runnable {
      try {
        val markupModel = editor.markupModel
        val doc = markupModel.document
        val lineStartOffset = doc.getLineStartOffset(index)
        val lineEndOffset = doc.getLineEndOffset(index)

        // IDEA-53203: add ERASE_MARKER for manually defined attributes
        markupModel.addRangeHighlighter(
          lineStartOffset, lineEndOffset,
          MultiHighlightManager.MULTIHIGHLIGHT_LAYER, TextAttributes.ERASE_MARKER,
          HighlighterTargetArea.EXACT_RANGE
        )

        markupModel.addRangeHighlighter(
          lineStartOffset, lineEndOffset,
          MultiHighlightManager.MULTIHIGHLIGHT_LAYER, textAttr,
          HighlighterTargetArea.EXACT_RANGE
        )
      } catch (e: Exception) {
        throw RuntimeException(e)
      }
    })
  }

  override fun blinkSelectedColor(textAttr: NamedTextAttr) {
  }

  private fun stopBlinking() {
  }

  override fun disposeUIResources() {
    EditorFactory.getInstance().releaseEditor(editor)
    stopBlinking()
  }
}
