package top.rammer.multihighlight.highlight;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import top.rammer.multihighlight.config.MultiHighlightConfig;
import top.rammer.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 06/02/2017.
 */
public class TextAttributesFactory {

    private static final TextAttributesFactory INSTANCE = new TextAttributesFactory();

    private static int INDEX;

    public static TextAttributesFactory getInstance() {
        return INSTANCE;
    }

    private List<NamedTextAttr> namedTextAttrs;

    private TextAttributesFactory() {
        update();
    }

    public void update() {
        namedTextAttrs = MultiHighlightConfig.getInstance().getNamedTextAttrs();
    }

    @NotNull
    public NamedTextAttr get() {
        final List<NamedTextAttr> colors = this.namedTextAttrs;
        if (colors.isEmpty()) {
            return NamedTextAttr.IDE_DEFAULT; // NOTICE: remember me
        } else {
            return colors.get(INDEX++ % colors.size());
        }
    }
}
