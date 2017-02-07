package top.rammer.multihighlight.ui;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.MutableCollectionComboBoxModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

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

    private final JBList namedTextAttrList;
    private final MutableCollectionComboBoxModel<NamedTextAttr> model;

    public MultiHighlightConfigPanel() {
        super(new BorderLayout(0, 10));

        /*------------------------------ view ------------------------------*/

        final List<NamedTextAttr> namedTextAttrs = cloneFromStorage();
        model = new MutableCollectionComboBoxModel<>(namedTextAttrs);

        namedTextAttrList = new JBList(model);
        namedTextAttrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        namedTextAttrList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                final NamedTextAttr namedTextAttr = (NamedTextAttr) value;
                return super.getListCellRendererComponent(list, namedTextAttr.getName(), index,
                        isSelected, cellHasFocus);
            }
        });

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
            final Object selectedValue = namedTextAttrList.getSelectedValue();
            if (selectedValue != null) {
                chooserPanel.apply(((NamedTextAttr) selectedValue).getTextAttributes());
                updatePreviewPanel();
            }
        });

        namedTextAttrList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateChooserPanel();
            }
        });

        model.addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                System.out.println("MultiHighlightConfigPanel.intervalAdded: e = [" + e + "]");
                updatePreviewPanel();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                System.out.println("MultiHighlightConfigPanel.intervalRemoved: e = [" + e + "]");
                updatePreviewPanel();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                System.out.println("MultiHighlightConfigPanel.contentsChanged: e = [" + e + "]");
                updatePreviewPanel();
            }
        });

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
            model.add(new NamedTextAttr(name, DEFAULT_TEXT_ATTRIBUTES.clone()));
            namedTextAttrList.setSelectedIndex(model.getSize() - 1);
        }
    }

    private void doEdit() {
        final NamedTextAttr selected = model.getSelected();
        if (selected != null) {
            final String name = EditNameDialog.edit(selected);
            if (name != null && !name.equals(selected.getName())) {
                selected.setName(name);
                model.update();
            }
        }
    }

    private void updateChooserPanel() {
        final Object selectedValue = namedTextAttrList.getSelectedValue();
        if (selectedValue != null) {
            chooserPanel.reset(((NamedTextAttr) selectedValue).getTextAttributes());
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
        model.update(cloneFromStorage());
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
