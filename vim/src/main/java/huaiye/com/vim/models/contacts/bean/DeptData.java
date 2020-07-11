package huaiye.com.vim.models.contacts.bean;

import android.text.TextUtils;

import java.io.Serializable;

public class DeptData implements Serializable {
    /**
     * 部门编号
     */
    public String strDepID;
    /**
     * 部门名称
     */
    public String strName;
    public String strDepName;
    /**
     * 部门类型
     */
    public int nDepType;
    /**
     * 上级部门id，根部门为0
     */
    public String strParentID;
    /**
     * 上级部门名称
     */
    public String strParentName;
    /**
     * 部门优先级，优先值值小的排序靠前
     */
    public int nPpriority;
    /**
     * 部门描述
     */
    public String strDesceribe;
    public String strDomainCode;
    public boolean isSelected;

    public DomainInfoList.DomainInfo domainInfo;
    public String getName() {
        return TextUtils.isEmpty(strName) ? strDepName : strName;
    }

    public DeptData() {
    }
    public DeptData(String strDepID, String strName, String strDepName, int nDepType, String strParentID, String strParentName, int nPpriority, String strDesceribe, String strDomainCode, DomainInfoList.DomainInfo domainInfo) {
        this.strDepID = strDepID;
        this.strName = strName;
        this.strDepName = strDepName;
        this.nDepType = nDepType;
        this.strParentID = strParentID;
        this.strParentName = strParentName;
        this.nPpriority = nPpriority;
        this.strDesceribe = strDesceribe;
        this.strDomainCode = strDomainCode;
        this.domainInfo = domainInfo;
    }
}
