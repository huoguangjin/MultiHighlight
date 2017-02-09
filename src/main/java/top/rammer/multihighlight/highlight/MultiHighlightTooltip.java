package top.rammer.multihighlight.highlight;

import com.intellij.psi.PsiElement;

/**
 * Created by Rammer on 09/02/2017.
 */
public class MultiHighlightTooltip {

    private final String desc;

    public static MultiHighlightTooltip create(final PsiElement target, final String description) {
        return new MultiHighlightTooltip(description);
    }

    private MultiHighlightTooltip(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
