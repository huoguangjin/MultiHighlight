package com.github.huoguangjin.multihighlight.config

import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import org.jdom.Element

class NamedTextAttr : TextAttributes {

  var name: String

  constructor(name: String, ta: TextAttributes) : super() {
    this.name = name
    copyFrom(ta)
  }

  constructor(element: Element) : super(element) {
    val attribute = element.getAttribute(ATTR_NAME)
    name = attribute?.value ?: ""
  }

  override fun writeExternal(element: Element) {
    super.writeExternal(element)
    element.setAttribute(ATTR_NAME, name)
  }

  override fun clone(): NamedTextAttr = NamedTextAttr(name, this)

  override fun equals(other: Any?): Boolean {
    val o = other as? NamedTextAttr ?: return false
    return name == o.name && super.equals(o)
  }

  override fun hashCode(): Int = 31 * name.hashCode() + super.hashCode()

  override fun toString(): String = "NamedTextAttr{" + name + "=" + super.toString() + '}'

  companion object {

    private const val ATTR_NAME = "name"

    @JvmField
    val IDE_DEFAULT = NamedTextAttr(
      "IDE default",
      EditorColorsManager.getInstance().globalScheme.getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES)
    )
  }
}
