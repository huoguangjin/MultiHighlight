package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.MultiHighlightConfig
import com.intellij.openapi.options.ConfigurableBase

class MultiHighlightConfigurable : ConfigurableBase<MultiHighlightConfigurableUi, MultiHighlightConfig>(
  "MultiHighlight",
  "MultiHighlight",
  null,
) {

  override fun getSettings() = MultiHighlightConfig.getInstance()

  override fun createUi() = MultiHighlightConfigurableUi()
}
