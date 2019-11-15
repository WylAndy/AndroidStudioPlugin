package com.browser.helper.plugin.action;

import com.browser.helper.plugin.view.NewPageFragmentDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
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
    private PsiDirectory rootDir = null;
    private Project project;
    private PsiDirectoryFactory psiDirectoryFactory;
    private String selectedPackageName;
    private boolean isShow = false;
    private JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
    private PsiDirectory sourceDir;
    private Module module;
    private static final String CLASS_TIP_FORMAT = "%s is not set to a valid class name";
    private static final String NAME_TIP_FORMAT = "%s is not set to a valid name";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // TODO: insert action logic here
        if (!isShow) return;
        psiDirectoryFactory = PsiDirectoryFactory.getInstance(Objects.requireNonNull(project));
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
        List<String> activityList = getActivityList(sourceDir);
        List<String> packageList = getPackageList(sourceDir);
        List<String> layoutList = new ArrayList<>(4);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("androidx.constraintlayout.widget.ConstraintLayout", GlobalSearchScope.allScope(project));
        if (psiClass != null) {
            layoutList.add("androidx.constraintlayout.widget.ConstraintLayout");
        }
        layoutList.add("RelativeLayout");
        layoutList.add("LinearLayout");
        layoutList.add("FrameLayout");
        List<String> intentFlagList = new ArrayList<>(intentFlags.keySet());
        pageFragmentDialog = new NewPageFragmentDialog(intentFlagList, activityList, layoutList);
        pageFragmentDialog.setOnCreateListener(new OnCreateListener());
        pageFragmentDialog.setPackageList(packageList, selectedPackageName);
        pageFragmentDialog.setActivityList(activityList);
        pageFragmentDialog.setTitle("New PageFragment");
        pageFragmentDialog.pack();
        pageFragmentDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.getProject()));
        pageFragmentDialog.setVisible(true);
    }

    private List<String> getActivityList(PsiDirectory directory) {
        List<String> activityList = new ArrayList<>(10);
        if (!directoryService.isSourceRoot(directory)) return activityList;
        List<PsiDirectory> psiDirectoryList = getSubdirectories(directory);
        for (PsiDirectory findDirectory : psiDirectoryList) {
            PsiClass[] psiClasses = Objects.requireNonNull(directoryService.getPackage(findDirectory)).getClasses();
            if (psiClasses.length > 0) {
                for (PsiClass psiClass : psiClasses) {
                    String activity = checkActivity(psiClass);
                    if (!TextUtils.isEmpty(activity)) {
                        activityList.add(activity);
                    }
                }
            }
        }
        return activityList;
    }

    private List<String> getPackageList(@NotNull PsiDirectory rootDir) {
        List<PsiDirectory> subdirectories = getSubdirectories(rootDir);
        List<String> packageList = new ArrayList<>(10);
        for (PsiDirectory subDir : subdirectories) {
            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(subDir);
            if (psiPackage != null)
                packageList.add(psiPackage.getQualifiedName());
        }
        return packageList;
    }

    private List<PsiDirectory> getSubdirectories(@NotNull PsiDirectory rootDir) {
        PsiDirectory subDirectory = rootDir;
        Stack<PsiDirectory> dirStack = new Stack<>();
        List<PsiDirectory> subDirectoryList = new ArrayList<>(10);
        while (subDirectory != null) {
            PsiDirectory[] subdirectories = subDirectory.getSubdirectories();
            for (PsiDirectory subDir : subdirectories) {
                dirStack.push(subDir);
                subDirectoryList.add(subDir);
            }
            subDirectory = dirStack.size() > 0 ? dirStack.pop() : null;
        }
        return subDirectoryList;
    }

    private PsiDirectory getSourceRoot(@NotNull PsiDirectory directory) {
        PsiDirectory parent = directory;
        while (parent != null) {
            if (directoryService.isSourceRoot(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private String checkActivity(PsiClass psiClass) {
        if (psiClass == null) return null;
        PsiMethod[] methods = psiClass.findMethodsByName("onCreate", true);
        int length = methods.length;
        if (length == 0) return null;
        PsiMethod lastMethod = methods[length - 1];
        PsiElement element = lastMethod.getParent();
        if (element instanceof PsiClass) {
            if (Objects.equals(((PsiClass) element).getQualifiedName(), "android.app.Activity")) {
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


    private void showWarnTip(String message) {
        pageFragmentDialog.setEnableOk(TextUtils.isEmpty(message));
        pageFragmentDialog.showTip(message);
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        VirtualFile virtualFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            isShow = false;
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        project = anActionEvent.getProject();
        module = anActionEvent.getData(LangDataKeys.MODULE);
        if (project == null || module == null) {
            isShow = false;
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("com.browser.core.Browser", module.getModuleWithDependenciesAndLibrariesScope(false));
        if (psiClass == null) {
            isShow = false;
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        PsiDirectory selectedDir = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile);
        PsiPackage psiPackage = directoryService.getPackage(selectedDir);
        if (psiPackage == null) {
            isShow = false;
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        sourceDir = getSourceRoot(selectedDir);
        if (!Objects.equals("java", sourceDir.getName())) {
            isShow = false;
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        rootDir = sourceDir.getParent();
        selectedPackageName = psiPackage.getQualifiedName();
        isShow = true;
    }

    class OnCreateListener implements NewPageFragmentDialog.OnCreateListener {
        private static final String validName = "^[a-zA-Z][a-zA-Z0-9_]*$";

        @Override
        public void onPackageChanged(String packageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(packageName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "PageFragment Name"));
        }

        @Override
        public void onFragmentNameChanged(String fragmentName) {
            boolean isValid = Pattern.matches(validName, fragmentName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "PageFragment Name"));
        }

        @Override
        public void onFragmentIdChanged(String fragmentId) {
            boolean isValid = Pattern.matches(validName, fragmentId);
            showWarnTip(isValid ? "" : String.format(NAME_TIP_FORMAT, "Fragment Id"));
        }

        @Override
        public void onViewModelNameChanged(String viewModelName) {
            boolean isValid = Pattern.matches(validName, viewModelName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "ViewModel Name"));
        }

        @Override
        public void onViewModelPackageNameChanged(String viewModelPackageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(viewModelPackageName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "ViewModel Name"));
        }

        @Override
        public void onContainerIdSelected(String containerId) {
            boolean isValid = Pattern.matches(validName, containerId);
            showWarnTip(isValid ? "" : String.format(NAME_TIP_FORMAT, "Container Id"));
        }

        @Override
        public void onActivitySelected(String activityName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(activityName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "Activity Name"));
            if (isValid) {
                PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(activityName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
                showWarnTip(psiClass == null ? "Activity is not defined" : "");
            }
        }

        @Override
        public void onLayoutNameChanged(String layoutName) {
            boolean isValid = Pattern.matches(validName, layoutName);
            showWarnTip(isValid ? "" : String.format(NAME_TIP_FORMAT, "Layout Name"));
        }

        @Override
        public void onServerPackageChanged(String packageName) {
            boolean isValid = psiDirectoryFactory.isValidPackageName(packageName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "TinyServer Name"));
        }

        @Override
        public void onServerNameChanged(String serverName) {
            boolean isValid = Pattern.matches(validName, serverName);
            showWarnTip(isValid ? "" : String.format(CLASS_TIP_FORMAT, "TinyServer Name"));
        }

        @Override
        public void onServerIdChanged(String serverId) {
            boolean isValid = Pattern.matches(validName, serverId);
            showWarnTip(isValid ? "" : String.format(NAME_TIP_FORMAT, "TinyServer Id"));
        }

        @Override
        public void onOK(FragmentModel model) {
            model.setRootDir(rootDir)
                    .setProject(project)
                    .build();
        }

        @Override
        public void onCancel() {

        }
    }
}
