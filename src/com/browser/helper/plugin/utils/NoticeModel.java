package com.browser.helper.plugin.utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

public final class NoticeModel {
    static final PriorityQueue<NoticeModel> noticeStack = new PriorityQueue<>(10, new Comparator<NoticeModel>() {
        @Override
        public int compare(NoticeModel o1, NoticeModel o2) {
            return Integer.compare(o2.priority, o1.priority);
        }
    });
    private static int basePriority = 1;
    public final String name;
    final String content;
    private int priority = 0;

    public NoticeModel(String name, String content) {
        this.name = name;
        this.content = content;
        priority = basePriority++;
    }

    public static void update(NoticeModel noticeModel) {
        if (noticeStack.contains(noticeModel)) {
            noticeStack.remove(noticeModel);
            noticeStack.add(noticeModel);
        }
    }

    public static void clear() {
        noticeStack.clear();
        basePriority = 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NoticeModel) {
            NoticeModel model = (NoticeModel) obj;
            return Objects.equals(name, model.name);
        } else {
            return false;
        }
    }
}
