package com.github.huoguangjin.multihighlight.config

object TextAttributesFactory {

  private val namedTextAttrs: MutableList<NamedTextAttr> by lazy {
    MultiHighlightConfig.getInstance().namedTextAttrs.ifEmpty {
      listOf(NamedTextAttr.IDE_DEFAULT)
    }.toMutableList()
  }

  private var index = 0

  internal fun update(textAttrs: List<NamedTextAttr>) {
    namedTextAttrs.clear()
    namedTextAttrs.addAll(textAttrs.ifEmpty {
      listOf(NamedTextAttr.IDE_DEFAULT)
    })

    index %= namedTextAttrs.size
  }

  fun getTextAttrs(): List<NamedTextAttr> = namedTextAttrs

  fun getNextTextAttrIndex(): Int = index

  fun advanceTextAttrIndex(): Int {
    index = (index + 1) % namedTextAttrs.size
    return index
  }

  fun getNextTextAttr(): NamedTextAttr {
    return namedTextAttrs[index].also {
      advanceTextAttrIndex()
    }
  }
}
