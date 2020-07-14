package huaiye.com.vim.models.contacts.bean;

import java.io.Serializable;

public class SelectedModeBean implements Serializable {

    public static final int type_user = 0;
    public static final int type_group = 1;
    public static final int type_dept = 2;

    public String strName;
    public int type;
    public String strId;

    public SelectedModeBean(String strName, int type, String strId) {
        this.strName = strName;
        this.type = type;
        this.strId = strId;
    }

    public boolean isUser() {
        return type == type_user;
    }
    public boolean isGroup() {
        return type == type_group;
    }
    public boolean isDept() {
        return type == type_dept;
    }

}
