package com.github.huoguangjin.multihighlight.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.Consumer
import com.intellij.util.containers.toMutableSmartList
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

object HighlightUsagesHandlerHelper {

  private val getTargetsMethod: KFunction<*>
  private val selectTargetsMethod: KFunction<*>
  private val computeUsagesMethod: KFunction<*>

  init {
    val methods = HighlightUsagesHandlerBase::class.declaredMemberFunctions
    getTargetsMethod = methods.find { it.name == "getTargets" }!!
    selectTargetsMethod = methods.find { it.name == "selectTargets" }!!.apply { isAccessible = true }
    computeUsagesMethod = methods.find { it.name == "computeUsages" }!!
  }

  /**
   * find usages by HighlightUsagesHandlerBase, but do not highlight
   *
   * see [HighlightUsagesHandlerBase.highlightUsages]
   */
  fun <T : PsiElement> findUsages(handler: HighlightUsagesHandlerBase<T>): MutableList<TextRange> {
    val targets = getTargetsMethod.call(handler)
    selectTargetsMethod.call(handler, targets, Consumer { selectedTargets: List<PsiElement> ->
      computeUsagesMethod.call(handler, selectedTargets)
    })

    return (handler.readUsages + handler.writeUsages).toMutableSmartList()
  }
}
