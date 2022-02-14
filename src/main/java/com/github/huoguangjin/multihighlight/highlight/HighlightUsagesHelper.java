package com.github.huoguangjin.multihighlight.highlight;

import com.intellij.codeInsight.highlighting.HighlightUsagesKt;
import com.intellij.codeInsight.highlighting.UsageRanges;
import com.intellij.model.Symbol;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.Collection;

class HighlightUsagesHelper {

    @SuppressWarnings({ "UnstableApiUsage", "KotlinInternalInJava" })
    public static Collection<TextRange> getUsageRanges(PsiFile file, Symbol symbol) {
        ArrayList<TextRange> textRanges = new ArrayList<>();

        UsageRanges usageRanges = HighlightUsagesKt.getUsageRanges(file, symbol);
        if (usageRanges != null) {
            textRanges.addAll(usageRanges.getReadRanges());
            textRanges.addAll(usageRanges.getReadDeclarationRanges());
            textRanges.addAll(usageRanges.getWriteRanges());
            textRanges.addAll(usageRanges.getWriteDeclarationRanges());
        }

        return textRanges;
    }
}
