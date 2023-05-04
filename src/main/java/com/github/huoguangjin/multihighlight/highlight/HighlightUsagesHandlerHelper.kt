package com.github.huoguangjin.multihighlight.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.Consumer
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

object HighlightUsagesHandlerHelper {

  private val selectTargetsMethod: KFunction<*>

  init {
    val methods = HighlightUsagesHandlerBase::class.declaredMemberFunctions
    selectTargetsMethod = methods.find { it.name == "selectTargets" }!!.apply { isAccessible = true }
  }

  /**
   * find usages by HighlightUsagesHandlerBase, but do not highlight
   *
   * see [HighlightUsagesHandlerBase.highlightUsages]
   */
  fun <T : PsiElement> findUsages(
    handler: HighlightUsagesHandlerBase<T>,
    usagesConsumer: (List<TextRange>, List<TextRange>) -> Unit
  ) {
    val targets = handler.targets
    selectTargetsMethod.call(handler, targets, Consumer { selectedTargets: MutableList<T> ->
      handler.computeUsages(selectedTargets)
      usagesConsumer(handler.readUsages, handler.writeUsages)
    })
  }
}
