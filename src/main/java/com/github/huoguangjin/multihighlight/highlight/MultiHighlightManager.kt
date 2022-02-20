package com.github.huoguangjin.multihighlight.highlight

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.MarkupModelEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Segment
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil

class MultiHighlightManager(
  private val project: Project
) {

  fun isClearHighlights(e: Editor): Boolean {
    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(e)

    val caretOffset = editor.caretModel.offset
    val highlighters = getHighlighters(editor)
    return highlighters.any {
      TextRange.create(it).grown(1).contains(caretOffset)
    }
  }

  fun addHighlighters(editor: Editor, textAttr: TextAttributes, textRanges: Collection<TextRange>) {
    val map = getHighlightInfo(editor, true)!!
    val info = MultiHighlightInfo(editor)

    val markupModel = editor.markupModel
    textRanges.forEach { textRange ->
      val highlighter = markupModel.addRangeHighlighter(
        textRange.startOffset,
        textRange.endOffset,
        MULTIHIGHLIGHT_LAYER,
        textAttr,
        HighlighterTargetArea.EXACT_RANGE
      )

      map[highlighter] = info
    }
  }

  fun removeHighlighters(e: Editor, textRanges: MutableList<TextRange>) {
    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(e)

    val highlighters = getHighlighters(editor)
    if (highlighters.isEmpty()) {
      return
    }

    highlighters.sortWith(Comparator.comparingInt(Segment::getStartOffset))
    textRanges.sortWith(Comparator.comparingInt(Segment::getStartOffset))

    var i = 0
    var j = 0
    while (i < highlighters.size && j < textRanges.size) {
      val highlighter = highlighters[i]
      val highlighterRange = TextRange.create(highlighter)
      val textRange = textRanges[j]

      if (textRange == highlighterRange) {
        removeHighlighter(editor, highlighter)
        i++
      } else if (textRange.startOffset > highlighterRange.endOffset) {
        i++
      } else if (textRange.endOffset < highlighterRange.startOffset) {
        j++
      } else {
        i++
        j++
      }
    }
  }

  fun removeHighlighter(editor: Editor, highlighter: RangeHighlighter): Boolean {
    val map = getHighlightInfo(editor, false) ?: return false
    val info = map[highlighter] ?: return false

    val markupModel = info.editor.markupModel as MarkupModelEx
    if (markupModel.containsHighlighter(highlighter)) {
      highlighter.dispose()
    }

    map.remove(highlighter)
    return true
  }

  private fun getHighlighters(editor: Editor): Array<out RangeHighlighter> {
    val map = getHighlightInfo(editor, false) ?: return RangeHighlighter.EMPTY_ARRAY
    return map.keys.toTypedArray()
  }

  private fun getHighlightInfo(editor: Editor, toCreate: Boolean): MutableMap<RangeHighlighter, MultiHighlightInfo>? {
    var map = editor.getUserData(MULTIHIGHLIGHT_INFO_KEY)

    if (map == null && toCreate) {
      map = mutableMapOf()
      editor.putUserData(MULTIHIGHLIGHT_INFO_KEY, map)
    }

    return map
  }

  class MultiHighlightInfo(val editor: Editor)

  companion object {

    const val MULTIHIGHLIGHT_LAYER = HighlighterLayer.SELECTION - 1

    private val MULTIHIGHLIGHT_INFO_KEY: Key<MutableMap<RangeHighlighter, MultiHighlightInfo>> =
      Key.create("MULTIHIGHLIGHT_INFO_KEY")

    @JvmStatic
    fun getInstance(project: Project): MultiHighlightManager {
      return project.getService(MultiHighlightManager::class.java)
    }
  }
}
