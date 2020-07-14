package huaiye.com.vim.bus;

import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;

public class EventUserClick {
    public SelectedModeBean user;

    public EventUserClick(SelectedModeBean user) {
        this.user = user;
    }
}
