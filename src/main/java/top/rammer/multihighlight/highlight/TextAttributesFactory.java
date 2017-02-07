package top.rammer.multihighlight.highlight;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Rammer on 06/02/2017.
 */
public class TextAttributesFactory {

    @NotNull
    public static TextAttributes get() {
        final EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        return scheme.getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
    }
}
