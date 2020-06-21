package huaiye.com.vim.dao.msgs;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by LENOVO on 2019/4/2.
 */
@Dao
public interface ChangyongLianXiRenDao {
    @Insert(onConflict = REPLACE)
    void insertAll(ChangyongLianXiRenBean... msg);


    @Insert(onConflict = REPLACE)
    void insertAll(List<ChangyongLianXiRenBean> msg);

    @Query("select * from tb_user_changyong_lianxi where (ownerUserId=:ownerUserId AND ownerUserDomain=:ownerUserDomain)")
    List<ChangyongLianXiRenBean> queryAll(String ownerUserId, String ownerUserDomain);

    @Query("delete from tb_user_changyong_lianxi where ownerUserId=:ownerUserId AND ownerUserDomain=:ownerUserDomain")
    void deleteByUser(String ownerUserId, String ownerUserDomain);

    @Query("delete from tb_user_changyong_lianxi where ownerUserId=:ownerUserId AND ownerUserDomain=:ownerUserDomain AND strUserID=:strUserID AND strDomainCode=:strDomainCode")
    void deleteByUser(String ownerUserId, String ownerUserDomain, String strUserID, String strDomainCode);

    @Query("delete from tb_user_changyong_lianxi")
    void clearData();

}
