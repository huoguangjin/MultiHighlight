package com.github.huoguangjin.multihighlight.highlight;

import com.intellij.codeInsight.highlighting.HighlightUsagesKt;
import com.intellij.model.Symbol;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;

import java.util.ArrayList;
import java.util.List;

class HighlightUsagesHelper {

    @SuppressWarnings({"UnstableApiUsage", "KotlinInternalInJava"})
    public static Pair<List<TextRange>, List<TextRange>> getUsageRanges(PsiFile file, Symbol symbol) {
        var usageRanges = HighlightUsagesKt.getUsageRanges(file, symbol);
        if (usageRanges == null) {
            return new Pair<>(new ArrayList<>(), new ArrayList<>());
        }

        var allReadRanges = CollectionsKt.plus(usageRanges.getReadRanges(), usageRanges.getReadDeclarationRanges());
        var allWriteRanges = CollectionsKt.plus(usageRanges.getWriteRanges(), usageRanges.getWriteDeclarationRanges());
        return new Pair<>(allReadRanges, allWriteRanges);
    }
}
