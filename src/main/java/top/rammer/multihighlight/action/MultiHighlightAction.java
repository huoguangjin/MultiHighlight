package top.rammer.multihighlight.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import top.rammer.multihighlight.Log;
import top.rammer.multihighlight.highlight.MultiHighlightHandler;

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
        presentation.setEnabled(e.getProject() != null
                && CommonDataKeys.EDITOR.getData(e.getDataContext()) != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        final PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (project == null || editor == null || psiFile == null || psiElement == null) {
            return;
        }

        final SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            Log.info("actionPerformed: selectionModel.hasSelection()");
            return;
        }

        CommandProcessor.getInstance().executeCommand(project, () -> {
            try {
                MultiHighlightHandler.invoke(project, editor, psiFile);
            } catch (IndexNotReadyException ex) {
                DumbService.getInstance(project)
                        .showDumbModeNotification("This usage search requires indices "
                                + "and cannot be performed until they are built");
            }
        }, "MultiHighlight", null);
    }
}
