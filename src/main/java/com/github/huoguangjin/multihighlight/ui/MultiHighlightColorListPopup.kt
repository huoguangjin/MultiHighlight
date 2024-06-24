package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.github.huoguangjin.multihighlight.config.TextAttributesFactory
import com.intellij.find.FindBundle
import com.intellij.find.FindModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.popup.list.ListPopupImpl
import java.awt.Color
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel

class MultiHighlightColorListPopup(
  project: Project,
  step: ListPopupStep<*>,
  private val findModel: FindModel,
) : ListPopupImpl(project, step) {
  companion object {
    fun create(
      project: Project,
      findModel: FindModel,
      listener: OnColorSelectListener,
    ): MultiHighlightColorListPopup {
      val namedTextAttrs = TextAttributesFactory.getTextAttrs()
      val colorList = namedTextAttrs.mapIndexed(::NamedTextAttrItem)
      val listPopupStep = ColorListPopupStep("Highlight with color", colorList, listener)
      listPopupStep.defaultOptionIndex = TextAttributesFactory.getNextTextAttrIndex()
      return MultiHighlightColorListPopup(project, listPopupStep, findModel)
    }
  }

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

interface OnColorSelectListener {
  fun onSelect(index: Int, textAttr: NamedTextAttr)
}

private class NamedTextAttrItem(
  val index: Int,
  val textAttr: NamedTextAttr,
) {
  override fun toString() = "$index ${textAttr.name}"
}

private class ColorListPopupStep(
  title: String?,
  colorList: List<NamedTextAttrItem>,
  private val listener: OnColorSelectListener,
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
      val index = selectedValue.index
      val textAttr = selectedValue.textAttr
      listener.onSelect(index, textAttr)
    }

    return FINAL_CHOICE
  }

  override fun getFinalRunnable(): Runnable? = finalRunnable
}
