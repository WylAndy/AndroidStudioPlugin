package com.browser.helper.plugin.action;

import com.browser.helper.plugin.view.AddFieldWithAnnotationDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddFieldWithAnnotationsAction extends AnAction {
    private Module module;
    private XmlFile xmlFile;
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) return;
        XmlTag[] xmlTags = rootTag.getSubTags();
        List<Pair<String, String>> idList = new ArrayList<>(10);
        for (XmlTag tag : xmlTags) {
            String id = tag.getAttributeValue("android:id");
            if (TextUtils.isEmpty(id)) continue;
            id = id.replace("@+id/", "");
            idList.add(new Pair<>(tag.getName(), id));
        }
        AddFieldWithAnnotationDialog annotationDialog = new AddFieldWithAnnotationDialog(idList);
        annotationDialog.pack();
        annotationDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(module.getProject()));
        annotationDialog.setVisible(true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(false);
        module = e.getData(LangDataKeys.MODULE);
        if (module == null) return;
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (TextUtils.isEmpty(selectedText)) return;
        VirtualFile virtualFile = module.getModuleFile();
        if (virtualFile == null) return;
        virtualFile = virtualFile.getParent().findFileByRelativePath(String.format("src/main/res/layout/%s.xml", selectedText));
        if (virtualFile == null) {
            return;
        }
        xmlFile = (XmlFile) PsiManager.getInstance(module.getProject()).findFile(virtualFile);
        if (xmlFile == null) return;
        presentation.setVisible(true);
    }
}
