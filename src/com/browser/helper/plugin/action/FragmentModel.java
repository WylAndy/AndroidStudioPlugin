package com.browser.helper.plugin.action;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FragmentModel implements Runnable {
    private String packageName;
    private String name;
    private String containerId;
    private String identityName;
    private String activityName;
    private String activityFlags;
    private String viewModelName;
    private String viewModelPackageName;
    private String layoutName;
    private String layoutRootElement;
    private String serverId;
    private String serverClassName;
    private String serverPackageName;
    private String permissionList;
    private boolean isDialog = false;

    private PsiDirectory rootDir;
    private Project project;
    private Module module;
    private String appPackageName;
    private PsiElementFactory elementFactory;

    public FragmentModel() {
    }

    public FragmentModel setModule(Module module) {
        this.module = module;
        return this;
    }


    public FragmentModel setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
        return this;
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

    public FragmentModel setIdentityName(String identityName) {
        this.identityName = identityName;
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
        //TODO PsiElementFactory.getInstance(project)会抛出异常，不知原因？
        elementFactory = JavaPsiFacade.getInstance(this.project).getElementFactory();
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

    public FragmentModel setPermissionList(String permissionList) {
        this.permissionList = permissionList;
        return this;
    }

    public FragmentModel setDialog(boolean dialog) {
        isDialog = dialog;
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

    public String getIdentityName() {
        return identityName;
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

    private void addField(PsiClass psiClass, String name, String value) {
        if (psiClass != null) {
            PsiField field = psiClass.findFieldByName(name, false);
            if (field == null) {
                field = elementFactory.createFieldFromText(String.format("public static final String %s = \"%s\";", name, value), psiClass);
                psiClass.add(field);
            }
        }
    }

    private void buildClass() {
        PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        PsiDirectory srcDir = rootDir.findSubdirectory("java");

        VirtualFile packageFile = makeDirs(Objects.requireNonNull(srcDir).getVirtualFile(), packageName);
        if (packageFile != null) {
            PsiClass fragmentClass = JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(packageFile), name);
            PsiClass browserManifest = createBrowserManifest();
            if (isDialog) {
                PsiClass dialogClass = browserManifest.findInnerClassByName("PageDialog", false);
                if (dialogClass != null) addField(dialogClass, identityName, fragmentClass.getQualifiedName());
            } else {
                PsiClass pageClass = browserManifest.findInnerClassByName("PageView", false);
                if (pageClass != null) addField(pageClass, identityName, fragmentClass.getQualifiedName());
            }
            PsiModifierList modifierList = fragmentClass.getModifierList();
            if (!Objects.requireNonNull(modifierList).hasModifierProperty(PsiModifier.PUBLIC))
                modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            StringBuilder extendsElement = new StringBuilder();
            if (!TextUtils.isEmpty(viewModelName) && !TextUtils.isEmpty(viewModelPackageName)) {
                extendsElement.append(String.format("com.browser.core.PageFragment<%s.%s>", viewModelPackageName, viewModelName));
                VirtualFile modelPackageFile = makeDirs(srcDir.getVirtualFile(), viewModelPackageName);
                if (modelPackageFile.findChild(viewModelName + ".java") == null) {
                    PsiClass psiClass = JavaDirectoryService.getInstance().createClass(psiDirectoryFactory.createDirectory(modelPackageFile), viewModelName);
                    modifierList = psiClass.getModifierList();
                    if (!Objects.requireNonNull(modifierList).hasModifierProperty(PsiModifier.PUBLIC))
                        modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                }
            } else {
                extendsElement.append("com.browser.core.PageFragment");
            }
            PsiElement element = psiElementFactory.createReferenceFromText(extendsElement.toString(), fragmentClass);
            Objects.requireNonNull(fragmentClass.getExtendsList()).add(element);
            PsiElement annotation = psiElementFactory.createAnnotationFromText(createAnnotation(), fragmentClass);
            PsiModifierList psiModifierList = fragmentClass.getModifierList();
            psiModifierList.addBefore(annotation, psiModifierList.getFirstChild());
            PsiClass serverClass = null;
            PsiClass iServerClass = null;
            if (!TextUtils.isEmpty(serverClassName) && !TextUtils.isEmpty(serverPackageName)) {
                VirtualFile serverPackageFile = makeDirs(srcDir.getVirtualFile(), serverPackageName);
                if (serverPackageFile.findChild(serverClassName + ".java") == null) {
                    PsiDirectory psiDirectory = psiDirectoryFactory.createDirectory(serverPackageFile);
                    serverClass = JavaDirectoryService.getInstance().createClass(psiDirectory, serverClassName);
                    modifierList = serverClass.getModifierList();
                    if (!Objects.requireNonNull(modifierList).hasModifierProperty(PsiModifier.PUBLIC))
                        modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                    iServerClass = JavaDirectoryService.getInstance().createInterface(psiDirectory, "I" + serverClassName);
                    modifierList = iServerClass.getModifierList();
                    if (!Objects.requireNonNull(modifierList).hasModifierProperty(PsiModifier.PUBLIC))
                        modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                    PsiElement serverAnnotation = psiElementFactory.createAnnotationFromText("@com.browser.annotations.TinyServer", fragmentClass);
                    modifierList.addBefore(serverAnnotation, modifierList.getFirstChild());
                    PsiElement psiElement = psiElementFactory.createReferenceElementByFQClassName("com.browser.core.BusinessServer", GlobalSearchScope.allScope(project));
                    Objects.requireNonNull(serverClass.getExtendsList()).add(psiElement);
//                    PsiElement implement = psiElementFactory.createReferenceElementByFQClassName(serverPackageName + "." + "I" + serverClassName, GlobalSearchScope.allScope(project));
//                    Objects.requireNonNull(serverClass.getImplementsList()).add(implement);
//                    String builder = String.format("@com.browser.annotations.TinyServer( name = \"%s\")", serverId);
//                    PsiElement serverAnnotation = psiElementFactory.createAnnotationFromText(builder, serverClass);
//                    serverClass.addBefore(serverAnnotation, serverClass.getModifierList().getFirstChild());
                }
                PsiClass server = browserManifest.findInnerClassByName("Server", false);
                if (server != null)
                    addField(server, serverId, Objects.requireNonNull(serverClass).getQualifiedName());
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
                        "            public void onViewCreated(%s view, %s savedInstanceState) {\n" +
                        "               com.browser.core.Browser.bind(this, view);\n" +
                        "            }", "android.view.View", "android.os.Bundle"), fragmentClass);
                fragmentClass.add(onCreateView);
            }
            if (!TextUtils.isEmpty(viewModelName) && !TextUtils.isEmpty(viewModelPackageName)) {
                PsiMethod onLoadFinished = psiElementFactory.createMethodFromText(String.format("@Override\n" +
                        "            protected void onLoadFinished(%s model) {\n" +
                        "\n" +
                        "            }", TextUtils.isEmpty(viewModelName) ? "java.lang.Object" : viewModelName), fragmentClass);
                fragmentClass.add(onLoadFinished);
            }
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
            styleManager.optimizeImports(fragmentClass.getContainingFile());
            styleManager.shortenClassReferences(fragmentClass);
            new ReformatCodeProcessor(project, fragmentClass.getContainingFile(), null, false).runWithoutProgress();
            if (serverClass != null) {
                styleManager.optimizeImports(serverClass.getContainingFile());
                styleManager.shortenClassReferences(serverClass);
                new ReformatCodeProcessor(project, serverClass.getContainingFile(), null, false).runWithoutProgress();
            }

            if (iServerClass != null) {
                styleManager.optimizeImports(iServerClass.getContainingFile());
                styleManager.shortenClassReferences(iServerClass);
                new ReformatCodeProcessor(project, iServerClass.getContainingFile(), null, false).runWithoutProgress();
            }
        }
    }

    private PsiClass createBrowserManifest() {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiDirectoryFactory psiDirectoryFactory = PsiDirectoryFactory.getInstance(project);
        JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
        PsiClass pageClass;
        PsiClass dialogClass;
        PsiClass serverClass;
        PsiClass psiClass = psiFacade.findClass("com.browser.manifest" + ".BrowserManifest", GlobalSearchScope.allScope(project));
        if (psiClass == null) {
            PsiDirectory srcDir = PsiDirectoryFactory.getInstance(project).createDirectory(Objects.requireNonNull(VirtualFileManager.getInstance().findFileByUrl("file://" + project.getBasePath() + "/browserManifest")));
            psiClass = directoryService.createClass(psiDirectoryFactory.createDirectory(FragmentModel.makeDirs(Objects.requireNonNull(srcDir).getVirtualFile(), "com.browser.manifest")), "BrowserManifest");
            PsiModifierList modifierList = psiClass.getModifierList();
            if (modifierList != null) {
                modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                modifierList.setModifierProperty(PsiModifier.FINAL, true);
            }
            pageClass = createChildClass("PageView", PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL);
            dialogClass = createChildClass("PageDialog", PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL);
            serverClass = createChildClass("Server", PsiModifier.PUBLIC, PsiModifier.STATIC, PsiModifier.FINAL);
            if (pageClass != null) psiClass.add(pageClass);
            if (dialogClass != null) psiClass.add(dialogClass);
            if (serverClass != null) psiClass.add(serverClass);
        }
        return psiClass;
    }


    private PsiClass createChildClass(final String name, String... modifiers) {
        try {
            PsiClass psiClass = elementFactory.createClass(name);
            PsiModifierList modifierList = psiClass.getModifierList();
            if (modifierList != null) {
                for (String modifier : modifiers) {
                    modifierList.setModifierProperty(modifier, true);
                }
            }
            return psiClass;
        } catch (Throwable e) {
            Messages.showErrorDialog(e.getMessage(), "error");
        }
        return null;
    }

    @Override
    public void run() {
        buildClass();
    }

    private String createAnnotation() {
        StringBuilder builder = new StringBuilder();
        HashMap<String, String> values = new HashMap<>(5);
        if (!TextUtils.isEmpty(activityName)) values.put("browserClass", activityName + ".class");
        if (!TextUtils.isEmpty(containerId)) values.put("containerId", containerId);
        if (!TextUtils.isEmpty(layoutName)) values.put("viewId", layoutName);
        if (!TextUtils.isEmpty(activityFlags)) values.put("browserFlags", activityFlags);
        if (!TextUtils.isEmpty(permissionList)) values.put("permissions", permissionList);
        if (isDialog) values.put("isDialog", "true");
        String format1 = "%s = \"%s\"";
        String format2 = "%s = %s";
        String format3 = "%s = {%s}";
        builder.append("@com.browser.annotations.PageView(");
        int size = values.size();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String format;
            switch (key) {
                case "containerId":
                    format = format1;
                    break;
                case "browserClass":
                case "isDialog":
                case "browserFlags":
                    format = format2;
                    break;
                case "permissions":
                    format = format3;
                    break;
                default:
                    format = "%s = \"%s\"";
                    break;
            }
            builder.append(String.format(format, entry.getKey(), entry.getValue()));
            size--;
            if (size > 0) builder.append(",\n");
        }
        builder.append(")");
        return builder.toString();
    }

    public void build() {
        WriteCommandAction.runWriteCommandAction(project, this);
    }

    public static VirtualFile makeDirs(VirtualFile virtualFile, String path) {
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
