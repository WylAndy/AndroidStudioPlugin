package com.browser.helper.plugin.view;

import com.browser.helper.plugin.utils.NoticeDialog;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.AddEditRemovePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AddFieldWithAnnotationDialog extends NoticeDialog implements ActionListener {
    private JPanel contentPane;
    private JButton clearFields;
    private JCheckBox checkBindView;
    private JCheckBox checkOnClick;
    private JCheckBox checkOnLongClick;
    private JButton buttonCancel;
    private JButton buttonOk;
    private JTable viewTable;
    private DialogListener dialogListener;
    public AddFieldWithAnnotationDialog() {
        setContentPane(contentPane);
        clearFields.addActionListener(this);
        buttonOk.addActionListener(this);
        buttonCancel.addActionListener(this);
    }

    @Override
    protected void setEnableOk(boolean isEnable) {
        buttonOk.setEnabled(isEnable);
    }

    @Override
    protected void showNotice(String content) {

    }

    public AddFieldWithAnnotationDialog setFieldList(List<Pair<String, String>> idList) {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Type");
        tableModel.addColumn("Id");
        tableModel.setNumRows(idList.size());
        for (int i = 0; i < idList.size(); i ++) {
            Pair<String, String> pair = idList.get(i);
            tableModel.setValueAt(pair.first, i, 0);
            tableModel.setValueAt(pair.second, i, 1);
        }
        viewTable.setModel(tableModel);
        return this;
    }

    public AddFieldWithAnnotationDialog setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(clearFields)) {
            viewTable.clearSelection();
        } else if (source.equals(buttonOk) || source.equals(buttonCancel)) {
            dispose();
            if (source.equals(buttonOk)) {
                int[] rows = viewTable.getSelectedRows();
                if (rows.length > 0 && dialogListener != null) {
                    for (int i = 0; i < rows.length; i++) {
                        String id = (String) viewTable.getValueAt(i, 1);
                        if (checkBindView.isSelected()) {
                            dialogListener.onAddBindView(id);
                        }
                        if (checkOnClick.isSelected()) {
                            dialogListener.onAddOnClick(id);
                        }
                        if (checkOnLongClick.isSelected()) {
                            dialogListener.onAddOnLongClick(id);
                        }
                    }
                    dialogListener.onOk();
                }
            }
        } else {

        }
    }

    public interface DialogListener {
        void onAddBindView(String id);
        void onAddOnClick(String id);
        void onAddOnLongClick(String id);
        void onOk();
    }
}
