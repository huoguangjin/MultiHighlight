package com.github.huoguangjin.multihighlight.action

import com.github.huoguangjin.multihighlight.highlight.MultiHighlightManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil

class MultiHighlightClearAction : DumbAwareAction() {

  init {
    setInjectedContext(true)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val editor = InjectedLanguageEditorUtil.getTopLevelEditor(e.getRequiredData(CommonDataKeys.EDITOR))

    val multiHighlightManager = MultiHighlightManager.getInstance()
    val highlighters = multiHighlightManager.getHighlighters(editor)

    highlighters.forEach {
      multiHighlightManager.removeHighlighter(editor, it)
    }
  }
}
