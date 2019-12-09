package com.browser.helper.plugin.action;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.apache.http.util.TextUtils;

import java.util.List;

public class AnnotationsModel implements Runnable {
    private boolean hasPage;
    private String pageName;
    private String serverPackageName;
    private String threadMode;
    private String priority;
    private boolean isSticky;
    private Project project;
    private List<PsiMethod> methodList;
    private PsiClass interfaceClass;
    private String manifestPackageName;

    public static Builder newBuilder() {
        return new Builder(new AnnotationsModel());
    }

    @Override
    public void run() {
        for (PsiMethod method : methodList) {
            if (method.hasAnnotation("com.browser.annotations.Injection")) {
                continue;
            }
            PsiModifierList modifierList = method.getModifierList();
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("@com.browser.annotations.Injection( source = %s.Server.%s, threadMode = com.browser.annotations.ThreadMode.%s", manifestPackageName, serverPackageName, threadMode));
            if (!TextUtils.isEmpty(priority)) {
                stringBuilder.append(String.format("\n, priority = %s", priority));
            }
            if (isSticky) {
                stringBuilder.append(", sticky = true");
            }
            stringBuilder.append(")");
            PsiElement injectAnnotation = elementFactory.createAnnotationFromText(stringBuilder.toString(), method);
            modifierList.addBefore(injectAnnotation, modifierList.getFirstChild());
            if (hasPage) {
                if (!method.hasAnnotation("com.browser.annotations.Page")) {
                    PsiElement pageAnnotation = elementFactory.createAnnotationFromText(String.format("@com.browser.annotations.Page( name = %s.%s)", manifestPackageName, pageName), method);
                    modifierList.addBefore(pageAnnotation, modifierList.getFirstChild());
                    String fieldName = method.getName();
                    if (interfaceClass.findFieldByName(fieldName, false) == null) {
                        PsiElement field = elementFactory.createFieldFromText(String.format("String %s = \"%s\";", fieldName, fieldName), interfaceClass);
                        interfaceClass.add(field);
                    }
                }
            }
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
            styleManager.optimizeImports(interfaceClass.getContainingFile());
            styleManager.shortenClassReferences(interfaceClass);
            new ReformatCodeProcessor(project, interfaceClass.getContainingFile(), null, false).runWithoutProgress();
        }
    }

    public static class Builder {
        private AnnotationsModel model;

        Builder(AnnotationsModel model) {
            this.model = model;
        }

        public Builder setHasPage(boolean hasPage) {
            model.hasPage = hasPage;
            return this;
        }

        public Builder setPageName(String pageName) {
            model.pageName = pageName;
            return this;
        }

        public Builder setServerPackageName(String serverPackageName) {
            model.serverPackageName = serverPackageName;
            return this;
        }

        public Builder setThreadMode(String threadMode) {
            model.threadMode = threadMode;
            return this;
        }

        public Builder setPriority(String priority) {
            model.priority = priority;
            return this;
        }

        public Builder setSticky(boolean sticky) {
            model.isSticky = sticky;
            return this;
        }

        public Builder setProject(Project project) {
            model.project = project;
            return this;
        }

        public Builder setMethodList(List<PsiMethod> methodList) {
            model.methodList = methodList;
            return this;
        }

        public Builder setInterfaceClass(PsiClass interfaceClass) {
            model.interfaceClass = interfaceClass;
            return this;
        }

        public Builder setManifestPackageName(String manifestPackageName) {
            model.manifestPackageName = manifestPackageName;
            return this;
        }

        public void build() {
            WriteCommandAction.runWriteCommandAction(model.project, model);
        }
    }
}
