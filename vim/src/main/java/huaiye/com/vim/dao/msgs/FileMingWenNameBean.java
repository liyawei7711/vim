package huaiye.com.vim.dao.msgs;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * 群组聊天记录
 * Created by LENOVO on 2019/3/28.
 */

@Entity(tableName = "tb_chat_mingwen_file_name")
public class FileMingWenNameBean implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int key;
    @ColumnInfo
    public String httpUrl;
    @ColumnInfo
    public String localFile;
    @ColumnInfo
    public String value1;
    @ColumnInfo
    public String value2;
    @ColumnInfo
    public String value3;
    @ColumnInfo
    public String value4;

    public FileMingWenNameBean(String httpUrl, String localFile) {
        this.httpUrl = httpUrl;
        this.localFile = localFile;
    }

}
