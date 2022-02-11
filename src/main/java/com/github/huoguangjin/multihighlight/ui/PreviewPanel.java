package com.github.huoguangjin.multihighlight.ui;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;
import java.util.List;

import javax.swing.JComponent;

import com.github.huoguangjin.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 07/02/2017.
 */
public interface PreviewPanel {

    @NotNull
    JComponent getPanel();

    void updateView(@NotNull List<NamedTextAttr> namedTextAttrList);

    void blinkSelectedColor(@NotNull NamedTextAttr namedTextAttr);

    void addListener(@NotNull PreviewSelectListener listener);

    void disposeUIResources();

    interface PreviewSelectListener extends EventListener {

        void onPreviewSelected();
    }
}
