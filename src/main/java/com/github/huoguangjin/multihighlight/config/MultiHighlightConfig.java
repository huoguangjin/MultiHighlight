package com.github.huoguangjin.multihighlight.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rammer on 06/02/2017.
 */
@State(name = "MultiHighlight", defaultStateAsResource = true, storages = {
        @Storage("MultiHighlight.xml")
})
public class MultiHighlightConfig implements PersistentStateComponent<Element> {

    private static final String COLOR_LIST_TAG = "list";
    private static final String COLOR_TAG = "color";

    private List<NamedTextAttr> namedTextAttrs;

    public static MultiHighlightConfig getInstance() {
        return ServiceManager.getService(MultiHighlightConfig.class);
    }

    public MultiHighlightConfig() {
        namedTextAttrs = new ArrayList<>();
    }

    public List<NamedTextAttr> getNamedTextAttrs() {
        return namedTextAttrs;
    }

    public void setNamedTextAttrs(List<NamedTextAttr> namedTextAttrs) {
        this.namedTextAttrs = namedTextAttrs;
    }

    @Nullable
    @Override
    public Element getState() {
        final Element rootTag = new Element("root");
        writeColors(rootTag);
        return rootTag;
    }

    private void writeColors(Element element) {
        if (namedTextAttrs == null || namedTextAttrs.isEmpty()) {
            return;
        }

        Element colorListTag = new Element(COLOR_LIST_TAG);
        for (NamedTextAttr namedTextAttr : namedTextAttrs) {
            Element colorTag = new Element(COLOR_TAG);
            namedTextAttr.writeExternal(colorTag);
            colorListTag.addContent(colorTag);
        }

        element.addContent(colorListTag);
    }

    @Override
    public void loadState(Element state) {
        readColors(state);
    }

    private void readColors(Element element) {
        if (namedTextAttrs == null) {
            namedTextAttrs = new ArrayList<>();
        }

        final Element colorListTag = element.getChild(COLOR_LIST_TAG);
        if (colorListTag != null) {
            for (Element colorTag : colorListTag.getChildren(COLOR_TAG)) {
                namedTextAttrs.add(new NamedTextAttr(colorTag));
            }
        }
    }

    @Override
    public String toString() {
        return "MultiHighlightConfig{namedTextAttrs=" + namedTextAttrs.toString() + '}';
    }
}
