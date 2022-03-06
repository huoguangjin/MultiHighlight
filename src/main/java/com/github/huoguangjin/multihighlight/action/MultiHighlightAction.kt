package com.github.huoguangjin.multihighlight.action

import com.github.huoguangjin.multihighlight.highlight.MultiHighlightHandler
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightTextHandler
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiDocumentManager

class MultiHighlightAction : DumbAwareAction() {

  init {
    setInjectedContext(true)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.EDITOR) != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val editor = e.getRequiredData(CommonDataKeys.EDITOR)

    CommandProcessor.getInstance().executeCommand(project, {
      try {
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val selectionModel = editor.selectionModel

        if (psiFile != null && !selectionModel.hasSelection()) {
          MultiHighlightHandler(project, editor, psiFile).highlight()
          return@executeCommand
        }

        if (!selectionModel.hasSelection()) {
          selectionModel.selectWordAtCaret(false)
        }

        val selectedText = selectionModel.selectedText ?: return@executeCommand
        MultiHighlightTextHandler(project, editor, psiFile).highlight(selectedText)
      } catch (ex: IndexNotReadyException) {
        DumbService.getInstance(project)
          .showDumbModeNotification("MultiHighlight requires indices and cannot be performed until they are built")
      }
    }, "MultiHighlight", null)
  }
}
