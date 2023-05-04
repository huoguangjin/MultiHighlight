package com.github.huoguangjin.multihighlight.highlight;

import com.intellij.codeInsight.highlighting.HighlightUsagesKt;
import com.intellij.model.Symbol;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

import kotlin.Pair;

class HighlightUsagesHelper {

  @SuppressWarnings({ "UnstableApiUsage", "KotlinInternalInJava" })
  public static Pair<ArrayList<TextRange>, ArrayList<TextRange>> getUsageRanges(PsiFile file, Symbol symbol) {
    ArrayList<TextRange> readRanges = new ArrayList<>();
    ArrayList<TextRange> writeRanges = new ArrayList<>();

    var usageRanges = HighlightUsagesKt.getUsageRanges(file, symbol);
    if (usageRanges != null) {
      readRanges.addAll(usageRanges.getReadRanges());
      readRanges.addAll(usageRanges.getReadDeclarationRanges());
      writeRanges.addAll(usageRanges.getWriteRanges());
      writeRanges.addAll(usageRanges.getWriteDeclarationRanges());
    }

    return new Pair<>(readRanges, writeRanges);
  }
}
