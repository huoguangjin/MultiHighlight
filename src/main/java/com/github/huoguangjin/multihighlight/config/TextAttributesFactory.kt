package com.github.huoguangjin.multihighlight.config

object TextAttributesFactory {

  private lateinit var namedTextAttrs: List<NamedTextAttr>
  private var index = 0

  init {
    update()
  }

  internal fun update() {
    namedTextAttrs = MultiHighlightConfig.getInstance().namedTextAttrs
    index %= namedTextAttrs.size
  }

  fun getTextAttrs(): List<NamedTextAttr> {
    val textAttrs = namedTextAttrs
    if (textAttrs.isEmpty()) {
      return listOf(NamedTextAttr.IDE_DEFAULT)
    }

    return textAttrs
  }

  fun getNextTextAttrIndex(): Int = index

  fun advanceTextAttrIndex(): Int {
    index = (index + 1) % namedTextAttrs.size
    return index
  }

  fun getNextTextAttr(): NamedTextAttr {
    val textAttrs = namedTextAttrs
    if (textAttrs.isEmpty()) {
      return NamedTextAttr.IDE_DEFAULT
    }

    return textAttrs[index].also {
      advanceTextAttrIndex()
    }
  }
}
