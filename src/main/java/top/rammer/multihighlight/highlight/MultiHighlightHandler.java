package top.rammer.multihighlight.highlight;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.highlighting.HighlightHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightManagerImpl;
import com.intellij.codeInsight.highlighting.HighlightUsagesDescriptionLocation;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.find.EditorSearchSession;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageTargetUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import top.rammer.multihighlight.Log;
import top.rammer.multihighlight.config.NamedTextAttr;

/**
 * Created by Rammer on 06/02/2017.
 */
public class MultiHighlightHandler {
    
    /**
     * {@link com.intellij.codeInsight.highlighting.HighlightUsagesHandler#invoke(Project, Editor,
     * PsiFile)}
     *
     * {@link com.intellij.codeInsight.daemon.impl.IdentifierHighlighterPass#doCollectInformation(ProgressIndicator)}
     */
    public static void invoke(@NotNull Project project, @NotNull Editor editor,
            @NotNull PsiFile file) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        
        if (handleCustomUsage(editor, file)) {
            return;
        }
        
        DumbService.getInstance(project).withAlternativeResolveEnabled(() -> {
            if (!findTarget(project, editor, file)) {
                handleNoUsageTargets(file, editor, project);
            }
        });
    }
    
    private static boolean handleCustomUsage(@NotNull Editor editor, @NotNull PsiFile file) {
        final HighlightUsagesHandlerBase handler =
                HighlightUsagesHandler.createCustomHandler(editor, file);
        
        if (handler == null) {
            return false;
        }
        
        final String featureId = handler.getFeatureId();
        if (featureId != null) {
            FeatureUsageTracker.getInstance().triggerFeatureUsed(featureId);
        }
        
        final List targets = handler.getTargets();
        if (targets == null) {
            return false;
        }
        
        try {
            // TODO: 06/02/2017 handle custom usages
            handler.highlightUsages();
//            targets
//            handler.computeUsages(targets);
//            final List readUsages = handler.getReadUsages();
//            final List writeUsages = handler.getWriteUsages();
            Log.className("handleCustomUsage", handler);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private static boolean findTarget(@NotNull Project project, @NotNull Editor editor,
            @NotNull PsiFile file) {
        UsageTarget[] usageTargets = UsageTargetUtil.findUsageTargets(editor, file);
        if (usageTargets != null) {
            for (UsageTarget target : usageTargets) {
                if (target instanceof PsiElement2UsageTargetAdapter) {
                    highlightPsiElement(project,
                            ((PsiElement2UsageTargetAdapter) target).getElement(), editor, file,
                            isClearHighlights(editor));
                } else {
                    Log.className("highlightUsageTarget usageTarget", target);
                    target.highlightUsages(file, editor, isClearHighlights(editor));
                }
            }
        } else {
            final PsiElement psiElement = findTargetElement(editor, file);
            if (psiElement != null) {
                highlightPsiElement(project, psiElement, editor, file, isClearHighlights(editor));
            } else {
                boolean found = false;
                
                final PsiReference ref = TargetElementUtil.findReference(editor);
                if (ref instanceof PsiPolyVariantReference) {
                    ResolveResult[] results = ((PsiPolyVariantReference) ref).multiResolve(false);
                    if (results.length > 0) {
                        final boolean shouldClear = isClearHighlights(editor);
                        for (ResolveResult result : results) {
                            final PsiElement element = result.getElement();
                            if (element != null) {
                                highlightPsiElement(project, element, editor, file, shouldClear);
                                found = true;
                            }
                        }
                    }
                }
                
                return found;
            }
        }
        
        return true;
    }
    
    private static boolean isClearHighlights(@NotNull Editor editor) {
        if (editor instanceof EditorWindow) {
            editor = ((EditorWindow) editor).getDelegate();
        }
        
        final Project project = editor.getProject();
        if (project == null) {
            Log.error("isClearHighlights: editor.getProject() == null");
            return false;
        }
        
        int caretOffset = editor.getCaretModel().getOffset();
        final RangeHighlighter[] highlighters =
                ((HighlightManagerImpl) HighlightManager.getInstance(project)).getHighlighters(
                        editor);
        for (RangeHighlighter highlighter : highlighters) {
            if (TextRange.create(highlighter).grown(1).contains(caretOffset)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static PsiElement findTargetElement(@NotNull Editor editor, @NotNull PsiFile file) {
        PsiElement targetElement = TargetElementUtil.findTargetElement(editor,
                TargetElementUtil.getInstance().getReferenceSearchFlags());
        
        if (targetElement != null && targetElement != file) {
            if (targetElement instanceof NavigationItem) {
                targetElement = (targetElement).getNavigationElement();
            }
            
            if (targetElement instanceof NavigationItem) {
                return targetElement;
            }
        }
        
        return null;
    }
    
    private static void highlightPsiElement(@NotNull Project project,
            @NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull PsiFile file,
            boolean shouldClear) {
        final PsiElement target = SmartPointerManager.getInstance(psiElement.getProject())
                .createSmartPsiElementPointer(psiElement)
                .getElement();
        
        if (target == null) {
            return;
        }
        
        if (file instanceof PsiCompiledFile) {
            file = ((PsiCompiledFile) file).getDecompiledPsiFile();
        }
        
        final Couple<List<TextRange>> usages = getUsages(target, file);
        
        final List<TextRange> readRanges = usages.first;
        final List<TextRange> writeRanges = usages.second;
        
        final HighlightManager highlightManager = HighlightManager.getInstance(project);
        if (shouldClear) {
            clearHighlights(editor, highlightManager, readRanges);
            clearHighlights(editor, highlightManager, writeRanges);
            WindowManager.getInstance().getStatusBar(project).setInfo("");
            return;
        }

        // TODO: 10/02/2017 pass target?
        final TextAttributes ta = TextAttributesFactory.getInstance().get();
        final Color scrollMarkColor;
        if (ta.getErrorStripeColor() != null) {
            scrollMarkColor = ta.getErrorStripeColor();
        } else if (ta.getBackgroundColor() != null) {
            scrollMarkColor = ta.getBackgroundColor().darker();
        } else {
            scrollMarkColor = null;
        }
        
        final String elementName = ElementDescriptionUtil.getElementDescription(target,
                HighlightUsagesDescriptionLocation.INSTANCE);
        
        // TODO: 06/02/2017 highlight write and read access
        ArrayList<RangeHighlighter> highlighters = new ArrayList<>();
        highlight(highlightManager, readRanges, editor, ta, highlighters, scrollMarkColor);
        highlight(highlightManager, writeRanges, editor, ta, highlighters, scrollMarkColor);

        final Document doc = editor.getDocument();
        for (RangeHighlighter highlighter : highlighters) {
            final String desc = HighlightHandlerBase.getLineTextErrorStripeTooltip(doc,
                    highlighter.getStartOffset(), true);
            highlighter.setErrorStripeTooltip(MultiHighlightTooltip.create(target, desc));
        }

        int refCount = readRanges.size() + writeRanges.size();
        String msg;
        if (refCount > 0) {
            msg = MessageFormat.format("{0} {0, choice, 1#usage|2#usages} of {1} found", refCount,
                    elementName);
        } else {
            msg = MessageFormat.format("No usages of {0} found", elementName);
        }
        
        WindowManager.getInstance().getStatusBar(project).setInfo(msg);
    }
    
    @NotNull
    private static Couple<List<TextRange>> getUsages(@NotNull PsiElement target,
            @NotNull PsiElement psiElement) {
        List<TextRange> readRanges = new ArrayList<>();
        List<TextRange> writeRanges = new ArrayList<>();
        final ReadWriteAccessDetector detector = ReadWriteAccessDetector.findDetector(target);
        final FindUsagesManager findUsagesManager = ((FindManagerImpl) FindManager.getInstance(
                target.getProject())).getFindUsagesManager();
        final FindUsagesHandler findUsagesHandler =
                findUsagesManager.getFindUsagesHandler(target, true);
        final LocalSearchScope scope = new LocalSearchScope(psiElement);
        Collection<PsiReference> refs =
                findUsagesHandler != null ? findUsagesHandler.findReferencesToHighlight(target,
                        scope) : ReferencesSearch.search(target, scope).findAll();
        for (PsiReference psiReference : refs) {
            if (psiReference == null) {
                Log.error("Null reference returned, findUsagesHandler=" + findUsagesHandler
                        + "; target=" + target + " of " + target.getClass());
                continue;
            }
            List<TextRange> destination;
            if (detector == null || detector.getReferenceAccess(target, psiReference)
                    == ReadWriteAccessDetector.Access.Read) {
                destination = readRanges;
            } else {
                destination = writeRanges;
            }
            HighlightUsagesHandler.collectRangesToHighlight(psiReference, destination);
        }
        
        final TextRange declareRange =
                HighlightUsagesHandler.getNameIdentifierRange(psiElement.getContainingFile(),
                        target);
        if (declareRange != null) {
            if (detector != null && detector.isDeclarationWriteAccess(target)) {
                writeRanges.add(declareRange);
            } else {
                readRanges.add(declareRange);
            }
        }
        
        return Couple.of(readRanges, writeRanges);
    }
    
    private static void clearHighlights(Editor editor, HighlightManager highlightManager,
            List<TextRange> toRemoves) {
        if (editor instanceof EditorWindow) {
            editor = ((EditorWindow) editor).getDelegate();
        }
        
        RangeHighlighter[] highlighters =
                ((HighlightManagerImpl) highlightManager).getHighlighters(editor);
        
        Arrays.sort(highlighters, (o1, o2) -> o1.getStartOffset() - o2.getStartOffset());
        Collections.sort(toRemoves, (o1, o2) -> o1.getStartOffset() - o2.getStartOffset());
        
        int i = 0;
        int j = 0;
        while (i < highlighters.length && j < toRemoves.size()) {
            RangeHighlighter highlighter = highlighters[i];
            final TextAttributes ta = highlighter.getTextAttributes();
            final TextRange textRange = TextRange.create(highlighter);
            final TextRange toRemove = toRemoves.get(j);
            if (ta != null && ta instanceof NamedTextAttr // wrap
                    && highlighter.getLayer() == HighlighterLayer.SELECTION - 1 // wrap
                    && toRemove.equals(textRange)) {
                highlightManager.removeSegmentHighlighter(editor, highlighter);
                i++;
            } else if (toRemove.getStartOffset() > textRange.getEndOffset()) {
                i++;
            } else if (toRemove.getEndOffset() < textRange.getStartOffset()) {
                j++;
            } else {
                i++;
                j++;
            }
        }
    }
    
    private static void highlight(@NotNull HighlightManager highlightManager,
            @NotNull Collection<TextRange> textRanges, @NotNull Editor editor,
            @NotNull TextAttributes ta, @Nullable Collection<RangeHighlighter> holder,
            @Nullable Color scrollMarkColor) {
        for (TextRange range : textRanges) {
            highlightManager.addOccurrenceHighlight(editor, range.getStartOffset(),
                    range.getEndOffset(), ta, 0, holder, scrollMarkColor);
        }
    }
    
    /**
     * {@link com.intellij.codeInsight.highlighting.HighlightUsagesHandler#handleNoUsageTargets(PsiFile,
     * Editor, SelectionModel, Project)}
     */
    private static void handleNoUsageTargets(PsiFile file, @NotNull Editor editor,
            @NotNull Project project) {
        if (file.findElementAt(editor.getCaretModel().getOffset()) instanceof PsiWhiteSpace) {
            return;
        }
        final SelectionModel selectionModel = editor.getSelectionModel();
        selectionModel.selectWordAtCaret(false);
        String selection = selectionModel.getSelectedText();
//        LOG.assertTrue(selection != null);
        if (selection != null) {
            for (int i = 0; i < selection.length(); i++) {
                if (!Character.isJavaIdentifierPart(selection.charAt(i))) {
                    selectionModel.removeSelection();
                }
            }
            
            searchSelection(editor, project);
            selectionModel.removeSelection();
        }
    }
    
    private static void searchSelection(Editor editor, Project project) {
        final SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            selectionModel.selectWordAtCaret(false);
        }
        
        final String text = selectionModel.getSelectedText();
        if (text == null) {
            return;
        }
        
        if (editor instanceof EditorWindow) {
            // highlightUsages selection in the whole editor, not injected fragment only
            editor = ((EditorWindow) editor).getDelegate();
        }
        
        EditorSearchSession oldSearch = EditorSearchSession.get(editor);
        if (oldSearch != null) {
            if (oldSearch.hasMatches()) {
                String oldText = oldSearch.getTextInField();
                if (!oldSearch.getFindModel().isRegularExpressions()) {
                    oldText = StringUtil.escapeToRegexp(oldText);
                    oldSearch.getFindModel().setRegularExpressions(true);
                }
                
                String newText = oldText + '|' + StringUtil.escapeToRegexp(text);
                oldSearch.setTextInField(newText);
                return;
            }
        }
        
        EditorSearchSession.start(editor, project).getFindModel().setRegularExpressions(false);
    }
}
