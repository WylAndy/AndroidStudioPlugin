package com.browser.helper.plugin.action;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.http.util.TextUtils;

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

    private PsiDirectory rootDir;
    private Project project;

    public FragmentModel(PsiDirectory rootDir, Project project) {
        this.rootDir = rootDir;
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

    public FragmentModel setRootDir(PsiDirectory rootDir) {
        this.rootDir = rootDir;
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
        PsiDirectory srcDir = rootDir.findSubdirectory("java");
        PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        VirtualFile packageFile = makeDirs(Objects.requireNonNull(srcDir).getVirtualFile(), packageName);
        if (packageFile != null) {
            PsiClass fragmentClass = JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(packageFile), name);
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            StringBuilder extendsElement = new StringBuilder();
            if (!TextUtils.isEmpty(viewModelName) && !TextUtils.isEmpty(viewModelPackageName)) {
                extendsElement.append(String.format("com.browser.core.PageFragment<%s.%s>", viewModelPackageName, viewModelName));
                VirtualFile modelPackageFile = makeDirs(srcDir.getVirtualFile(), viewModelPackageName);
                if (modelPackageFile.findChild(viewModelName + ".java") == null) {
                    JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(modelPackageFile), viewModelName);
                }
            } else {
                extendsElement.append("com.browser.core.PageFragment");
            }
            PsiElement element = psiElementFactory.createReferenceFromText(extendsElement.toString(), fragmentClass);
            Objects.requireNonNull(fragmentClass.getExtendsList()).add(element);
            PsiElement annotation = psiElementFactory.createAnnotationFromText(createAnnotation(), fragmentClass);
            fragmentClass.addBefore(annotation, fragmentClass.getFirstChild());
            PsiClass serverClass = null;
            if (!TextUtils.isEmpty(serverClassName) && !TextUtils.isEmpty(serverPackageName)) {
                VirtualFile serverPackageFile = makeDirs(srcDir.getVirtualFile(), serverPackageName);
                if (serverPackageFile.findChild(serverClassName + ".java") == null) {
                    PsiDirectory psiDirectory = psiDirectoryFactory.createDirectory(serverPackageFile);
                    serverClass = JavaDirectoryService.getInstance().createClass(psiDirectory, serverClassName);
                    JavaDirectoryService.getInstance().createInterface(psiDirectory, "I" + serverClassName);
                    PsiElement implement = psiElementFactory.createReferenceElementByFQClassName(serverPackageName + "." + "I" + serverClassName, GlobalSearchScope.allScope(project));
                    Objects.requireNonNull(serverClass.getImplementsList()).add(implement);
                    String builder = String.format("@com.browser.annotations.TinyServer( name = \"%s\")", serverId);
                    PsiElement serverAnnotation = psiElementFactory.createAnnotationFromText(builder, serverClass);
                    serverClass.addBefore(serverAnnotation, serverClass.getFirstChild());
                }
                String iServer = "I" + serverClassName;
                if (JavaPsiFacade.getInstance(project).findClass(String.format("%s.I%s", serverPackageName, serverClassName), GlobalSearchScope.allScope(project)) != null) {
                    PsiElement var = psiElementFactory.createFieldFromText(String.format("private %s.%s m%s;", serverPackageName, iServer, serverClassName), serverClass);
                    fragmentClass.add(var);
                    PsiMethod onCreate = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                            "            public void onCreate(%s savedInstanceState) {\n" +
                            "               super.onCreate(savedInstanceState);\n" +
                            "               m%s = com.browser.core.Browser.getInstance().create(%s.%s.class);\n" +
                            "            }", "android.os.Bundle", serverClassName, serverPackageName, iServer), fragmentClass);
                    fragmentClass.add(onCreate);
                }
            }
            if (!TextUtils.isEmpty(layoutName)) {
                PsiDirectory resDir = rootDir.findSubdirectory("res");
                if (resDir == null) {
                    resDir = Objects.requireNonNull(Objects.requireNonNull(rootDir.getParent()).findSubdirectory("main")).findSubdirectory("res");
                }
                VirtualFile resFile = makeDirs(Objects.requireNonNull(resDir).getVirtualFile(), "layout");
                PsiDirectory psiDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(resFile);
                PsiFile xmlFile = psiDirectory.findFile(layoutName + ".xml");
                if (xmlFile == null) {
                    String xmlBuilder = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n" +
                            "<" + layoutRootElement + "\n" +
                            "        xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                            "        xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                            "        xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                            "        android:layout_width=\"match_parent\"\n" +
                            "        android:layout_height=\"match_parent\">\n" +
                            String.format("</%s>", layoutRootElement);
                    xmlFile = PsiFileFactory.getInstance(project).createFileFromText(layoutName + ".xml", StdFileTypes.XML, xmlBuilder);
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

    private VirtualFile makeDirs(VirtualFile virtualFile, String path) {
        String[] subDirs = path.contains(".") ? path.split("\\.") : path.contains("/") ? path.split("/") : new String[]{path};
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
