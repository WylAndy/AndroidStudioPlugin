package com.browser.helper.plugin.view;

import com.browser.helper.plugin.utils.NoticeDialog;
import com.intellij.openapi.util.Pair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AddFieldWithAnnotationDialog extends NoticeDialog implements ActionListener {
    private JPanel contentPane;
    private JList fieldListView;
    private JButton clearFields;
    private JCheckBox checkBindView;
    private JCheckBox checkOnClick;
    private JCheckBox checkOnLongClick;
    private JButton buttonCancel;
    private JButton buttonOk;
    private List<Pair<String, String>> idList;
    public AddFieldWithAnnotationDialog(List<Pair<String, String>> idList) {
        this.idList = idList;
        setContentPane(contentPane);
        clearFields.addActionListener(this);
        buttonOk.addActionListener(this);
        buttonCancel.addActionListener(this);
        setFieldList(idList);
    }

    @Override
    protected void setEnableOk(boolean isEnable) {
        buttonOk.setEnabled(isEnable);
    }

    @Override
    protected void showNotice(String content) {

    }

    public AddFieldWithAnnotationDialog setFieldList(List<Pair<String, String>> idList) {
        DefaultListModel<String> comboBoxModel = new DefaultListModel<>();
        for (Pair<String, String> pair : idList) {
            comboBoxModel.addElement(pair.first + " : " + pair.second);
        }
        fieldListView.setVisibleRowCount(comboBoxModel.size() > 4 ? 5 : comboBoxModel.size());
        fieldListView.setModel(comboBoxModel);
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(clearFields)) {
            fieldListView.clearSelection();
        } else if (e.getSource().equals(buttonOk) || e.getSource().equals(buttonCancel)) {
            dispose();
        } else {

        }
    }
}
