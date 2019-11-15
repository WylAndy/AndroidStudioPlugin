package com.browser.helper.plugin.view;

import com.browser.helper.plugin.action.FragmentModel;
import com.intellij.openapi.ui.Messages;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.util.List;

public class NewPageFragmentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> pagePackageNameView;
    private JTextField pageClassNameView;
    private JComboBox<String> activityNameView;
    private JComboBox<String> containerIds;
    private JList<String> intentFlags;
    private JButton clearButton;
    private JTextField pageFragmentId;
    private JTextField containerIdView;
    private JTabbedPane tabbedPane1;
    private JPanel ViewModel;
    private JComboBox<String> viewModelPackageName;
    private JTextField viewModelClassName;
    private JLabel tipLabel;
    private JTextField layoutNameView;
    private JComboBox rootElementView;
    private JTextField serverClassNameView;
    private JComboBox<String> serverPackageNameView;
    private JTextField serverIdView;
    private List<String> intentFlagList;
    private List<String> activityList;
    private List<String> layoutList;

    private OnCreateListener onCreateListener;

    public NewPageFragmentDialog(List<String> intentFlagList, List<String> activityList, List<String> layoutList) {
        this.intentFlagList = intentFlagList;
        this.activityList = activityList;
        this.layoutList = layoutList;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        activityNameView.setEditable(true);

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

        pagePackageNameView.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField jTextField = (JTextField) e.getSource();
                if (onCreateListener != null) onCreateListener.onPackageChanged(jTextField.getText());
            }
        });

        pageFragmentId.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentIdChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentIdChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentIdChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        pageClassNameView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onFragmentNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        viewModelClassName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        viewModelPackageName.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField jTextField = (JTextField) e.getSource();
                if (onCreateListener != null) onCreateListener.onViewModelPackageNameChanged(jTextField.getText());
            }
        });
        containerIdView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onContainerIdSelected(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onContainerIdSelected(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onContainerIdSelected(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (activityList != null && activityList.size() > 0) {
            DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
            for (String name : activityList) {
                listModel.addElement(name);
            }
            activityNameView.setModel(listModel);
        }
        activityNameView.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (onCreateListener != null) {
                    JTextField jTextField = (JTextField) e.getSource();
                    onCreateListener.onActivitySelected(jTextField.getText());
                }
            }
        });

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String intentFlag : intentFlagList) {
            listModel.addElement(intentFlag);
        }
        intentFlags.setModel(listModel);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intentFlags.clearSelection();
            }
        });

        serverIdView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerIdChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerIdChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerIdChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        ComboBoxEditor boxEditor = serverPackageNameView.getEditor();
        boxEditor.getEditorComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                JTextField jTextField = (JTextField) e.getSource();
                if (onCreateListener != null) onCreateListener.onServerPackageChanged(jTextField.getText());
            }
        });
        serverClassNameView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        layoutNameView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onLayoutNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onLayoutNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onLayoutNameChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        DefaultComboBoxModel<String> layoutModel = new DefaultComboBoxModel<>();
        for (String layout : layoutList) {
            layoutModel.addElement(layout);
        }
        rootElementView.setEditable(true);
        rootElementView.setModel(layoutModel);
    }

    private boolean checkInputValid() {
        if (TextUtils.isEmpty(containerIdView.getText())) {
            Messages.showErrorDialog("Container Id is not set to a valid value", "Error");
            return false;
        } else if (TextUtils.isEmpty(pageFragmentId.getText())) {
            Messages.showErrorDialog("PageFragment Id is not set to a valid value", "Error");
            return false;
        } else if (TextUtils.isEmpty(pageClassNameView.getText()) || TextUtils.isEmpty((String) pagePackageNameView.getSelectedItem())) {
            Messages.showErrorDialog("PageFragment Name is not set to a valid class name", "Error");
            return false;
        } else if (TextUtils.isEmpty(((String) activityNameView.getSelectedItem()))) {
            Messages.showErrorDialog("Activity Name is not set to a valid class name", "Error");
            return false;
        }
        return true;
    }

    private void onOK() {
        if (!checkInputValid()) return;
        StringBuilder flagsBuilder = null;
        List<String> flags = intentFlags.getSelectedValuesList();
        int size = flags.size();
        if (size > 0) {
            flagsBuilder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                flagsBuilder.append("android.content.Intent.").append(flags.get(i));
                if (i < size - 1) flagsBuilder.append("|");
            }
        }
        FragmentModel fragmentModel = new FragmentModel();
        fragmentModel.setViewModelPackageName((String) viewModelPackageName.getSelectedItem())
                .setName(pageClassNameView.getText())
                .setPackageName((String) pagePackageNameView.getSelectedItem())
                .setId(pageFragmentId.getText())
                .setContainerId(containerIdView.getText())
                .setActivityName((String) activityNameView.getSelectedItem())
                .setViewModelName(viewModelClassName.getText())
                .setLayoutName(layoutNameView.getText())
                .setLayoutRootElement((String) rootElementView.getSelectedItem())
                .setServerClassName(serverClassNameView.getText())
                .setServerId(serverIdView.getText())
                .setServerPackageName((String) serverPackageNameView.getSelectedItem())
                .setActivityFlags(flagsBuilder == null ? "" : flagsBuilder.toString());
        // add your code here
        if (onCreateListener != null) onCreateListener.onOK(fragmentModel);
        clear();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (onCreateListener != null) onCreateListener.onCancel();
        clear();
        dispose();
    }

    private void clear() {
        if (activityNameView.getModel().getSize() > 0)
            activityNameView.setSelectedIndex(0);
        containerIdView.setText("");
        pageFragmentId.setText("");
        intentFlags.clearSelection();
        pageClassNameView.setText("");
        if (pagePackageNameView.getModel().getSize() > 0)
            pagePackageNameView.setSelectedIndex(0);
        layoutNameView.setText("");
        if (rootElementView.getModel().getSize() > 0)
            rootElementView.setSelectedIndex(0);
        if (viewModelPackageName.getModel().getSize() > 0)
            viewModelPackageName.setSelectedIndex(0);
        viewModelClassName.setText("");
        serverIdView.setText("");
        if (serverPackageNameView.getModel().getSize() > 0)
            serverPackageNameView.setSelectedIndex(0);
        serverClassNameView.setText("");
    }

//    public static void main(String[] args) {
//        NewPageFragmentDialog dialog = new NewPageFragmentDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public NewPageFragmentDialog setOnCreateListener(OnCreateListener onCreateListener) {
        this.onCreateListener = onCreateListener;
        return this;
    }

    public void showTip(String message) {
        boolean isEmpty = TextUtils.isEmpty(message);
        tipLabel.setText(message);
        tipLabel.setVisible(!isEmpty);
    }

    public void setEnableOk(boolean isEnable) {
        buttonOK.setEnabled(isEnable);
    }

    public void setActivityList(List<String> activityList) {
        if (activityList == null) return;
        this.activityList = activityList;
        DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
        for (String name : activityList) {
            listModel.addElement(name);
        }
        activityNameView.setModel(listModel);
    }

    public void setPackageList(List<String> packageList, String currentPackage) {
        if (packageList == null || packageList.size() == 0) return;
        DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> listModel1 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> listModel2 = new DefaultComboBoxModel<>();

        for (String packageName : packageList) {
            listModel.addElement(packageName);
            listModel1.addElement(packageName);
            listModel2.addElement(packageName);
        }
        pagePackageNameView.setModel(listModel);
        pagePackageNameView.setSelectedIndex(listModel.getIndexOf(currentPackage));
        viewModelPackageName.setModel(listModel1);
        serverPackageNameView.setModel(listModel2);
    }

    public interface OnCreateListener {
        void onPackageChanged(String packageName);

        void onFragmentNameChanged(String fragmentName);

        void onFragmentIdChanged(String fragmentId);

        void onViewModelNameChanged(String viewModelName);

        void onViewModelPackageNameChanged(String viewModelPackageName);

        void onContainerIdSelected(String containerId);

        void onActivitySelected(String activityName);

        void onLayoutNameChanged(String layoutName);

        void onServerPackageChanged(String packageName);

        void onServerNameChanged(String serverName);

        void onServerIdChanged(String serverId);

        void onOK(FragmentModel model);

        void onCancel();
    }
}
