package com.browser.helper.plugin.action;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FragmentModel implements Runnable {
    private String packageName;
    private String name;
    private String containerId;
    private String id;
    private String activityName;
    private String activityFlags;
    private String viewModelName;
    private String viewModelPackageName;
    private String layoutName;
    private String layoutRootElement;
    private String serverId;
    private String serverClassName;
    private String serverPackageName;

    private String basePath;
    private Project project;

    public FragmentModel(String basePath, Project project) {
        this.basePath = basePath;
        this.project = project;
    }

    public FragmentModel() {
    }

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

    public FragmentModel setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public FragmentModel setProject(Project project) {
        this.project = project;
        return this;
    }

    public FragmentModel setLayoutName(String layoutName) {
        this.layoutName = layoutName;
        return this;
    }

    public FragmentModel setLayoutRootElement(String layoutRootElement) {
        this.layoutRootElement = layoutRootElement;
        return this;
    }

    public FragmentModel setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public FragmentModel setServerClassName(String serverClassName) {
        this.serverClassName = serverClassName;
        return this;
    }

    public FragmentModel setServerPackageName(String serverPackageName) {
        this.serverPackageName = serverPackageName;
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

    public String getLayoutName() {
        return layoutName;
    }

    private void buildClass() {
        String srcDir = basePath + File.separator + "src/main/java";
        PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        VirtualFile virtualFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(srcDir + File.separator + packageName.replace(".", "/"));
        if (virtualFile != null) {
            PsiClass fragmentClass = JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(virtualFile), name);
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            StringBuilder extendsElement = new StringBuilder();
            if (!TextUtils.isEmpty(viewModelName) && !TextUtils.isEmpty(viewModelPackageName)) {
                extendsElement.append(String.format("com.browser.core.PageFragment<%s.%s>", viewModelPackageName, viewModelName));
                String path = srcDir + File.separator + viewModelPackageName.replace(".", "/");
                VirtualFile packageFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(path);
                if (packageFile == null) {
                    VirtualFile rootFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(srcDir);
                    packageFile = makeDirs(rootFile, viewModelPackageName, "\\.");
                }
                JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(packageFile), viewModelName);
            } else {
                extendsElement.append("com.browser.core.PageFragment");
            }
            PsiElement element = psiElementFactory.createReferenceFromText(extendsElement.toString(), fragmentClass);
            Objects.requireNonNull(fragmentClass.getExtendsList()).add(element);
            PsiElement annotation = psiElementFactory.createAnnotationFromText(createAnnotation(), fragmentClass);
            fragmentClass.addBefore(annotation, fragmentClass.getFirstChild());
            PsiClass serverClass = null;
            if (!TextUtils.isEmpty(serverClassName) && !TextUtils.isEmpty(serverPackageName)) {
                String path = srcDir + File.separator + serverPackageName.replace(".", "/");
                VirtualFile packageFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(path);
                if (packageFile == null) {
                    VirtualFile rootFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(srcDir);
                    packageFile = makeDirs(rootFile, serverPackageName, "\\.");
                }
                PsiDirectory psiDirectory = psiDirectoryFactory.createDirectory(packageFile);
                serverClass = JavaDirectoryService.getInstance().createClass(psiDirectory, serverClassName);
                JavaDirectoryService.getInstance().createInterface(psiDirectory, "I" + serverClassName);
                PsiElement implement = psiElementFactory.createReferenceElementByFQClassName(serverPackageName + "." + "I" + serverClassName, GlobalSearchScope.allScope(project));
                serverClass.getImplementsList().add(implement);
                StringBuilder builder = new StringBuilder();
                builder.append("@com.browser.annotations.TinyServer(");
                builder.append(String.format("name = \"%s\")", serverId));
                PsiElement serverAnnotation = psiElementFactory.createAnnotationFromText(builder.toString(), serverClass);
                serverClass.addBefore(serverAnnotation, serverClass.getFirstChild());
                String iServer = "I" + serverClassName;
                PsiElement var = psiElementFactory.createFieldFromText(String.format("private %s.%s m%s;", serverPackageName, iServer, serverClassName), serverClass);
                fragmentClass.add(var);
                PsiMethod onCreate = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                        "            public void onCreate(%s savedInstanceState) {\n" +
                        "               super.onCreate(savedInstanceState);\n" +
                        "               m%s = com.browser.core.Browser.getInstance().create(%s.%s.class);\n" +
                        "            }", "android.os.Bundle", serverClassName, serverPackageName, iServer), fragmentClass);
                fragmentClass.add(onCreate);
            }
            if (!TextUtils.isEmpty(layoutName)) {
                String resDir = basePath + File.separator + "src/main/res/layout";
                VirtualFile resFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(resDir);
                if (resFile == null) {
                    VirtualFile baseFile = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(basePath);
                    resFile = makeDirs(baseFile, "src/main/res/layout", "/");
                }
                PsiDirectory psiDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(resFile);
                PsiFile xmlFile = psiDirectory.findFile(layoutName + ".xml");
                if (xmlFile == null) {
                    StringBuilder xmlBuilder = new StringBuilder();
                    xmlBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append("\n");
                    xmlBuilder.append("<").append(layoutRootElement).append("\n");
                    xmlBuilder.append("" +
                            "        xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                            "        xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                            "        xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                            "        android:layout_width=\"match_parent\"\n" +
                            "        android:layout_height=\"match_parent\">").append("\n");
                    xmlBuilder.append(String.format("</%s>", layoutRootElement));
                    xmlFile = PsiFileFactory.getInstance(project).createFileFromText(layoutName + ".xml", StdFileTypes.XML, xmlBuilder.toString());
                    psiDirectory.add(xmlFile);
                }
                PsiMethod onCreateView = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                        "            public android.view.View onCreateView(%s inflater, %s container, %s savedInstanceState) {\n" +
                        "               View view = inflater.inflate(R.layout.%s, container, false);\n" +
                        "               com.browser.core.Browser.getInstance().bind(this, view);\n" +
                        "               return view;\n" +
                        "            }", "android.view.LayoutInflater", "android.view.ViewGroup", "android.os.Bundle", layoutName), fragmentClass);
                fragmentClass.add(onCreateView);
            }

            PsiMethod onLoadFinished = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                    "            protected void onLoadFinished(%s model) {\n" +
                    "\n" +
                    "            }", viewModelName), fragmentClass);
            fragmentClass.add(onLoadFinished);
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
            styleManager.optimizeImports(fragmentClass.getContainingFile());
            styleManager.shortenClassReferences(fragmentClass);
            new ReformatCodeProcessor(project, fragmentClass.getContainingFile(), null, false).runWithoutProgress();
            if (serverClass != null) {
                styleManager.optimizeImports(serverClass.getContainingFile());
                styleManager.shortenClassReferences(serverClass);
                new ReformatCodeProcessor(project, serverClass.getContainingFile(), null, false).runWithoutProgress();
            }
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

    private VirtualFile makeDirs(VirtualFile virtualFile, String path, String regex) {
        String[] subDirs = path.contains(regex) ? path.split(regex) : new String[]{path};
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
