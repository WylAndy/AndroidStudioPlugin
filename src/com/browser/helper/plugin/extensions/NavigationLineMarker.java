package com.browser.helper.plugin.extensions;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.ListActions;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NavigationLineMarker extends LineMarkerProviderDescriptor implements GutterIconNavigationHandler<PsiElement> {

    private Icon navigationOnIcon = IconLoader.getIcon("/drawable/location_icon.png");

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Nullable("null means disabled")
    @Override
    public String getName() {
        return "Location";
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (isNavigationCall(element)) {
            navigationOnIcon = IconLoader.getDarkIcon(navigationOnIcon, true);
            return new LineMarkerInfo(element, element.getTextRange(), navigationOnIcon, SyntaxTraverser.psiApiReversed(), this, GutterIconRenderer.Alignment.LEFT);
        } else {
            return null;
        }
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }

    @Override
    public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
        if (psiElement instanceof PsiMethodCallExpression) {
            Project project = psiElement .getProject();
            PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
            String target = expression.getArgumentList().getExpressions()[0].getText();
            if (!target.contains(".")) {
                notifyNotFound("Could not resolve parameter");
                return;
            }
            String[] array = target.split("\\.");
            if (array.length != 3 || !"BrowserManifest".equals(array[0]) || !"PageView".equals(array[1]) && !"PageDialog".equals(array[1]) && !"Server".equals(array[1])) {
                notifyNotFound("Could not resolve parameter");
                return;
            }
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(String.format("com.browser.manifest.%s", array[0]), GlobalSearchScope.allScope(project));
            if (psiClass == null) {
                notifyNotFound("Could not find browser manifest");
                return;
            }
            PsiClass innerClass = psiClass.findInnerClassByName(array[1], false);
            if (innerClass == null) {
                notifyNotFound(String.format("Could not find %s in browser manifest", array[1]));
                return;
            }
            PsiField field = innerClass.findFieldByName(array[2], false);
            if (field == null) {
                notifyNotFound(String.format("Could not find %s in %s", array[2], array[1]));
                return;
            }
            String className = (String) field.computeConstantValue();
            if (TextUtils.isEmpty(className)) {
                notifyNotFound("Could not find target");
                return;
            }
            PsiClass pageClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
            if (pageClass != null) pageClass.navigate(true);
            else notifyNotFound("Could not find target");
        } else if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            PsiAnnotation annotation = psiClass.getAnnotation("com.browser.annotations.PageView");
            if (annotation == null) return;
            PsiAnnotationMemberValue value = annotation.findAttributeValue("viewId");
            if (value == null) return;
            String layoutFileName = value.getText();
            if (TextUtils.isEmpty(layoutFileName)) return;
            layoutFileName = layoutFileName.replace("\"", "");
            navigateToLayout(psiElement.getProject(), layoutFileName);
        } else if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            PsiClass psiClass = psiField.getContainingClass();
            if (psiClass == null) return;
            PsiAnnotation fieldAnnotation = psiField.getAnnotation("com.browser.annotations.BindView");
            if (fieldAnnotation == null) return;
            PsiAnnotation annotation = psiClass.getAnnotation("com.browser.annotations.PageView");
            if (annotation == null) return;
            PsiAnnotationMemberValue value = annotation.findAttributeValue("viewId");
            if (value == null) return;
            String layoutFileName = value.getText();
            layoutFileName = layoutFileName.replace("\"", "");
            navigateToLayout(psiElement.getProject(), layoutFileName);
        }
    }
    //TODO 后续处理不同module，layout文件名相同的情况
    private void navigateToLayout(Project project, String layoutName) {
        VirtualFile rootFile = VirtualFileManager.getInstance().findFileByUrl("file://" + project.getBasePath());
        if (rootFile == null) return;
        List<Pair<String, String>> idList = new ArrayList<>(10);
        for (VirtualFile virtualFile : rootFile.getChildren()) {
            VirtualFile findFile = virtualFile.findFileByRelativePath(String.format("src/main/res/layout/%s.xml", layoutName));
            if (findFile != null) {
                XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(findFile);
                assert xmlFile != null;
                xmlFile.navigate(false);
            }
        }
    }

    /**
     * Judge whether the code used for navigation.
     */
    private boolean isNavigationCall(PsiElement psiElement) {
        if (psiElement instanceof PsiCallExpression) {
            PsiMethod method = ((PsiCallExpression) psiElement).resolveMethod();
            if (method == null) return false;
            PsiElement parent = method.getParent();
            PsiExpressionList expressionList = ((PsiCallExpression) psiElement).getArgumentList();
            if (expressionList == null) return false;
            String target = expressionList.getExpressions()[0].getText();
            if (!target.contains(".")) {
                return false;
            }
            String[] array = target.split("\\.");
            if (array.length != 3 || !"BrowserManifest".equals(array[0]) || !"PageView".equals(array[1]) && !"PageDialog".equals(array[1]) && !"Server".equals(array[1])) {
                return false;
            }
            return "showPageView".equals(method.getName()) && parent instanceof PsiClass;
        } else if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            PsiAnnotation annotation = psiClass.getAnnotation("com.browser.annotations.PageView");
            return annotation != null;
        } else if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            PsiClass psiClass = psiField.getContainingClass();
            if (psiClass == null) return false;
            PsiAnnotation fieldAnnotation = psiField.getAnnotation("com.browser.annotations.BindView");
            if (fieldAnnotation == null) return false;
            PsiAnnotation annotation = psiClass.getAnnotation("com.browser.annotations.PageView");
            if (annotation == null) return false;
            PsiAnnotationMemberValue value = annotation.findAttributeValue("viewId");
            if (value == null) return false;
            String layoutFileName = value.getText();
            return !TextUtils.isEmpty(layoutFileName);
        }
        return false;
    }

    private void notifyNotFound(String message) {
        String NOTIFY_SERVICE_NAME = "Hotchpotch Plugin Tips";
        String NOTIFY_TITLE = "Location error";
        Notifications.Bus.notify(new Notification(NOTIFY_SERVICE_NAME, NOTIFY_TITLE, message, NotificationType.WARNING));
    }
}
