package com.browser.helper.plugin.action;

import com.browser.helper.plugin.utils.DirectoryTools;
import com.browser.helper.plugin.utils.NoticeDialog;
import com.browser.helper.plugin.utils.NoticeModel;
import com.browser.helper.plugin.view.AddAnnotationsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class AddAnnotationsAction extends AnAction {

    private PsiFile classFile;
    private PsiClass interfaceClass;
    private Editor editor;
    private PsiMethod[] interfaceMethods;
    private AddAnnotationsDialog annotationsDialog;
    private Module module;
    private PsiClass manifestClass;
    private HashMap<String, String> fieldMap = new HashMap<>(20);
    private NoticeDialog noticeDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        if (editor != null && interfaceClass != null && interfaceMethods != null) {
            Project project = e.getProject();
            PsiDirectory rootDir = Objects.requireNonNull(DirectoryTools.getSourceRoot(Objects.requireNonNull(interfaceClass.getContainingFile().getParent()))).getParent();
            manifestClass = DirectoryTools.findBrowserManifest(Objects.requireNonNull(rootDir), module);
            List<String> pageNameList = new ArrayList<>(10);
            List<String> serverList = new ArrayList<>(10);
            if (manifestClass != null) {
                PsiClass pageClass = manifestClass.findInnerClassByName("PageView", false);
                PsiClass dialogClass = manifestClass.findInnerClassByName("PageDialog", false);
                PsiClass serverClass = manifestClass.findInnerClassByName("Server", false);
                if (pageClass != null) {
                    PsiField[] fields = pageClass.getAllFields();
                    for (PsiField field : fields) {
                        pageNameList.add("PageView." + field.getName());
                        String text = ((PsiFieldImpl) field).getStubOrPsiChild(JavaStubElementTypes.LITERAL_EXPRESSION).getText();
                        fieldMap.put("PageView." + field.getName(), text.substring(1, text.length() - 1));
                    }
                }
                if (dialogClass != null) {
                    PsiField[] fields = dialogClass.getAllFields();
                    for (PsiField field : fields) {
                        pageNameList.add("PageDialog." + field.getName());
                        String text = ((PsiFieldImpl) field).getStubOrPsiChild(JavaStubElementTypes.LITERAL_EXPRESSION).getText();
                        fieldMap.put("PageDialog." + field.getName(), text.substring(1, text.length() - 1));
                    }
                }
                if (serverClass != null) {
                    PsiField[] fields = serverClass.getAllFields();
                    for (PsiField field : fields) {
                        serverList.add(field.getName());
                        String text = Objects.requireNonNull(((PsiFieldImpl) field).getStubOrPsiChild(JavaStubElementTypes.LITERAL_EXPRESSION)).getText();
                        fieldMap.put(field.getName(), text.substring(1, text.length() - 1));
                    }
                }
            }
            PsiClass threadModeClass = JavaPsiFacade.getInstance(Objects.requireNonNull(project)).findClass("com.browser.annotations.ThreadMode", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            List<String> threadModeList = new ArrayList<>(4);
            if (threadModeClass != null) {
                PsiField[] fields = threadModeClass.getAllFields();
                for (PsiField field : fields) {
                    threadModeList.add(field.getName());
                }
            }
            noticeDialog = annotationsDialog = new AddAnnotationsDialog();
            annotationsDialog.setPageNameList(pageNameList)
                    .setServerNameList(serverList)
                    .setThreadModeList(threadModeList)
                    .setDialogListener(new AnnotationsDialogListener());
            annotationsDialog.setTitle("Add annotations");
            annotationsDialog.pack();
            annotationsDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
            annotationsDialog.setVisible(true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        boolean isVisible = false;
        editor = e.getData(LangDataKeys.EDITOR);
        module = e.getData(LangDataKeys.MODULE);
        if (editor != null && module != null) {
            classFile = e.getData(LangDataKeys.PSI_FILE);
            if (classFile == null || !(classFile instanceof PsiJavaFile)) {
                isVisible = false;
            } else {
                PsiJavaFile javaFile = (PsiJavaFile) classFile;
                PsiClass[] classes = javaFile.getClasses();
                PsiClass serverClass = classes[0];
                if (serverClass.isInterface()) {
                    interfaceClass = serverClass;
                    String methodName = editor.getSelectionModel().getSelectedText();
                    PsiMethod[] psiMethods = interfaceClass.findMethodsByName(methodName, false);
                    if (psiMethods.length > 0) {
                        interfaceMethods = psiMethods;
                        isVisible = true;
                    } else {
                        isVisible = false;
                    }
                } else {
                    isVisible = false;
                }
            }
        } else {
            isVisible = false;
        }
        presentation.setVisible(isVisible);
    }

    private class AnnotationsDialogListener implements AddAnnotationsDialog.DialogListener {

        private static final String validName = "^(0|\\+?[1-9][0-9]*)$";

        @Override
        public void onPriorityChanged(String priority) {
            boolean isValid = Pattern.matches(validName, priority) || TextUtils.isEmpty(priority);
            showWarnTip(new NoticeModel("priority", isValid ? "" : "priority is not set to a valid value"));
        }

        @Override
        public void onPageNameChanged(String packageName) {
            PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass(fieldMap.get(packageName), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            boolean isValid = psiClass != null && psiClass.getSuperClass() != null && Objects.equals("com.browser.core.PageFragment", psiClass.getSuperClass().getQualifiedName());
            showWarnTip(new NoticeModel("page", isValid ? "" : "page name is not set to a valid value"));
        }

        @Override
        public void onImplicationChanged(String packageName) {
            PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass(fieldMap.get(packageName), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
            boolean isValid = psiClass != null;
            showWarnTip(new NoticeModel("implication", isValid ? "" : "implication is not set to a valid value"));
        }

        @Override
        public void onEnablePage(boolean isEnable) {
            if (!isEnable) showWarnTip(new NoticeModel("page", ""));
        }

        @Override
        public void onOK(AnnotationsModel.Builder builder) {
            builder.setInterfaceClass(interfaceClass)
                    .setManifestPackageName(manifestClass.getQualifiedName())
                    .setMethod(interfaceMethods[0])
                    .setProject(module.getProject())
                    .build();
        }
    }

    private void showWarnTip(NoticeModel noticeModel) {
        noticeDialog.showWarnTip(noticeModel);
    }
}
