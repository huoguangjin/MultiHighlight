package com.github.huoguangjin.multihighlight.action

import com.github.huoguangjin.multihighlight.config.MultiHighlightConfig
import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightHandler
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightManager
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightTextHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiDocumentManager
import java.awt.Color
import javax.swing.Icon

class MultiHighlightWithColorAction : DumbAwareAction() {

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
      PsiDocumentManager.getInstance(project).commitAllDocuments()

      val multiHighlightManager = MultiHighlightManager.getInstance()
      if (multiHighlightManager.tryRemoveHighlighterAtCaret(editor)) {
        return@executeCommand
      }

      val namedTextAttrs = MultiHighlightConfig.getInstance().namedTextAttrs
      val colorList = namedTextAttrs.mapIndexed(::NamedTextAttrItem)
      val listPopupStep = ColorListPopupStep(project, editor, "Highlight with color..", colorList)
      JBPopupFactory.getInstance()
        .createListPopup(listPopupStep)
        .showInBestPositionFor(editor)
    }, "MultiHighlight", null)
  }
}

private class ColorListPopupStep(
  private val project: Project,
  private val editor: Editor,
  title: String?,
  colorList: List<NamedTextAttrItem>,
) : BaseListPopupStep<NamedTextAttrItem>(title, colorList) {

  private var finalRunnable: Runnable? = null

  override fun isSpeedSearchEnabled() = true

  override fun getForegroundFor(item: NamedTextAttrItem): Color? = item.textAttr.foregroundColor

  override fun getBackgroundFor(item: NamedTextAttrItem): Color? = item.textAttr.backgroundColor

  override fun getSelectedIconFor(value: NamedTextAttrItem): Icon = AllIcons.Actions.Execute

  override fun onChosen(selectedValue: NamedTextAttrItem, finalChoice: Boolean): PopupStep<*>? {
    if (!finalChoice) {
      return super.onChosen(selectedValue, finalChoice)
    }

    finalRunnable = Runnable {
      try {
        val textAttr = selectedValue.textAttr

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        val selectionModel = editor.selectionModel

        if (psiFile != null && !selectionModel.hasSelection()) {
          MultiHighlightHandler(project, editor, psiFile, textAttr).highlight()
          return@Runnable
        }

        MultiHighlightTextHandler(project, editor, textAttr).highlight()
      } catch (ex: IndexNotReadyException) {
        DumbService.getInstance(project)
          .showDumbModeNotification("MultiHighlight requires indices and cannot be performed until they are built")
      }
    }

    return FINAL_CHOICE
  }

  override fun getFinalRunnable(): Runnable? = finalRunnable
}

private class NamedTextAttrItem(
  val index: Int,
  val textAttr: NamedTextAttr,
) {
  override fun toString() = "$index ${textAttr.name}"
}
