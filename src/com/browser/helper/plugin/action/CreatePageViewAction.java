package com.browser.helper.plugin.action;

import com.browser.helper.plugin.view.NewPageFragmentDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class CreatePageViewAction extends AnAction {

    private NewPageFragmentDialog pageFragmentDialog;
    private static Map<String, Integer> intentFlags;
    private List<String> activityList;
    private PsiDirectory rootDir = null;
    private PsiDirectory sourceDir = null;
    private FragmentModel fragmentModel;
    private Project project;
    private PsiDirectoryFactory psiDirectoryFactory;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // TODO: insert action logic here
        project = anActionEvent.getProject();
        psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        Object[] objects = anActionEvent.getData(PlatformDataKeys.SELECTED_ITEMS);
        PsiDirectory selectedDir = null;
        if (objects != null && objects.length > 0 && objects[0] instanceof PsiDirectory) {
            selectedDir = (PsiDirectory) objects[0];
        }
        if (selectedDir == null) return;
        boolean isPackage = PsiDirectoryFactory.getInstance(project).isPackage(selectedDir);
        if (isPackage) {
            if (intentFlags == null) {
                intentFlags = new HashMap<>(21);
                PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("android.content.Intent", GlobalSearchScope.allScope(project));
                if (psiClass == null) return;
                PsiField[] psiFields = psiClass.getFields();
                for (PsiField psiField : psiFields) {
                    String fieldName = psiField.getName();
                    if (fieldName != null && fieldName.startsWith("FLAG_ACTIVITY_")) {
                        intentFlags.put(fieldName, (Integer) psiField.computeConstantValue());
                    }
                }
            }

            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(selectedDir);
            Objects.requireNonNull(psiPackage, "selected package is invalid");
            String packageName = psiPackage.getQualifiedName();
            sourceDir = selectedDir.getParent();
            if (sourceDir == null) return;
            for (int i = 0; i < packageName.split("\\.").length; i++) {
                sourceDir = sourceDir.getParent();
            }
            rootDir = sourceDir.getParent();
            for (int i = 0; i < 3; i++) {
                rootDir = rootDir.getParent();
            }
            activityList = getActivityList(sourceDir);
            if (pageFragmentDialog == null) {
                List<String> layoutList = new ArrayList<>(4);
                PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("androidx.constraintlayout.widget.ConstraintLayout", GlobalSearchScope.allScope(project));
                if (psiClass != null) {
                    layoutList.add("androidx.constraintlayout.widget.ConstraintLayout");
                }
                layoutList.add("RelativeLayout");
                layoutList.add("LinearLayout");
                layoutList.add("FrameLayout");
                List<String> intentFlagList = new ArrayList<>(intentFlags.keySet());
                pageFragmentDialog = new NewPageFragmentDialog(intentFlagList, activityList, layoutList, packageName);
                pageFragmentDialog.setOnCreateListener(new OnCreateListener());
            }
            pageFragmentDialog.setPackageName(packageName);
            pageFragmentDialog.setActivityList(activityList);
            pageFragmentDialog.setTitle("New PageFragment");
            pageFragmentDialog.pack();
            pageFragmentDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.getProject()));
            pageFragmentDialog.setVisible(true);
        }
    }

    private List<String> getActivityList(PsiDirectory directory) {
        List<String> activityList = new ArrayList<>(10);
        Project project = directory.getProject();
        Stack<VirtualFile> fileStack = new Stack<>();
        VirtualFile virtualFile = directory.getVirtualFile();
        while (virtualFile != null) {
            VirtualFile[] children = virtualFile.getChildren();
            for (VirtualFile file : children) {
                if (file.isDirectory()) {
                    PsiDirectory psiDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(file);
                    if (PsiDirectoryFactory.getInstance(project).isPackage(psiDirectory)) {
                        fileStack.push(file);
                        PsiClass[] psiClasses = JavaDirectoryService.getInstance().getPackage(psiDirectory).getClasses();
                        if (psiClasses.length > 0) {
                            for (PsiClass psiClass : psiClasses) {
                                String activity = checkActivity(psiClass);
                                if (!TextUtils.isEmpty(activity)) {
                                    activityList.add(activity);
                                }
                            }
                        }
                    }
                }
            }
            virtualFile = fileStack.size() > 0 ? fileStack.pop() : null;
        }
        return activityList;
    }

    private String checkActivity(PsiClass psiClass) {
        if (psiClass == null) return null;
        PsiMethod[] methods = psiClass.findMethodsByName("onCreate", true);
        int length = methods.length;
        if (length == 0) return null;
        PsiMethod lastMethod = methods[length - 1];
        PsiElement element = lastMethod.getParent();
        if (element instanceof PsiClass) {
            if (((PsiClass) element).getQualifiedName().equals("android.app.Activity")) {
                return psiClass.getQualifiedName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Map<String, List<String>> getActivityMap(PsiDirectory directory) {
        Map<String, List<String>> activityMap = new HashMap<>(10);
        Project project = directory.getProject();
        Stack<VirtualFile> fileStack = new Stack<>();
        VirtualFile virtualFile = directory.getVirtualFile();
        while (virtualFile != null) {
            VirtualFile[] children = virtualFile.getChildren();
            for (VirtualFile file : children) {
                if (file.isDirectory()) {
                    fileStack.push(file);
                    PsiClass[] psiClasses = JavaDirectoryService.getInstance().getPackage(PsiDirectoryFactory.getInstance(project).createDirectory(file)).getClasses();
                    if (psiClasses.length > 0) {
                        for (PsiClass psiClass : psiClasses) {
                            String layoutName = getContentViewLayout(psiClass);
                            if (!TextUtils.isEmpty(layoutName)) {
                                XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(String.format("%s/%s.xml", rootDir.getVirtualFile().getPath() + "/src/main/res/layout", layoutName)));
                                if (xmlFile != null) {
                                    XmlTag[] xmlTags = xmlFile.getRootTag().getSubTags();
                                    List<String> idList = new ArrayList<>(10);
                                    for (XmlTag tag : xmlTags) {
                                        String id = tag.getAttributeValue("android:id").replace("@+id/", "");
                                        if (!TextUtils.isEmpty(id)) idList.add(id);
                                    }
                                    if (idList.size() > 0) {
                                        activityMap.put(psiClass.getQualifiedName(), idList);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            virtualFile = fileStack.size() > 0 ? fileStack.pop() : null;
        }
        return activityMap;
    }

    private String getContentViewLayout(PsiClass psiClass) {
        if (psiClass == null) return null;
        PsiMethod[] methods = psiClass.findMethodsByName("onCreate", true);
        int length = methods.length;
        if (length == 0) return null;
        PsiMethod lastMethod = methods[length - 1];
        PsiElement element = lastMethod.getParent();
        if (element instanceof PsiClass) {
            if (((PsiClass) element).getQualifiedName().equals("android.app.Activity")) {
                PsiMethod firstMethod = methods[0];
                PsiCodeBlock body = firstMethod.getBody();
                if (body == null) return null;
                PsiStatement[] statements = body.getStatements();
                String layoutName = null;
                for (PsiStatement statement : statements) {
                    PsiElement psiElement = statement.getFirstChild().getFirstChild();
                    if (psiElement != null && psiElement.getText().equals("setContentView")) {
                        layoutName = statement.getFirstChild().getLastChild().getText();
                        if (layoutName.contains("R.layout.")) {
                            layoutName = layoutName.replace("(R.layout.", "").replace(")", "");
                        }
                    }
                }
                return layoutName;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private void showWarnTip(boolean isValid, String labelName) {
        pageFragmentDialog.setEnableOk(isValid);
        if (isValid) {
            pageFragmentDialog.showTip("");
        } else {
            pageFragmentDialog.showTip(String.format("%s is not set to a valid value", labelName));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        Object[] objects = anActionEvent.getData(PlatformDataKeys.SELECTED_ITEMS);
        PsiDirectory selectedDir = null;
        if (objects != null && objects.length > 0 && objects[0] instanceof PsiDirectory) {
            anActionEvent.getPresentation().setVisible(true);
        } else {
            anActionEvent.getPresentation().setVisible(false);
        }
    }

    class OnCreateListener implements NewPageFragmentDialog.OnCreateListener {
        private static final String validName = "^[a-zA-Z][a-zA-Z0-9_]*$";

        @Override
        public void onPackageChanged(String packageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(packageName);
            showWarnTip(isValid, "PageFragment Name");
        }

        @Override
        public void onFragmentNameChanged(String fragmentName) {
            boolean isValid = Pattern.matches(validName, fragmentName);
            showWarnTip(isValid, "PageFragment Name");
        }

        @Override
        public void onFragmentIdChanged(String fragmentId) {
            boolean isValid = Pattern.matches(validName, fragmentId);
            showWarnTip(isValid, "Fragment Id");
        }

        @Override
        public void onViewModelNameChanged(String viewModelName) {
            boolean isValid = Pattern.matches(validName, viewModelName);
            showWarnTip(isValid, "ViewModel Name");
        }

        @Override
        public void onViewModelPackageNameChanged(String viewModelPackageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(viewModelPackageName);
            showWarnTip(isValid, "ViewModel Name");
        }

        @Override
        public void onContainerIdSelected(String containerId) {
            boolean isValid = Pattern.matches(validName, containerId);
            showWarnTip(isValid, "Container Id");
        }

        @Override
        public void onActivitySelected(String activityName) {

        }

        @Override
        public void onLayoutNameChanged(String layoutName) {
            boolean isValid = Pattern.matches(validName, layoutName);
            showWarnTip(isValid, "Layout Name");
        }

        @Override
        public void onServerPackageChanged(String packageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(packageName);
            showWarnTip(isValid, "TinyServer Name");
        }

        @Override
        public void onServerNameChanged(String serverName) {
            boolean isValid = Pattern.matches(validName, serverName);
            showWarnTip(isValid, "TinyServer Name");
        }

        @Override
        public void onServerIdChanged(String serverId) {
            boolean isValid = Pattern.matches(validName, serverId);
            showWarnTip(isValid, "TinyServer Id");
        }

        @Override
        public void onOK(FragmentModel model) {
            model.setBasePath(rootDir.getVirtualFile().getPath())
                    .setProject(project)
                    .build();
        }

        @Override
        public void onCancel() {

        }
    }
}
