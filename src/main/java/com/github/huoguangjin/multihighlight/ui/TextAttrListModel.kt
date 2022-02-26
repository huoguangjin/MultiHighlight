package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel

class TextAttrListModel : ListTableModel<NamedTextAttr>(NamedColumnInfo()) {

  private class NamedColumnInfo : ColumnInfo<NamedTextAttr, String>("Name") {
    override fun valueOf(textAttr: NamedTextAttr): String {
      return textAttr.name
    }

    override fun setValue(textAttr: NamedTextAttr, value: String?) {
      textAttr.name = value.orEmpty()
    }
  }
}
