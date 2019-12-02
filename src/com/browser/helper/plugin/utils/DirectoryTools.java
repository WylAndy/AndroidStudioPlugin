package com.browser.helper.plugin.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class DirectoryTools {
    private static JavaDirectoryService directoryService = JavaDirectoryService.getInstance();

    public static PsiDirectory getSourceRoot(@NotNull PsiDirectory directory) {
        PsiDirectory parent = directory;
        while (parent != null) {
            if (directoryService.isSourceRoot(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public static List<PsiDirectory> getSubdirectories(@NotNull PsiDirectory rootDir) {
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

    public static PsiClass findBrowserManifest(PsiDirectory rootDir, Module module) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(module.getProject());
        XmlFile manifest = (XmlFile) rootDir.findFile("AndroidManifest.xml");
        PsiClass psiClass = null;
        if (manifest != null) {
            XmlTag rootTag = manifest.getRootTag();
            String appPackageName = Objects.requireNonNull(rootTag).getAttributeValue("package");
            psiClass = psiFacade.findClass(appPackageName + ".BrowserManifest", GlobalSearchScope.moduleScope(module));
        }
        return psiClass;
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
