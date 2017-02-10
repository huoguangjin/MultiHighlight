package top.rammer.multihighlight.ui;

import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import top.rammer.multihighlight.config.MultiHighlightConfig;
import top.rammer.multihighlight.config.NamedTextAttr;
import top.rammer.multihighlight.highlight.TextAttributesFactory;

/**
 * Created by Rammer on 06/02/2017.
 */
public class MultiHighlightConfigPanel extends JPanel
        implements Configurable, Configurable.NoScroll {

    private final ChooserPanel chooserPanel;

    private final PreviewPanel previewPanel;

    private final TableView<NamedTextAttr> namedTextAttrList;
    private final ListTableModel<NamedTextAttr> model;

    public MultiHighlightConfigPanel() {
        super(new BorderLayout(0, 10));

        /*------------------------------ view ------------------------------*/

        model = new TextAttrListModel(cloneFromStorage());
        namedTextAttrList = new TableView<>(model);
        namedTextAttrList.setShowColumns(false);
        namedTextAttrList.setStriped(true);
        namedTextAttrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        chooserPanel = new ColorChooserPanel();
        previewPanel = new ColorPreviewPanel();

        /*------------------------------ listener ------------------------------*/

        new DoubleClickListener() {

            @Override
            protected boolean onDoubleClick(MouseEvent event) {
                doEdit();
                return true;
            }
        }.installOn(namedTextAttrList);

        chooserPanel.addListener(e -> {
            final NamedTextAttr selected = namedTextAttrList.getSelectedObject();
            if (selected != null) {
                chooserPanel.apply(selected.getTextAttributes());
                updatePreviewPanel();
            }
        });

        namedTextAttrList.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateChooserPanel();
            }
        });

        model.addTableModelListener(e -> updatePreviewPanel());

        /*------------------------------ layout ------------------------------*/

        final JPanel leftPanel = ToolbarDecorator.createDecorator(namedTextAttrList)
                .setAddAction(button -> doAdd())
                .setEditAction(button -> doEdit())
                .addExtraActions((AnActionButton) new ToolbarDecorator.ElementActionButton(
                        ActionsBundle.message("action.EditorCopy.text"), AllIcons.Actions.Copy) {

                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        doCopy();
                    }
                })
                .createPanel();

        final JBSplitter rightPanel = new JBSplitter(true, 0.3f);
        rightPanel.setFirstComponent(chooserPanel.getPanel());
        rightPanel.setSecondComponent(previewPanel.getPanel());

        final JBSplitter jbSplitter = new JBSplitter(false, 0.3f);
        jbSplitter.setFirstComponent(leftPanel);
        jbSplitter.setSecondComponent(rightPanel);
        add(jbSplitter, BorderLayout.CENTER);
        setBorder(IdeBorderFactory.createTitledBorder("MultiHighlight Colors", false));

        /*------------------------------ init ------------------------------*/

        updateChooserPanel();
        updatePreviewPanel();
    }

    private void doAdd() {
        final String name = askForColorName(null);
        if (name != null) {
            model.addRow(new NamedTextAttr(name, NamedTextAttr.IDE_DEFAULT_TEXT_ATTRIBUTE.clone()));
            final int newRow = model.getRowCount() - 1;
            namedTextAttrList.getSelectionModel().setSelectionInterval(newRow, newRow);
        }
    }

    private void doEdit() {
        final int selectedRow = namedTextAttrList.getSelectedRow();
        final NamedTextAttr selected = namedTextAttrList.getSelectedObject();
        if (selected != null) {
            final String name = askForColorName(selected);
            if (name != null && !name.equals(selected.getName())) {
                selected.setName(name);
                model.fireTableRowsUpdated(selectedRow, selectedRow);
            }
        }
    }

    private void doCopy() {
        final NamedTextAttr selected = namedTextAttrList.getSelectedObject();
        if (selected != null) {
            final String name = askForColorName(null);
            if (name != null) {
                model.addRow(new NamedTextAttr(name, selected.getTextAttributes().clone()));
                final int newRow = model.getRowCount() - 1;
                namedTextAttrList.getSelectionModel().setSelectionInterval(newRow, newRow);
            }
        }
    }

    @Nullable
    private String askForColorName(@Nullable NamedTextAttr attr) {
        final String name = attr != null ? attr.getName() : "default name";
        return Messages.showInputDialog("Color Name:", "Edit Color Name", null, name, null);
    }

    private void updateChooserPanel() {
        final NamedTextAttr selected = namedTextAttrList.getSelectedObject();
        if (selected != null) {
            chooserPanel.reset(selected.getTextAttributes());
        } else {
            chooserPanel.resetDefault();
        }
    }

    private void updatePreviewPanel() {
        previewPanel.updateView(model.getItems());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "MultiHighlight";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // FIXME: 10/02/2017 move create swing component code from constructor to here
        return this;
    }

    @Override
    public boolean isModified() {
        final List<NamedTextAttr> current = model.getItems();
        final List<NamedTextAttr> origin = MultiHighlightConfig.getInstance().getNamedTextAttrs();
        final int originSize = origin.size();

        if (current.size() != originSize) {
            return true;
        }

        for (int i = 0; i < originSize; i++) {
            if (!origin.get(i).equals(current.get(i))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        MultiHighlightConfig.getInstance().setNamedTextAttrs(model.getItems());
        TextAttributesFactory.getInstance().update();
    }

    @Override
    public void reset() {
        model.setItems(cloneFromStorage());
    }

    @Override
    public void disposeUIResources() {
        previewPanel.disposeUIResources();
    }

    private List<NamedTextAttr> cloneFromStorage() {
        final ArrayList<NamedTextAttr> clone = new ArrayList<>();
        for (NamedTextAttr attr : MultiHighlightConfig.getInstance().getNamedTextAttrs()) {
            clone.add(attr);
        }

        return clone;
    }
}
