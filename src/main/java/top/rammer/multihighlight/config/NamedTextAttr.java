package top.rammer.multihighlight.config;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Rammer on 07/02/2017.
 */
public class NamedTextAttr {

    public static final TextAttributes IDE_DEFAULT_TEXT_ATTRIBUTE =
            EditorColorsManager.getInstance()
                    .getGlobalScheme()
                    .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);

    public static final NamedTextAttr IDE_DEFAULT =
            new NamedTextAttr("IDE default", IDE_DEFAULT_TEXT_ATTRIBUTE);

    private static final String ATTR_NAME = "name";

    private String name;
    private TextAttributes textAttributes;

    public NamedTextAttr(@NotNull String name, @NotNull TextAttributes textAttributes) {
        this.name = name;
        this.textAttributes = textAttributes;
    }

    public NamedTextAttr(@NotNull Element element) {
        final Attribute attribute = element.getAttribute(ATTR_NAME);
        if (attribute != null) {
            name = attribute.getValue();
        }
        if (name == null) {
            name = "";
        }

        textAttributes = new TextAttributes(element);
    }

    public void writeExternal(Element element) {
        element.setAttribute(ATTR_NAME, name);
        textAttributes.writeExternal(element);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public NamedTextAttr clone() {
        return new NamedTextAttr(name, textAttributes.clone());
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
        return name.equals(that.name) && textAttributes.equals(that.textAttributes);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (textAttributes != null ? textAttributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NamedTextAttr{" + name + " ta=" + textAttributes + '}';
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public TextAttributes getTextAttributes() {
        return textAttributes;
    }

    public void setTextAttributes(@NotNull TextAttributes textAttributes) {
        this.textAttributes = textAttributes;
    }
}
