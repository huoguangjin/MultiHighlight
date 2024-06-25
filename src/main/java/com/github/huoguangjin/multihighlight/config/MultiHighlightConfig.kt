package com.github.huoguangjin.multihighlight.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.util.xmlb.Constants
import org.jdom.Element

@State(
  name = "MultiHighlight",
  defaultStateAsResource = true,
  storages = [Storage("MultiHighlight.xml")]
)
class MultiHighlightConfig : PersistentStateComponent<Element> {

  var matchCase = false
  var matchWord = false

  var namedTextAttrs = listOf<NamedTextAttr>()
    private set

  fun updateTextAttrs(textAttrs: List<NamedTextAttr>) {
    namedTextAttrs = textAttrs

    TextAttributesFactory.update(textAttrs)
  }

  override fun getState() = Element("root").also { rootTag ->
    JDOMExternalizerUtil.writeField(rootTag, OPTION_MATCH_CASE, matchCase.toString())
    JDOMExternalizerUtil.writeField(rootTag, OPTION_MATCH_WORD, matchWord.toString())
    writeColors(namedTextAttrs, rootTag)
  }

  private fun writeColors(textAttrs: List<NamedTextAttr>, element: Element) {
    if (textAttrs.isEmpty()) {
      return
    }

    element.addContent(Element(COLOR_LIST_TAG).also { colorListTag ->
      textAttrs.forEach { textAttr ->
        colorListTag.addContent(Element(COLOR_TAG).also { colorTag ->
          textAttr.writeExternal(colorTag)
        })
      }
    })
  }

  override fun loadState(state: Element) {
    matchCase = JDOMExternalizerUtil.readField(state, OPTION_MATCH_CASE) == true.toString()
    matchWord = JDOMExternalizerUtil.readField(state, OPTION_MATCH_WORD) == true.toString()
    namedTextAttrs = readColors(state)
  }

  private fun readColors(element: Element): List<NamedTextAttr> {
    val colorListTag = element.getChild(COLOR_LIST_TAG) ?: return listOf()

    return colorListTag.getChildren(COLOR_TAG).map { colorTag ->
      NamedTextAttr(colorTag)
    }
  }

  override fun toString(): String = "MultiHighlightConfig(c=$matchCase, w=$matchWord, attrs=$namedTextAttrs)"

  companion object {

    private const val OPTION_MATCH_CASE = "MATCH_CASE"
    private const val OPTION_MATCH_WORD = "MATCH_WORD"
    private const val COLOR_LIST_TAG = Constants.LIST
    private const val COLOR_TAG = "color"

    @JvmStatic
    fun getInstance(): MultiHighlightConfig = service()
  }
}
