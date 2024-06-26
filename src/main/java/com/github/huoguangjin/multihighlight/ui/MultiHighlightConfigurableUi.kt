package com.github.huoguangjin.multihighlight.ui

import com.github.huoguangjin.multihighlight.config.MultiHighlightConfig
import com.github.huoguangjin.multihighlight.config.NamedTextAttr
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import java.awt.event.MouseEvent
import javax.swing.*

class MultiHighlightConfigurableUi : ConfigurableUi<MultiHighlightConfig>, Disposable {

  private val model = TextAttrListModel()
  private val namedTextAttrTable = TableView(model).apply {
    isStriped = true
    setShowColumns(false)
    setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
  }

  private val chooserPanel: ChooserPanel = ColorChooserPanel().apply {
    resetDefault()
  }

  private val previewPanel: PreviewPanel = ColorPreviewPanel()

  private val rootPanel: DialogPanel = panel {
    val copyAction = DumbAwareAction.create(
      ActionsBundle.message("action.EditorCopy.text"),
      AllIcons.Actions.Copy
    ) {
      doCopy()
    }

    val tableWithToolbar: JPanel = ToolbarDecorator.createDecorator(namedTextAttrTable)
      .setAddAction { doAdd() }
      .setEditAction { doEdit() }
      .addExtraAction(copyAction)
      .createPanel()

    val ideCopyAction = ActionManager.getInstance().getAction(IdeActions.ACTION_COPY)
    copyAction.registerCustomShortcutSet(ideCopyAction.shortcutSet, tableWithToolbar)

    val chooserAndPreviewPanel: JPanel = JBSplitter(true, 0.3f).apply {
      firstComponent = chooserPanel.panel
      secondComponent = previewPanel.panel
    }

    val mainPanel: JPanel = JBSplitter(false, 0.3f).apply {
      firstComponent = tableWithToolbar
      secondComponent = chooserAndPreviewPanel
    }

    val config = MultiHighlightConfig.getInstance()

    row {
      cell(mainPanel).align(Align.FILL)
        .onIsModified {
          model.items != config.namedTextAttrs
        }
        .onApply {
          config.updateTextAttrs(model.items.map(NamedTextAttr::clone))
        }
        .onReset {
          model.items = config.namedTextAttrs.map(NamedTextAttr::clone)
        }
    }.resizableRow()

    group(indent = false) {
      row {
        contextHelp("MultiHighlight will try to highlight the plain text if no identifiers are found")
          .label("Highlight plain text:")
        checkBox("Match case").bindSelected(config::matchCase)
        checkBox("Match words").bindSelected(config::matchWord)
      }
    }
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

    val selectRows = namedTextAttrTable.selectedRows
    if (selectRows.size > 1) {
      selectRows.forEach {
        val row = namedTextAttrTable.getRow(it)
        model.insertRow(it + selectRows.size, NamedTextAttr("${row.name} copy", row))
      }
      val rowIndex = selectRows.last() + 1
      namedTextAttrTable.setRowSelectionInterval(rowIndex, rowIndex + selectRows.size - 1)
      return
    }

    val name = askForColorName("${selected.name} copy") ?: return

    val newRow = namedTextAttrTable.selectedRow + 1
    model.insertRow(newRow, NamedTextAttr(name, selected))
    namedTextAttrTable.setRowSelectionInterval(newRow, newRow)
  }

  private fun askForColorName(hintName: String): String? {
    return Messages.showInputDialog("Color name:", "Edit Color Name", null, hintName, null)
  }

  override fun isModified(settings: MultiHighlightConfig): Boolean {
    return rootPanel.isModified()
  }

  override fun apply(settings: MultiHighlightConfig) {
    rootPanel.apply()
  }

  override fun reset(settings: MultiHighlightConfig) {
    rootPanel.reset()
  }

  override fun dispose() {
    previewPanel.disposeUIResources()
  }
}
