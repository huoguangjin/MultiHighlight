package com.github.huoguangjin.multihighlight.ui

import com.intellij.find.FindBundle
import com.intellij.find.FindModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.popup.list.ListPopupImpl
import javax.swing.JComponent
import javax.swing.JPanel

class MultiHighlightColorListPopup(
  project: Project,
  step: ListPopupStep<*>,
  private val findModel: FindModel,
) : ListPopupImpl(project, step) {

  override fun createContent(): JComponent {
    val toggleMatchCaseAction = object : CheckboxAction(
      FindBundle.message("find.case.sensitive"), null, AllIcons.Actions.MatchCase,
    ) {
      override fun isSelected(e: AnActionEvent): Boolean = findModel.isCaseSensitive
      override fun setSelected(e: AnActionEvent, selected: Boolean) {
        findModel.isCaseSensitive = selected
      }
    }

    val toggleWholeWordsOnlyAction = object : CheckboxAction(
      FindBundle.message("find.whole.words"), null, AllIcons.Actions.Words
    ) {
      override fun isSelected(e: AnActionEvent): Boolean = findModel.isWholeWordsOnly
      override fun setSelected(e: AnActionEvent, selected: Boolean) {
        findModel.isWholeWordsOnly = selected
      }
    }

    val list = super.createContent()
    val listWithToolbar: JPanel = ToolbarDecorator.createDecorator(list)
      .disableAddAction()
      .disableRemoveAction()
      .disableUpDownActions()
      .addExtraAction(toggleMatchCaseAction)
      .addExtraAction(toggleWholeWordsOnlyAction)
      .setVisibleRowCount(20)
      .createPanel()

    ActionUtil.getMnemonicAsShortcut(toggleMatchCaseAction)?.let {
      toggleMatchCaseAction.registerCustomShortcutSet(it, listWithToolbar)
    }

    ActionUtil.getMnemonicAsShortcut(toggleWholeWordsOnlyAction)?.let {
      toggleWholeWordsOnlyAction.registerCustomShortcutSet(it, listWithToolbar)
    }

    return listWithToolbar
  }
}
