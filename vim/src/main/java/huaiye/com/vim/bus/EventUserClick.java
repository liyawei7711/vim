package huaiye.com.vim.bus;

import huaiye.com.vim.dao.msgs.User;

public class EventUserClick {
    public User user;

    public EventUserClick(User user) {
        this.user = user;
    }
}
