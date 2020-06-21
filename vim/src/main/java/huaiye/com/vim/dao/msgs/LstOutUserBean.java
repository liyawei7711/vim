package huaiye.com.vim.dao.msgs;

import java.io.Serializable;

public class LstOutUserBean implements Serializable {
    /**
     * strUserDomainCode : f079595d296b
     * strUserID : f079595d296b611552662
     * strUserName : jy
     */

    public String strUserDomainCode;
    public String strUserID;
    public String strUserName;

    public LstOutUserBean(String strUserDomainCode, String strUserID, String strUserName) {
        this.strUserDomainCode = strUserDomainCode;
        this.strUserID = strUserID;
        this.strUserName = strUserName;
    }

    public String getStrUserDomainCode() {
        return strUserDomainCode;
    }

    public void setStrUserDomainCode(String strUserDomainCode) {
        this.strUserDomainCode = strUserDomainCode;
    }

    public String getStrUserID() {
        return strUserID;
    }

    public void setStrUserID(String strUserID) {
        this.strUserID = strUserID;
    }

    public String getStrUserName() {
        return strUserName;
    }

    public void setStrUserName(String strUserName) {
        this.strUserName = strUserName;
    }
}
