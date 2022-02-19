package com.github.huoguangjin.multihighlight.highlight

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.codeInsight.highlighting.HighlightManagerImpl
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.featureStatistics.FeatureUsageTracker
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.model.psi.impl.targetSymbols
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiCompiledFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil

class MultiHighlightHandler(
  private val project: Project,
  private val editor: Editor,
  private val psiFile: PsiFile,
) {

  fun highlight() {
    if (highlightCustomUsages()) {
      return
    }

    DumbService.getInstance(project).withAlternativeResolveEnabled {
      if (highlightSymbols()) {
        return@withAlternativeResolveEnabled
      }
    }

    // TODO: 2022/2/12 custom highlight usage handler
    // TODO: 2022/2/12 highlight usage
  }

  fun highlightCustomUsages(): Boolean {
    val handler = HighlightUsagesHandler.createCustomHandler<PsiElement>(editor, psiFile) ?: return false

    handler.featureId?.let(FeatureUsageTracker.getInstance()::triggerFeatureUsed)

    // TODO: 2022/2/13 highlight usage with custom TextAttributes
    handler.highlightUsages()
    // val methods = HighlightUsagesHandlerBase::class.declaredMemberFunctions
    // val selectTargetsMethod = methods.find { it.name == "selectTargets" }
    // thisLogger().debug("selectTargets=selectTargetsMethod")
    return true
  }

  fun highlightSymbols(): Boolean {
    @Suppress("UnstableApiUsage")
    val allTargets = targetSymbols(psiFile, editor.caretModel.offset)
    if (allTargets.isEmpty()) {
      return false
    }

    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(editor)
    var file = if (psiFile is PsiCompiledFile) psiFile.decompiledPsiFile else psiFile
    file = InjectedLanguageManager.getInstance(project).getTopLevelFile(file)

    val isClear = isClearHighlights(editor)
    for (target in allTargets) {
      val textRanges = HighlightUsagesHelper.getUsageRanges(file, target)
      if (isClear) {
        // TODO: 2022/2/13 remove highlighters
      } else {
        val textAttr = TextAttributesFactory.getInstance().get()
        addHighlight(editor, textAttr, textRanges)
      }
    }

    // TODO: 2022/2/13 show highlighted ref count
    return true
  }

  companion object {

    fun isClearHighlights(e: Editor): Boolean {
      val editor = InjectedLanguageEditorUtil.getTopLevelEditor(e)

      val caretOffset = editor.caretModel.offset
      val highlighters = (HighlightManager.getInstance(editor.project) as HighlightManagerImpl).getHighlighters(editor)
      return highlighters.any {
        TextRange.create(it).grown(1).contains(caretOffset)
      }
    }

    fun addHighlight(editor: Editor, textAttr: TextAttributes, textRanges: Collection<TextRange>) {
      val markupModel = editor.markupModel

      textRanges.forEach { textRange ->
        markupModel.addRangeHighlighter(
          textRange.startOffset,
          textRange.endOffset,
          HighlighterLayer.SELECTION - 1,
          textAttr,
          HighlighterTargetArea.EXACT_RANGE
        )
      }
    }
  }
}
