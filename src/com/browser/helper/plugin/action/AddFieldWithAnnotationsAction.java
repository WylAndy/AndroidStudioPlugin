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
import java.util.HashMap;
import java.util.List;

public class AddFieldWithAnnotationsAction extends AnAction implements AddFieldWithAnnotationDialog.DialogListener {
    private final int ADD_BIND_VIEW = 0X01;
    private final int ADD_ON_CLICK = ADD_BIND_VIEW << 1;
    private final int ADD_ON_LONG_CLICK = ADD_ON_CLICK << 1;
    private HashMap<String, Integer> idMap = new HashMap<>(10);
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
        AddFieldWithAnnotationDialog annotationDialog = new AddFieldWithAnnotationDialog().setFieldList(idList).setDialogListener(this);
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

    @Override
    public void onAddBindView(String id) {
        addFlags(id, ADD_BIND_VIEW);
    }

    @Override
    public void onAddOnClick(String id) {
        addFlags(id, ADD_ON_CLICK);
    }

    @Override
    public void onAddOnLongClick(String id) {
        addFlags(id, ADD_ON_LONG_CLICK);
    }

    @Override
    public void onOk() {

    }

    private void addFlags(String id, int flag) {
        if (idMap.containsKey(id)) {
            int flags = idMap.get(id);
            flags |= flag;
            idMap.put(id, flags);
        } else {
            idMap.put(id, flag);
        }
    }
}
