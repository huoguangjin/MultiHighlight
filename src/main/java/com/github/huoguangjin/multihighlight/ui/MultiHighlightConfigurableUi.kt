package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.MultiHighlightConfig
import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class MultiHighlightConfigurableUi : ConfigurableUi<MultiHighlightConfig>, Disposable {

  private val model = TextAttrListModel()
  private val namedTextAttrTable = TableView(model).apply {
    isStriped = true
    setShowColumns(false)
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  }

  private val chooserPanel: ChooserPanel = ColorChooserPanel().apply {
    resetDefault()
  }

  private val previewPanel: PreviewPanel = ColorPreviewPanel()

  private val rootPanel: DialogPanel = panel {
    val tableWithToolbar: JPanel = ToolbarDecorator.createDecorator(namedTextAttrTable)
      .setAddAction { doAdd() }
      .setEditAction { doEdit() }
      .addExtraAction(DumbAwareAction.create(
        ActionsBundle.message("action.EditorCopy.text"),
        AllIcons.Actions.Copy
      ) {
        doCopy()
      })
      .createPanel()

    val chooserAndPreviewPanel: JPanel = JBSplitter(true, 0.3f).apply {
      firstComponent = chooserPanel.panel
      secondComponent = previewPanel.panel
    }

    val mainPanel: JPanel = JBSplitter(false, 0.3f).apply {
      firstComponent = tableWithToolbar
      secondComponent = chooserAndPreviewPanel
    }

    row {
      cell(mainPanel).align(Align.FILL)
    }.resizableRow()
  }

  init {
    model.addTableModelListener {
      updatePreviewPanel()
    }

    object : DoubleClickListener() {
      override fun onDoubleClick(event: MouseEvent) = true.also { doEdit() }
    }.installOn(namedTextAttrTable)

    namedTextAttrTable.selectionModel.addListSelectionListener {
      if (!it.valueIsAdjusting) {
        updateChooserPanel()
      }
    }

    chooserPanel.addListener {
      namedTextAttrTable.selectedObject?.let {
        chooserPanel.apply(it)
        updatePreviewPanel()
      }
    }
  }

  override fun getComponent(): JComponent = rootPanel

  private fun updateChooserPanel() {
    val selected = namedTextAttrTable.selectedObject
    if (selected != null) {
      chooserPanel.reset(selected)
    } else {
      chooserPanel.resetDefault()
    }
  }

  private fun updatePreviewPanel() {
    previewPanel.updateView(model.items)
  }

  private fun doAdd() {
    val name = askForColorName("unnamed") ?: return

    val added = NamedTextAttr.IDE_DEFAULT.clone().also { it.name = name }
    model.addRow(added)
    val newRow = model.rowCount - 1
    namedTextAttrTable.setRowSelectionInterval(newRow, newRow)
  }

  private fun doEdit() {
    val selected = namedTextAttrTable.selectedObject ?: return
    val selectedRow = namedTextAttrTable.selectedRow
    val name = askForColorName(selected.name) ?: return

    if (selected.name != name) {
      selected.name = name
      model.fireTableRowsUpdated(selectedRow, selectedRow)
    }
  }

  private fun doCopy() {
    val selected = namedTextAttrTable.selectedObject ?: return
    val name = askForColorName("${selected.name} copy") ?: return

    val newRow = namedTextAttrTable.selectedRow + 1
    model.insertRow(newRow, NamedTextAttr(name, selected))
    namedTextAttrTable.setRowSelectionInterval(newRow, newRow)
  }

  private fun askForColorName(hintName: String): String? {
    return Messages.showInputDialog("Color name:", "Edit Color Name", null, hintName, null)
  }

  override fun isModified(settings: MultiHighlightConfig): Boolean {
    val current = model.items
    val origin = settings.namedTextAttrs
    return current != origin
  }

  override fun apply(settings: MultiHighlightConfig) {
    val lastSelectedRow = namedTextAttrTable.selectedRow

    val textAttrs = model.items.map(NamedTextAttr::clone)
    settings.updateTextAttrs(textAttrs)

    if (lastSelectedRow in 0..namedTextAttrTable.rowCount) {
      namedTextAttrTable.setRowSelectionInterval(lastSelectedRow, lastSelectedRow)
    }
  }

  override fun reset(settings: MultiHighlightConfig) {
    model.items = settings.namedTextAttrs.map(NamedTextAttr::clone)
  }

  override fun dispose() {
    previewPanel.disposeUIResources()
  }
}
