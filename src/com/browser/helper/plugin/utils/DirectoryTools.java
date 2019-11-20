package com.browser.helper.plugin.utils;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
}
