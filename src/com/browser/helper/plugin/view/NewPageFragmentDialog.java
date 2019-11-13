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
    private JTextField pagePackageNameView;
    private JTextField pageClassNameView;
    private JComboBox<String> activityNameView;
    private JComboBox<String> containerIds;
    private JList<String> intentFlags;
    private JButton clearButton;
    private JTextField pageFragmentId;
    private JTextField containerIdView;
    private JTabbedPane tabbedPane1;
    private JPanel ViewModel;
    private JTextField viewModelPackageName;
    private JTextField viewModelClassName;
    private JLabel tipLabel;
    private JTextField layoutNameView;
    private JComboBox rootElementView;
    private JTextField serverClassNameView;
    private JTextField serverPackageNameView;
    private JTextField serverIdView;
    private List<String> intentFlagList;
    private List<String> activityList;
    private List<String> layoutList;

    private OnCreateListener onCreateListener;
    private String selectedActivityName;
    private String selectedContainerId;
    private String packageName;

    public NewPageFragmentDialog(List<String> intentFlagList, List<String> activityList, List<String> layoutList, String packageName) {
        this.intentFlagList = intentFlagList;
        this.activityList = activityList;
        this.layoutList = layoutList;
        this.packageName = packageName;
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

        pagePackageNameView.setText(packageName);
        pagePackageNameView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onPackageChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onPackageChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onPackageChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
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
        viewModelPackageName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelPackageNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelPackageNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                try {
                    String content = e.getDocument().getText(0, len);
                    if (onCreateListener != null) onCreateListener.onViewModelPackageNameChanged(content);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
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
        activityNameView.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();
                if (!item.equals(selectedActivityName)) {
                    selectedActivityName = (String) activityNameView.getSelectedItem();
                    if (onCreateListener != null) onCreateListener.onActivitySelected(selectedActivityName);
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

        serverPackageNameView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int len = e.getDocument().getLength();
                if (onCreateListener != null) {
                    try {
                        onCreateListener.onServerPackageChanged(e.getDocument().getText(0, len));
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
                        onCreateListener.onServerPackageChanged(e.getDocument().getText(0, len));
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
                        onCreateListener.onServerPackageChanged(e.getDocument().getText(0, len));
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
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
        } else if (TextUtils.isEmpty(pageClassNameView.getText()) || TextUtils.isEmpty(pagePackageNameView.getText())) {
            Messages.showErrorDialog("PageFragment Name is not set to a valid class name", "Error");
            return false;
        } else if (TextUtils.isEmpty(((String)activityNameView.getSelectedItem()))) {
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
        fragmentModel.setViewModelPackageName(viewModelPackageName.getText())
                .setName(pageClassNameView.getText())
                .setPackageName(pagePackageNameView.getText())
                .setId(pageFragmentId.getText())
                .setContainerId(containerIdView.getText())
                .setActivityName((String) activityNameView.getSelectedItem())
                .setViewModelName(viewModelClassName.getText())
                .setLayoutName(layoutNameView.getText())
                .setLayoutRootElement((String) rootElementView.getSelectedItem())
                .setServerClassName(serverClassNameView.getText())
                .setServerId(serverIdView.getText())
                .setServerPackageName(serverPackageNameView.getText())
                .setActivityFlags(flagsBuilder == null ? "" : flagsBuilder.toString());
        // add your code here
        if (onCreateListener != null) onCreateListener.onOK(fragmentModel);
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (onCreateListener != null) onCreateListener.onCancel();
        activityNameView.setSelectedIndex(0);
        containerIdView.setText("");
        pageFragmentId.setText("");
        intentFlags.clearSelection();
        pageClassNameView.setText("");
        pagePackageNameView.setText(packageName);
        layoutNameView.setText("");
        rootElementView.setSelectedIndex(0);
        viewModelPackageName.setText("");
        viewModelClassName.setText("");
        serverIdView.setText("");
        serverPackageNameView.setText("");
        serverClassNameView.setText("");
        dispose();
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

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        pagePackageNameView.setText(packageName);
    }

    public void setActivityList(List<String> activityList) {
        if (activityList == null) return;
        this.activityList = activityList;
        DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
        for (String name : activityList) {
            listModel.addElement(name);
        }
        activityNameView.setModel(listModel);
        activityNameView.updateUI();
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
