package com.github.huoguangjin.multihighlight.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import org.jdom.Element

@State(
  name = "MultiHighlight",
  defaultStateAsResource = true,
  storages = [Storage("MultiHighlight.xml")]
)
class MultiHighlightConfig : PersistentStateComponent<Element> {

  var namedTextAttrs = listOf<NamedTextAttr>()
    private set

  fun updateTextAttrs(textAttrs: List<NamedTextAttr>) {
    namedTextAttrs = textAttrs

    TextAttributesFactory.update()
  }

  override fun getState() = Element("root").also { rootTag ->
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
    namedTextAttrs = readColors(state)
  }

  private fun readColors(element: Element): List<NamedTextAttr> {
    val colorListTag = element.getChild(COLOR_LIST_TAG) ?: return listOf()

    return colorListTag.getChildren(COLOR_TAG).map { colorTag ->
      NamedTextAttr(colorTag)
    }
  }

  override fun toString(): String = "MultiHighlightConfig{namedTextAttrs=$namedTextAttrs}"

  companion object {

    private const val COLOR_LIST_TAG = "list"
    private const val COLOR_TAG = "color"

    @JvmStatic
    fun getInstance(): MultiHighlightConfig = service()
  }
}
