package com.github.huoguangjin.multihighlight.highlight

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.MarkupIterator
import com.intellij.openapi.editor.ex.MarkupModelEx
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Segment
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.util.containers.UnsafeWeakList
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MultiHighlightManager {

  private val highlightGroupIdGenerator = AtomicInteger()

  fun tryRemoveHighlighterAtCaret(sourceEditor: Editor): Boolean {
    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(sourceEditor)
    val highlighter = findHighlightAtCaret(editor) ?: return false
    removeHighlighters(editor, highlighter)
    return true
  }

  private inline fun MarkupModelEx.useOverlappingIterator(
    startOffset: Int,
    endOffset: Int,
    block: (MarkupIterator<RangeHighlighterEx>) -> Unit
  ) {
    val iterator = overlappingIterator(startOffset, endOffset)
    try {
      return block(iterator)
    } finally {
      iterator.dispose()
    }
  }

  fun findHighlightAtCaret(editor: Editor): RangeHighlighter? {
    val map = getHighlightInfo(editor, false) ?: return null

    val caret = editor.caretModel.currentCaret
    val startOffset: Int
    val endOffset: Int
    if (caret.hasSelection()) {
      startOffset = caret.selectionStart
      endOffset = caret.selectionEnd
    } else {
      startOffset = caret.offset
      endOffset = startOffset
    }

    val markupModel = editor.markupModel as MarkupModelEx
    markupModel.useOverlappingIterator(startOffset, endOffset) {
      it.forEach { highlighter ->
        if (highlighter in map) {
          return highlighter
        }
      }
    }

    return null
  }

  fun addHighlighters(sourceEditor: Editor, textAttr: TextAttributes, textRanges: Iterable<Segment>) {
    val groupId = highlightGroupIdGenerator.incrementAndGet()
    val document = sourceEditor.document
    val editors = EditorFactory.getInstance().editors(document)
    editors.forEach {
      if (it.document == document) {
        addHighlightersForEditor(groupId, it, textAttr, textRanges)
      }
    }
  }

  private fun addHighlightersForEditor(
    groupId: Int,
    editor: Editor,
    textAttr: TextAttributes,
    textRanges: Iterable<Segment>,
  ) {
    val map = getHighlightInfo(editor, true)!!
    val group = MultiHighlightGroup(groupId)

    val markupModel = editor.markupModel
    textRanges.forEach { textRange ->
      val highlighter = markupModel.addRangeHighlighter(
        textRange.startOffset,
        textRange.endOffset,
        MULTIHIGHLIGHT_LAYER,
        textAttr,
        HighlighterTargetArea.EXACT_RANGE
      )

      map[highlighter] = group
      group.highlighters.add(highlighter)
    }
  }

  fun removeHighlighters(sourceEditor: Editor, highlighter: RangeHighlighter) {
    if (!highlighter.isValid) {
      return
    }

    val map = getHighlightInfo(sourceEditor, false) ?: return
    val group = map[highlighter] ?: return

    val groupId = group.id
    val document = sourceEditor.document
    val editors = EditorFactory.getInstance().editors(document)
    editors.forEach {
      if (it.document == document) {
        removeHighlightersForEditor(groupId, it)
      }
    }
  }

  private fun removeHighlightersForEditor(groupId: Int, editor: Editor) {
    val map = getHighlightInfo(editor, false) ?: return

    for ((_, highlightGroup) in map) {
      // find HighlightGroup by groupId
      if (highlightGroup.id != groupId) {
        continue
      }

      val markupModel = editor.markupModel as MarkupModelEx
      highlightGroup.highlighters.forEach { highlighter ->
        if (markupModel.containsHighlighter(highlighter)) {
          highlighter.dispose()
        }

        map.remove(highlighter)
      }

      break
    }
  }

  fun removeAllHighlighters(editor: Editor): Boolean {
    val map = getHighlightInfo(editor, false) ?: return false

    map.forEach { (highlighter, _) ->
      highlighter.dispose()
    }

    map.clear()
    return true
  }

  private fun getHighlightInfo(
    editor: Editor,
    toCreate: Boolean,
  ): MutableMap<RangeHighlighter, MultiHighlightGroup>? {
    var map = editor.getUserData(MULTI_HIGHLIGHT_INFO_KEY)

    if (map == null && toCreate) {
      map = WeakHashMap()
      editor.putUserData(MULTI_HIGHLIGHT_INFO_KEY, map)
    }

    return map
  }

  class MultiHighlightGroup(
    val id: Int,
  ) {
    val highlighters = UnsafeWeakList<RangeHighlighter>()
  }

  companion object {

    const val MULTIHIGHLIGHT_LAYER = HighlighterLayer.SELECTION - 1

    private val MULTI_HIGHLIGHT_INFO_KEY: Key<MutableMap<RangeHighlighter, MultiHighlightGroup>> =
      Key.create("MULTI_HIGHLIGHT_INFO_KEY")

    fun getInstance(): MultiHighlightManager = service()
  }
}
