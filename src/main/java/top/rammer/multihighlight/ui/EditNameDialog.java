package top.rammer.multihighlight.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import top.rammer.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 07/02/2017.
 */
public class EditNameDialog extends DialogWrapper {

    private JTextField myTextField;

    @Nullable
    public static String edit(@Nullable NamedTextAttr attr) {
        final String name = attr != null ? attr.getName() : "default name";
        final EditNameDialog dialog = new EditNameDialog(name);
        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog.myTextField.getText().trim();
    }

    private EditNameDialog(@NotNull String name) {
        super((Project) null, true);
        setTitle("Edit Color Name");
        myTextField = new JTextField();
        myTextField.setText(name);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel("Color Name:"),
                new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                        GridBagConstraints.NONE, new Insets(0, 5, 3, 5), 0, 0));

        panel.add(myTextField, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));

        myTextField.setPreferredSize(new Dimension(350, myTextField.getPreferredSize().height));

        return panel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myTextField;
    }
}
