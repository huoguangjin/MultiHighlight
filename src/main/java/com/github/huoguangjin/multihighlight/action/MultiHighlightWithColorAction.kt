package com.github.huoguangjin.multihighlight.action

import com.github.huoguangjin.multihighlight.config.MultiHighlightConfig
import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightHandler
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightManager
import com.github.huoguangjin.multihighlight.highlight.MultiHighlightTextHandler
import com.github.huoguangjin.multihighlight.ui.MultiHighlightColorListPopup
import com.github.huoguangjin.multihighlight.ui.OnColorSelectListener
import com.intellij.find.FindModel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.popup.WizardPopup
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

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

      val findModel = FindModel().apply {
        MultiHighlightConfig.getInstance().run {
          isCaseSensitive = matchCase
          isWholeWordsOnly = matchWord
        }
      }

      val listener = object : OnColorSelectListener {
        override fun onSelect(index: Int, textAttr: NamedTextAttr) {
          try {
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            val selectionModel = editor.selectionModel

            if (psiFile != null && !selectionModel.hasSelection()) {
              MultiHighlightHandler(project, editor, psiFile, textAttr).highlight {
                MultiHighlightTextHandler(project, editor, textAttr, findModel).highlight()
              }
            } else {
              MultiHighlightTextHandler(project, editor, textAttr, findModel).highlight()
            }

            if (index == TextAttributesFactory.getNextTextAttrIndex()) {
              TextAttributesFactory.advanceTextAttrIndex()
            }
          } catch (ex: IndexNotReadyException) {
            DumbService.getInstance(project)
              .showDumbModeNotification("MultiHighlight requires indices and cannot be performed until they are built")
          }
        }
      }

      MultiHighlightColorListPopup.create(project, findModel, listener)
        .also(::addKeyStrokeAction)
        .showInBestPositionFor(editor)
    }, "MultiHighlight", null)
  }

  private fun addKeyStrokeAction(listPopup: ListPopup) {
    if (listPopup !is WizardPopup) {
      return
    }

    val actionId = "MultiHighlightWithColor"
    val shortcuts = KeymapUtil.getActiveKeymapShortcuts(actionId).shortcuts // todo
    for (shortcut in shortcuts) {
      if (shortcut !is KeyboardShortcut || shortcut.secondKeyStroke != null) {
        continue
      }

      val keyStroke = shortcut.firstKeyStroke
      listPopup.registerAction(actionId, keyStroke, object : AbstractAction(actionId) {
        override fun actionPerformed(e: ActionEvent?) {
          listPopup.handleSelect(true)
        }
      })
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
