package com.browser.helper.plugin.utils;

import org.apache.http.util.TextUtils;

import javax.swing.*;

public abstract class NoticeDialog extends JDialog {


    public void showWarnTip(NoticeModel noticeModel) {
        if (noticeModel != null) {
            if (TextUtils.isEmpty(noticeModel.content)) NoticeModel.noticeStack.remove(noticeModel);
            else if (!NoticeModel.noticeStack.contains(noticeModel)) NoticeModel.noticeStack.add(noticeModel);
            else NoticeModel.update(noticeModel);
        }
        if (NoticeModel.noticeStack.isEmpty()) {
            setEnableOk(true);
            showNotice("");
        } else {
            NoticeModel notice = NoticeModel.noticeStack.peek();
            setEnableOk(TextUtils.isEmpty(notice.content));
            showNotice(notice.content);
        }
    }

    protected abstract void setEnableOk(boolean isEnable);
    protected abstract void showNotice(String content);

}
