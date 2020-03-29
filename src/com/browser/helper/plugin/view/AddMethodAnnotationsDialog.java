package com.browser.helper.plugin.view;

import com.browser.helper.plugin.action.AnnotationsModel;
import com.browser.helper.plugin.utils.NoticeDialog;
import com.browser.helper.plugin.utils.NoticeModel;
import com.intellij.psi.PsiMethod;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.util.*;

public class AddMethodAnnotationsDialog extends NoticeDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> pageNameValueView;
    private JComboBox<String> implicationValueView;
    private JComboBox<String> threadModeValueView;
    private JTextField priorityValueView;
    private JCheckBox stickyValueView;
    private JLabel tipLabel;
    private JList<String> methodListView;
    private JButton clearMethod;
    private DialogListener dialogListener;
    private HashMap<String, PsiMethod> methodMap;

    public AddMethodAnnotationsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        clearMethod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                methodListView.clearSelection();
            }
        });

        pageNameValueView.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (dialogListener != null) dialogListener.onPageNameChanged((String) e.getItem());
                }
            }
        });

        implicationValueView.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (dialogListener != null) dialogListener.onImplicationChanged((String) e.getItem());
                }
            }
        });

        priorityValueView.setToolTipText("只能输入非负整数！");
        priorityValueView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String text = e.getDocument().getText(0, len);
                    if (dialogListener != null) dialogListener.onPriorityChanged(text);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String text = e.getDocument().getText(0, len);
                    if (dialogListener != null) dialogListener.onPriorityChanged(text);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String text = e.getDocument().getText(0, len);
                    if (dialogListener != null) dialogListener.onPriorityChanged(text);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
//        injectionAnnotationView.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                enableInjection(injectionAnnotationView.isSelected());
//            }
//        });
    }

    private void onOK() {
        // add your code here
        NoticeModel.clear();
        dispose();
        if (dialogListener != null) {
            AnnotationsModel.Builder builder = AnnotationsModel.newBuilder()
                    .setMethodList(getMethodList(methodListView.getSelectedValuesList()))
                    .setPageName((String) pageNameValueView.getSelectedItem())
                    .setServerPackageName((String) implicationValueView.getSelectedItem())
                    .setThreadMode((String) threadModeValueView.getSelectedItem())
                    .setPriority(priorityValueView.getText())
                    .setSticky(stickyValueView.isSelected());
            dialogListener.onOK(builder);
        }
    }

    private List<PsiMethod> getMethodList(List<String> selectedList) {
        ArrayList<PsiMethod> methods = new ArrayList<>(10);
        for (String selected : selectedList) {
            methods.add(methodMap.get(selected));
        }
        return methods;
    }

    private void onCancel() {
        // add your code here if necessary
        NoticeModel.clear();
        dispose();

    }

    private void enablePage(boolean isEnable) {
        pageNameValueView.setEnabled(isEnable);
        if (dialogListener != null) {
            dialogListener.onEnablePage(isEnable);
            if (isEnable) dialogListener.onPageNameChanged((String) pageNameValueView.getSelectedItem());
        }
    }

    public AddMethodAnnotationsDialog setPageNameList(List<String> pageNameList) {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (String name : pageNameList) {
            comboBoxModel.addElement(name);
        }
        pageNameValueView.setModel(comboBoxModel);
        return this;
    }

    public AddMethodAnnotationsDialog setMethodMap(HashMap<String, PsiMethod> methodMap) {
        this.methodMap = methodMap;
        DefaultListModel<String> comboBoxModel = new DefaultListModel<>();
        for (Map.Entry<String, PsiMethod> entry : methodMap.entrySet()) {
            comboBoxModel.addElement(entry.getKey());
        }
        methodListView.setVisibleRowCount(comboBoxModel.size() > 4 ? 5 : comboBoxModel.size());
        methodListView.setModel(comboBoxModel);
        return this;
    }

    private void setPageMethodList(boolean isPage, HashMap<String, PsiMethod> methodMap) {
        DefaultListModel<String> comboBoxModel = new DefaultListModel<>();
        for (Map.Entry<String, PsiMethod> entry : methodMap.entrySet()) {
            PsiMethod method = entry.getValue();
            if (isPage && !Objects.equals("void", Objects.requireNonNull(method.getReturnType()).getCanonicalText())) {
                comboBoxModel.addElement(entry.getKey());
            } else if (!isPage) {
                comboBoxModel.addElement(entry.getKey());
            }
        }
        methodListView.setVisibleRowCount(comboBoxModel.size() > 4 ? 5 : comboBoxModel.size());
        methodListView.updateUI();
        methodListView.setModel(comboBoxModel);
    }

    public AddMethodAnnotationsDialog setServerNameList(List<String> serverNameList) {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (String name : serverNameList) {
            comboBoxModel.addElement(name);
        }
        implicationValueView.setModel(comboBoxModel);
        return this;
    }

    public AddMethodAnnotationsDialog setThreadModeList(List<String> threadModeList) {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (String name : threadModeList) {
            comboBoxModel.addElement(name);
        }
        threadModeValueView.setModel(comboBoxModel);
        return this;
    }

    public AddMethodAnnotationsDialog setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
        if (dialogListener != null) {
            dialogListener.onImplicationChanged((String) implicationValueView.getSelectedItem());
        }
        return this;
    }

    public interface DialogListener {

        void onPriorityChanged(String priority);

        void onPageNameChanged(String packageName);

        void onImplicationChanged(String packageName);

        void onEnablePage(boolean isEnable);

        void onOK(AnnotationsModel.Builder builder);
    }

    @Override
    protected void setEnableOk(boolean isEnable) {
        buttonOK.setEnabled(isEnable);
    }

    @Override
    protected void showNotice(String content) {
        boolean isEmpty = TextUtils.isEmpty(content);
        tipLabel.setText(content);
        tipLabel.setVisible(!isEmpty);
    }

//    public static void main(String[] args) {
//        AddAnnotationsDialog dialog = new AddAnnotationsDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }
}
