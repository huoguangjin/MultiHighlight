package top.rammer.multihighlight.ui;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
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

/**
 * Created by Rammer on 06/02/2017.
 */
public class MultiHighlightConfigPanel extends JPanel
        implements Configurable, Configurable.NoScroll {

    private static final TextAttributes DEFAULT_TEXT_ATTRIBUTES = EditorColorsManager.getInstance()
            .getGlobalScheme()
            .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);

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
        final String name = EditNameDialog.edit(null);
        if (name != null) {
            model.addRow(new NamedTextAttr(name, DEFAULT_TEXT_ATTRIBUTES.clone()));
        }
    }

    private void doEdit() {
        final int selectedRow = namedTextAttrList.getSelectedRow();
        final NamedTextAttr selected = namedTextAttrList.getSelectedObject();
        if (selected != null) {
            final String name = EditNameDialog.edit(selected);
            if (name != null && !name.equals(selected.getName())) {
                selected.setName(name);
                model.fireTableRowsUpdated(selectedRow, selectedRow);
            }
        }
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

        // TODO: 07/02/2017 notify data set changed
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
