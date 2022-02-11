package com.github.huoguangjin.multihighlight.config;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Rammer on 07/02/2017.
 */
public class NamedTextAttr extends TextAttributes {

    public static final NamedTextAttr IDE_DEFAULT = new NamedTextAttr("IDE default",
            EditorColorsManager.getInstance()
                    .getGlobalScheme()
                    .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES));

    private static final String ATTR_NAME = "name";

    private String name;

    public NamedTextAttr(@NotNull String name, @NotNull TextAttributes ta) {
        super(ta.getForegroundColor(), ta.getBackgroundColor(), ta.getEffectColor(),
                ta.getEffectType(), ta.getFontType());
        setErrorStripeColor(ta.getErrorStripeColor());
        this.name = name;
    }

    public NamedTextAttr(@NotNull Element element) {
        super(element);
        final Attribute attribute = element.getAttribute(ATTR_NAME);
        if (attribute != null) {
            name = attribute.getValue();
        }
        if (name == null) {
            name = "";
        }
    }

    public void writeExternal(Element element) {
        super.writeExternal(element);
        element.setAttribute(ATTR_NAME, name);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public NamedTextAttr clone() {
        return new NamedTextAttr(name, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NamedTextAttr that = (NamedTextAttr) o;

        // name and textAttributes both are not null
        return name.equals(that.name) && super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NamedTextAttr{" + name + "=" + super.toString() + '}';
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }
}
