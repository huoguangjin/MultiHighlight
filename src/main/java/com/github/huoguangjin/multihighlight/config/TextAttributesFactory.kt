package com.github.huoguangjin.multihighlight.config

object TextAttributesFactory {

  private lateinit var namedTextAttrs: List<NamedTextAttr>
  private var index = 0

  init {
    val textAttrs = MultiHighlightConfig.getInstance().namedTextAttrs
    update(textAttrs)
  }

  internal fun update(textAttrs: List<NamedTextAttr>) {
    namedTextAttrs = textAttrs.ifEmpty {
      listOf(NamedTextAttr.IDE_DEFAULT)
    }

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
