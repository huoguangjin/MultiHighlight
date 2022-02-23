package com.github.huoguangjin.multihighlight.config

object TextAttributesFactory {

  private lateinit var namedTextAttrs: List<NamedTextAttr>
  private var index = 0

  init {
    update()
  }

  internal fun update() {
    namedTextAttrs = MultiHighlightConfig.getInstance().namedTextAttrs
    index = 0
  }

  fun getNextTextAttr(): NamedTextAttr {
    val textAttrs = namedTextAttrs
    if (textAttrs.isEmpty()) {
      return NamedTextAttr.IDE_DEFAULT
    }
    return textAttrs[index++ % textAttrs.size]
  }
}
