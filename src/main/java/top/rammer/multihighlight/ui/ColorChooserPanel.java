package top.rammer.multihighlight.ui;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.BitUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Created by Rammer on 07/02/2017.
 */
public class ColorChooserPanel extends JPanel implements ChooserPanel {

    private JPanel myPanel;

    private JBCheckBox myCbForeground;
    private JBCheckBox myCbBackground;
    private JBCheckBox myCbErrorStripe;
    private JBCheckBox myCbEffects;

    private ColorPanel myForegroundChooser;
    private ColorPanel myBackgroundChooser;
    private ColorPanel myErrorStripeColorChooser;
    private ColorPanel myEffectsColorChooser;

    private final Map<String, EffectType> myEffectsMap;

    {
        Map<String, EffectType> map = new LinkedHashMap<>();
        map.put(ApplicationBundle.message("combobox.effect.bordered"), EffectType.BOXED);
        map.put(ApplicationBundle.message("combobox.effect.underscored"),
                EffectType.LINE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.boldunderscored"),
                EffectType.BOLD_LINE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.underwaved"),
                EffectType.WAVE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.strikeout"), EffectType.STRIKEOUT);
        map.put(ApplicationBundle.message("combobox.effect.bold.dottedline"),
                EffectType.BOLD_DOTTED_LINE);
        myEffectsMap = Collections.unmodifiableMap(map);
    }

    private JBCheckBox myCbBold;
    private JBCheckBox myCbItalic;

    private ComboBox myEffectsCombo;

    private final EventDispatcher<ColorChangedListener> myDispatcher =
            EventDispatcher.create(ColorChangedListener.class);

    private final CollectionComboBoxModel<String> myEffectsModel;

    public ColorChooserPanel() {
        super(new BorderLayout());
        add(myPanel, BorderLayout.CENTER);

        setBorder(JBUI.Borders.empty(4, 0, 4, 4));
        myEffectsModel = new CollectionComboBoxModel<>(new ArrayList<>(myEffectsMap.keySet()));
        //noinspection unchecked
        myEffectsCombo.setModel(myEffectsModel);
        //noinspection unchecked
        myEffectsCombo.setRenderer(SimpleListCellRenderer.create((label, value, index) -> {
            label.setText(value != null ? (String) value : "<invalid>");
        }));

        ActionListener actionListener = e -> {
            myForegroundChooser.setEnabled(myCbForeground.isSelected());
            myBackgroundChooser.setEnabled(myCbBackground.isSelected());
            myEffectsColorChooser.setEnabled(myCbEffects.isSelected());
            myEffectsCombo.setEnabled(myCbEffects.isSelected());
            myErrorStripeColorChooser.setEnabled(myCbErrorStripe.isSelected());

            myDispatcher.getMulticaster().onColorChanged(e);
        };

        myCbBold.addActionListener(actionListener);
        myCbItalic.addActionListener(actionListener);

        myCbForeground.addActionListener(actionListener);
        myCbBackground.addActionListener(actionListener);
        myCbErrorStripe.addActionListener(actionListener);
        myCbEffects.addActionListener(actionListener);

        myForegroundChooser.addActionListener(actionListener);
        myBackgroundChooser.addActionListener(actionListener);
        myErrorStripeColorChooser.addActionListener(actionListener);
        myEffectsColorChooser.addActionListener(actionListener);

        myEffectsCombo.addActionListener(actionListener);
    }

    @NotNull
    @Override
    public JComponent getPanel() {
        return this;
    }

    @Override
    public void resetDefault() {
        myCbBold.setSelected(false);
        myCbItalic.setSelected(false);

        resetColorChooser(myCbForeground, myForegroundChooser, null);
        resetColorChooser(myCbBackground, myBackgroundChooser, null);
        resetColorChooser(myCbErrorStripe, myErrorStripeColorChooser, null);
        resetColorChooser(myCbEffects, myEffectsColorChooser, null);

        myCbBold.setEnabled(false);
        myCbItalic.setEnabled(false);

        myCbForeground.setEnabled(false);
        myCbBackground.setEnabled(false);
        myCbErrorStripe.setEnabled(false);
        myCbEffects.setEnabled(false);

        myForegroundChooser.setEnabled(false);
        myBackgroundChooser.setEnabled(false);
        myErrorStripeColorChooser.setEnabled(false);
        myEffectsColorChooser.setEnabled(false);

        myEffectsCombo.setEnabled(false);
    }

    @Override
    public void reset(@NotNull TextAttributes ta) {
        myCbBold.setEnabled(true);
        myCbItalic.setEnabled(true);

        int fontType = ta.getFontType();
        myCbBold.setSelected(BitUtil.isSet(fontType, Font.BOLD));
        myCbItalic.setSelected(BitUtil.isSet(fontType, Font.ITALIC));

        resetColorChooser(myCbForeground, myForegroundChooser, ta.getForegroundColor());
        resetColorChooser(myCbBackground, myBackgroundChooser, ta.getBackgroundColor());
        resetColorChooser(myCbErrorStripe, myErrorStripeColorChooser, ta.getErrorStripeColor());

        Color effectColor = ta.getEffectColor();
        resetColorChooser(myCbEffects, myEffectsColorChooser, effectColor);

        if (effectColor == null) {
            myEffectsCombo.setEnabled(false);
        } else {
            myEffectsCombo.setEnabled(true);
            myEffectsModel.setSelectedItem(
                    ContainerUtil.reverseMap(myEffectsMap).get(ta.getEffectType()));
        }
    }

    private void resetColorChooser(JCheckBox checkBox, ColorPanel colorPanel,
            @Nullable Color color) {
        checkBox.setEnabled(true);
        if (color == null) {
            checkBox.setSelected(false);
            colorPanel.setEnabled(false);
            colorPanel.setSelectedColor(null);
        } else {
            checkBox.setSelected(true);
            colorPanel.setEnabled(true);
            colorPanel.setSelectedColor(color);
        }
    }

    @Override
    public void apply(@NotNull TextAttributes ta) {
        int fontType = Font.PLAIN;
        if (myCbBold.isSelected()) {
            fontType |= Font.BOLD;
        }
        if (myCbItalic.isSelected()) {
            fontType |= Font.ITALIC;
        }

        ta.setFontType(fontType);

        if (myCbForeground.isSelected()) {
            ta.setForegroundColor(myForegroundChooser.getSelectedColor());
        } else {
            ta.setForegroundColor(null);
        }

        if (myCbBackground.isSelected()) {
            ta.setBackgroundColor(myBackgroundChooser.getSelectedColor());
        } else {
            ta.setBackgroundColor(null);
        }

        if (myCbErrorStripe.isSelected()) {
            ta.setErrorStripeColor(myErrorStripeColorChooser.getSelectedColor());
        } else {
            ta.setErrorStripeColor(null);
        }

        if (myCbEffects.isSelected()) {
            Color effectColor = myEffectsColorChooser.getSelectedColor();
            ta.setEffectColor(effectColor);
            //noinspection SuspiciousMethodCalls
            if (effectColor == null) {
                ta.setEffectType(null);
            } else {
                //noinspection SuspiciousMethodCalls
                ta.setEffectType(myEffectsMap.get(myEffectsCombo.getModel().getSelectedItem()));
            }
        } else {
            ta.setEffectColor(null);
            ta.setEffectType(null);
        }
    }

    @Override
    public void addListener(@NotNull ColorChangedListener listener) {
        myDispatcher.addListener(listener);
    }
}
