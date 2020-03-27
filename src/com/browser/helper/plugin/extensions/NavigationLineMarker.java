package com.browser.helper.plugin.extensions;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class NavigationLineMarker extends LineMarkerProviderDescriptor implements GutterIconNavigationHandler<PsiElement> {

    private Icon navigationOnIcon = IconLoader.getIcon("/drawable/warning_icon.png");
    private String NOTIFY_SERVICE_NAME = "ARouter Plugin Tips";
    private String NOTIFY_TITLE = "Road Sign";
    private String NOTIFY_NO_TARGET_TIPS = "No destination found or unsupported type.";

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
            PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
            String target = expression.getArgumentList().getExpressions()[0].getText().split("\\.")[2];
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
            if ("showPageView".equals(method.getName()) && parent instanceof PsiClass) {
                    return true;
            }
        }
        return false;
    }

    private void notifyNotFound() {
        Notifications.Bus.notify(new Notification(NOTIFY_SERVICE_NAME, NOTIFY_TITLE, NOTIFY_NO_TARGET_TIPS, NotificationType.WARNING));
    }
}
