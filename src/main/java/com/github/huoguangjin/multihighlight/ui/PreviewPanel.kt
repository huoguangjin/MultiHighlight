package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import javax.swing.JComponent

interface PreviewPanel {

  val panel: JComponent

  fun updateView(textAttrs: List<NamedTextAttr>)

  fun blinkSelectedColor(textAttr: NamedTextAttr)

  fun disposeUIResources()
}
