package huaiye.com.vim.bus;

import java.util.ArrayList;

import huaiye.com.vim.models.meet.bean.FileBean;

public class StartTransFileBean {

    public ArrayList<FileBean> allData = new ArrayList<>();

    public StartTransFileBean(ArrayList<FileBean> allData) {
        this.allData.clear();
        this.allData.addAll(allData);
    }
    public StartTransFileBean(FileBean data) {
        this.allData.clear();
        this.allData.add(data);
    }
}
