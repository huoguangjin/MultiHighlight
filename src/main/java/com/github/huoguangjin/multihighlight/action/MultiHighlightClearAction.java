package com.github.huoguangjin.multihighlight.action;

import com.github.huoguangjin.multihighlight.config.NamedTextAttr;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightManagerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

/**
 * Created by hgj on 13/10/2017.
 */
public class MultiHighlightClearAction extends DumbAwareAction {
    
    public MultiHighlightClearAction() {
        setInjectedContext(true);
    }
    
    @Override
    public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(e.getProject() != null && e.getData(CommonDataKeys.EDITOR) != null);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        
        final HighlightManager highlightManager = HighlightManager.getInstance(project);
        final RangeHighlighter[] highlighters =
                ((HighlightManagerImpl) highlightManager).getHighlighters(editor);
        for (RangeHighlighter highlighter : highlighters) {
            final TextAttributes ta = highlighter.getTextAttributes();
            if (ta != null && ta instanceof NamedTextAttr
                    && highlighter.getLayer() == HighlighterLayer.SELECTION - 1) {
                highlightManager.removeSegmentHighlighter(editor, highlighter);
            }
        }
    }
}
