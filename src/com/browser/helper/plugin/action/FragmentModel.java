package com.browser.helper.plugin.action;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FragmentModel implements Runnable{
    private String packageName;
    private String name;
    private String containerId;
    private String id;
    private String activityName;
    private String activityFlags;
    private String viewModelName;
    private String viewModelPackageName;
    private boolean enableBack;

    private String basePath;
    private Project project;

    public FragmentModel(String basePath, Project project) {
        this.basePath = basePath;
        this.project = project;
    }

    public FragmentModel(){}

    public FragmentModel setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public FragmentModel setName(String name) {
        this.name = name;
        return this;
    }

    public FragmentModel setContainerId(String containerId) {
        this.containerId = containerId;
        return this;
    }

    public FragmentModel setId(String id) {
        this.id = id;
        return this;
    }

    public FragmentModel setActivityName(String activityName) {
        this.activityName = activityName;
        return this;
    }

    public FragmentModel setActivityFlags(String activityFlags) {
        this.activityFlags = activityFlags;
        return this;
    }

    public FragmentModel setViewModelName(String viewModelName) {
        this.viewModelName = viewModelName;
        return this;
    }

    public FragmentModel setViewModelPackageName(String viewModelPackageName) {
        this.viewModelPackageName = viewModelPackageName;
        return this;
    }

    public FragmentModel setEnableBack(boolean enableBack) {
        this.enableBack = enableBack;
        return this;
    }

    public FragmentModel setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public FragmentModel setProject(Project project) {
        this.project = project;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getId() {
        return id;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityFlags() {
        return activityFlags;
    }

    public String getViewModelName() {
        return viewModelName;
    }

    public String getViewModelPackageName() {
        return viewModelPackageName;
    }

    public boolean isEnableBack() {
        return enableBack;
    }

    private void buildClass() {
        PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        VirtualFile virtualFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(basePath + File.separator + packageName.replace(".", "/"));
        if (virtualFile != null) {
            PsiClass fragmentClass = JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(virtualFile), name);
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            StringBuilder extendsElement = new StringBuilder();
            if (!TextUtils.isEmpty(viewModelName) && !TextUtils.isEmpty(viewModelPackageName)) {
                extendsElement.append(String.format("com.browser.core.PageFragment<%s.%s>", viewModelPackageName, viewModelName));
                String path = basePath + File.separator + viewModelPackageName.replace(".", "/");
                VirtualFile modelFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(path);
                if (modelFile == null) {
                    VirtualFile rootFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(basePath);
                    VirtualFile packageFile = makePackageDir(rootFile, viewModelPackageName);
                    JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(packageFile), viewModelName);
                }
            }
            PsiElement element = psiElementFactory.createReferenceFromText(String.format("com.browser.core.PageFragment%s", TextUtils.isEmpty(viewModelName) ? "" : "<" + viewModelPackageName + "." + viewModelName + ">"), fragmentClass);
            Objects.requireNonNull(fragmentClass.getExtendsList()).add(element);
            PsiElement annotation = psiElementFactory.createAnnotationFromText(createAnnotation(), fragmentClass);
            fragmentClass.addBefore(annotation, fragmentClass.getFirstChild());
            PsiMethod method = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                    "            protected void onLoadFinished(%s model) {\n" +
                    "\n" +
                    "            }", viewModelName), fragmentClass);
            fragmentClass.add(method);
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
            styleManager.optimizeImports(fragmentClass.getContainingFile());
            styleManager.shortenClassReferences(fragmentClass);
            new ReformatCodeProcessor(project, fragmentClass.getContainingFile(), null, false).runWithoutProgress();
        }
    }

    @Override
    public void run() {
        buildClass();
    }

    private String createAnnotation() {
        StringBuilder builder = new StringBuilder();
        builder.append("@com.browser.annotations.PageView(");
        if (!TextUtils.isEmpty(name)) builder.append(String.format("name = \"%s\"", name));
        if (!TextUtils.isEmpty(activityName)) builder.append(String.format(", \nbrowserName = \"%s\"", activityName));
        if (!TextUtils.isEmpty(containerId)) builder.append(String.format(", \ncontainerId = \"%s\"", containerId));
        if (!TextUtils.isEmpty(activityFlags)) builder.append(String.format(", \nbrowserFlags = %s", activityFlags));
        builder.append(")");
        return builder.toString();
    }

    public void build() {
        WriteCommandAction.runWriteCommandAction(project, this);
    }

    private VirtualFile makePackageDir(VirtualFile virtualFile, String packageName) {
        String[] subDirs = packageName.split("\\.");
        VirtualFile rootFile = virtualFile;
        for (String subDir : subDirs) {
            VirtualFile childFile = rootFile.findChild(subDir);
            if (childFile == null) {
                try {
                    rootFile = rootFile.createChildDirectory(null, subDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                rootFile = childFile;
            }
        }
        return rootFile;
    }
}
