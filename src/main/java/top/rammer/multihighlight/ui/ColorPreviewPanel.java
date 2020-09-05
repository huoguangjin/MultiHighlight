package top.rammer.multihighlight.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorMarkupModel;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.swing.JComponent;

import top.rammer.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 07/02/2017.
 */
public class ColorPreviewPanel implements PreviewPanel {

    private final EditorEx myEditor;
    
    public ColorPreviewPanel() {
        final EditorFactory editorFactory = EditorFactory.getInstance();
        myEditor = (EditorEx) editorFactory.createViewer(editorFactory.createDocument(""));

        final EditorSettings settings = myEditor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setWhitespacesShown(true);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(false);
        settings.setFoldingOutlineShown(false);
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setRightMarginShown(true);
        settings.setRightMargin(60);

        EditorMarkupModel markupModel = (EditorMarkupModel) myEditor.getMarkupModel();
        markupModel.setErrorStripeVisible(true);
    }

    @NotNull
    @Override
    public JComponent getPanel() {
        return myEditor.getComponent();
    }

    @Override
    public void updateView(@NotNull List<NamedTextAttr> namedTextAttrList) {
        UIUtil.invokeLaterIfNeeded(() -> {
            if (myEditor.isDisposed()) {
                return;
            }

            myEditor.getMarkupModel().removeAllHighlighters();

            myEditor.getSelectionModel().removeSelection();
            if (namedTextAttrList.isEmpty()) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    myEditor.getDocument().setText("");
                });
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (NamedTextAttr namedTextAttr : namedTextAttrList) {
                sb.append(namedTextAttr.getName()).append("\n");
            }
            ApplicationManager.getApplication().runWriteAction(() -> {
                myEditor.getDocument().setText(sb.toString());
            });

            int size = namedTextAttrList.size();
            for (int i = 0; i < size; i++) {
                highlightLine(i, namedTextAttrList.get(i));
            }
        });
    }

    private void highlightLine(int index, NamedTextAttr namedTextAttr) {
        UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
            try {
                MarkupModelEx markupModel = myEditor.getMarkupModel();
                final Document doc = markupModel.getDocument();
                final int lineStartOffset = doc.getLineStartOffset(index);
                final int lineEndOffset = doc.getLineEndOffset(index);
                // IDEA-53203: add ERASE_MARKER for manually defined attributes
                markupModel.addRangeHighlighter(lineStartOffset, lineEndOffset,
                        HighlighterLayer.SELECTION - 1, TextAttributes.ERASE_MARKER,
                        HighlighterTargetArea.EXACT_RANGE);
                RangeHighlighter rangeHighlight =
                        markupModel.addRangeHighlighter(lineStartOffset, lineEndOffset,
                                HighlighterLayer.SELECTION - 1, namedTextAttr,
                                HighlighterTargetArea.EXACT_RANGE);
                rangeHighlight.setErrorStripeMarkColor(namedTextAttr.getErrorStripeColor());
                rangeHighlight.setErrorStripeTooltip(namedTextAttr.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void blinkSelectedColor(@NotNull NamedTextAttr namedTextAttr) {

    }

    private void stopBlinking() {

    }

    @Override
    public void addListener(@NotNull PreviewSelectListener listener) {

    }

    @Override
    public void disposeUIResources() {
        EditorFactory.getInstance().releaseEditor(myEditor);
        stopBlinking();
    }
}
