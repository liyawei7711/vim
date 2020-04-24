package huaiye.com.vim.dao.msgs;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface FileMingWenNameDao {

    //返回Long数据表示，插入条目的主键值
    @Insert(onConflict = REPLACE)
    Long insert(FileMingWenNameBean bean);

    @Insert(onConflict = REPLACE)
    void insertAll(FileMingWenNameBean... beans);

    @Insert(onConflict = REPLACE)
    void insertAll(List<FileMingWenNameBean> beans);

    @Query("select * from tb_chat_mingwen_file_name")
    List<FileMingWenNameBean> getFileLocalList();

    @Query("select * from tb_chat_mingwen_file_name where httpUrl=:httpUrl")
    FileMingWenNameBean getFileMingWenInfo(String httpUrl);

    @Update
    void updateFileLocal(FileMingWenNameBean sendMsgUserBean);

    @Delete
    void deleteFromDao(FileMingWenNameBean sendMsgUserBean);


    @Query("delete from tb_chat_mingwen_file_name")
    void clearData();
    
}
