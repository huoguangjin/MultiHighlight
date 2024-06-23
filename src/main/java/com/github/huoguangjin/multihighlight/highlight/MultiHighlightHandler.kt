package com.github.huoguangjin.multihighlight.highlight

import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.featureStatistics.FeatureUsageTracker
import com.intellij.find.FindBundle
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.model.psi.impl.targetSymbols
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.diagnostic.runAndLogException
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiCompiledFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import java.text.MessageFormat

class MultiHighlightHandler(
  private val project: Project,
  private val editor: Editor,
  private val psiFile: PsiFile,
  private val textAttr: TextAttributes
) {

  constructor(
    project: Project,
    editor: Editor,
    psiFile: PsiFile,
  ) : this(project, editor, psiFile, TextAttributesFactory.getNextTextAttr())

  fun highlight(noHighlightFallback: (() -> Unit)? = null) {
    if (highlightCustomUsages()) {
      return
    }

    DumbService.getInstance(project).withAlternativeResolveEnabled {
      if (highlightSymbols()) {
        return@withAlternativeResolveEnabled
      }

      noHighlightFallback?.invoke()
    }
  }

  fun highlightCustomUsages(): Boolean {
    val handler = HighlightUsagesHandler.createCustomHandler<PsiElement>(editor, psiFile) ?: return false

    handler.featureId?.let(FeatureUsageTracker.getInstance()::triggerFeatureUsed)

    thisLogger().runAndLogException {
      HighlightUsagesHandlerHelper.findUsages(handler) { readRanges, writeRanges ->
        val multiHighlightManager = MultiHighlightManager.getInstance()
        val hostEditor = InjectedLanguageEditorUtil.getTopLevelEditor(editor)
        highlightTextRanges(multiHighlightManager, hostEditor, readRanges.toSet(), writeRanges.toSet())
      }
    }

    return true
  }

  fun highlightSymbols(): Boolean {
    @Suppress("UnstableApiUsage")
    val allTargets = targetSymbols(psiFile, editor.caretModel.offset)
    if (allTargets.isEmpty()) {
      return false
    }

    val hostEditor = InjectedLanguageEditorUtil.getTopLevelEditor(editor)
    var file = if (psiFile is PsiCompiledFile) psiFile.decompiledPsiFile else psiFile
    file = InjectedLanguageManager.getInstance(project).getTopLevelFile(file)

    val multiHighlightManager = MultiHighlightManager.getInstance()
    for (target in allTargets) {
      val (readRanges, writeRanges) = ActionUtil.underModalProgress(
        project, FindBundle.message("progress.title.finding.usages")
      ) { HighlightUsagesHelper.getUsageRanges(file, target) } ?: continue
      highlightTextRanges(multiHighlightManager, hostEditor, readRanges, writeRanges)
    }

    return true
  }

  fun highlightTextRanges(
    multiHighlightManager: MultiHighlightManager,
    editor: Editor,
    readRanges: Collection<TextRange>,
    writeRanges: Collection<TextRange>,
  ) {
    val textRanges = readRanges + writeRanges
    multiHighlightManager.addHighlighters(editor, textAttr, textRanges)

    val highlightCount = textRanges.size
    WindowManager.getInstance().getStatusBar(project).info = if (highlightCount > 0) {
      MessageFormat.format(
        "{0} {0, choice, 1#usage|2#usages} highlighted (read: {1} write: {2})",
        highlightCount, readRanges.size, writeRanges.size
      )
    } else {
      "No usages highlighted"
    }
  }
}
