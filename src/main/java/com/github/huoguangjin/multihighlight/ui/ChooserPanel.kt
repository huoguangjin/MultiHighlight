package com.github.huoguangjin.multihighlight.ui

import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.JComponent

/**
 * [com.intellij.application.options.colors.OptionsPanelImpl.ColorDescriptionPanel]
 */
interface ChooserPanel {

  val panel: JComponent

  fun resetDefault()

  fun reset(textAttr: TextAttributes)

  fun apply(textAttr: TextAttributes)

  fun addListener(listener: ColorChangedListener)

  fun interface ColorChangedListener : EventListener {
    fun onColorChanged(e: ActionEvent)
  }
}
