package com.github.huoguangjin.multihighlight.action;

import com.github.huoguangjin.multihighlight.highlight.MultiHighlightHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Created by Rammer on 06/02/2017.
 */
public class MultiHighlightAction extends DumbAwareAction {
    
    public MultiHighlightAction() {
        setInjectedContext(true);
    }
    
    @Override
    public void update(AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(e.getProject() != null && e.getData(CommonDataKeys.EDITOR) != null
                && e.getData(CommonDataKeys.PSI_FILE) != null);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        
        CommandProcessor.getInstance().executeCommand(project, () -> {
            try {
                MultiHighlightHandler.invoke(project, editor, psiFile);
            } catch (IndexNotReadyException ex) {
                DumbService.getInstance(project)
                        .showDumbModeNotification("MultiHighlight requires indices "
                                + "and cannot be performed until they are built");
            }
        }, "MultiHighlight", null);
    }
}
