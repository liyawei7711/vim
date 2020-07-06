package huaiye.com.vim.dao.msgs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * 群组聊天dao
 * Created by LENOVO on 2019/3/28.
 */
@Dao
public interface ChatGroupMsgDao {

    //返回Long数据表示，插入条目的主键值（uid）
    @Insert(onConflict = REPLACE)
    Long insert(ChatGroupMsgBean msg);

    @Insert(onConflict = REPLACE)
    void insertAll(ChatGroupMsgBean... msg);

    @Query("select * from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId  order by time desc limit 1")
    ChatGroupMsgBean queryLastItem(String strGroupID, String userId);

    @Query("select * from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId order by time")
    LiveData<List<ChatGroupMsgBean>> queryAll(String strGroupID, String userId);

    @Query("select *from (select * from tb_chat_group_msg where groupID=:strGroupID and extend1=:userId order by time desc limit :index,:limit) order by time")
    List<ChatGroupMsgBean> queryPagingItemWithoutLive(String strGroupID, String userId, int index, int limit);

    @Query("select * from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId order by time")
    List<ChatGroupMsgBean> queryAllGroupChat(String strGroupID, String userId);

    @Query("select count(*) from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId AND read=0")
    int getGroupUnreadNum(String strGroupID, String userId);

    @Query("select * from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId order by time desc limit 1")
    ChatGroupMsgBean getGroupNewestMsg(String strGroupID, String userId);

    @Query("update tb_chat_group_msg set read= 1 where groupID=:strGroupID and extend1 =:userId AND type!=9996 AND bFire!=1")
    void updateAllRead(String strGroupID, String userId);

    @Query("update tb_chat_group_msg set read= 1 where sessionID=:sessionID and extend1 =:userId AND type!=9996 AND bFire!=1")
    void updateSessionIDRead(String sessionID, String userId);

    @Query("update tb_chat_group_msg set read= 1 where groupID=:strGroupID and extend1 =:userId AND msgID=:msgID")
    void updateReadWithMsgID(String strGroupID, String msgID, String userId);

    @Query("update tb_chat_group_msg set localFilePath=:localFilePath where groupID=:strGroupID and extend1 =:userId AND id=:messageId")
    void updateDownloadState(String strGroupID, String localFilePath, long messageId, String userId);

    @Query("delete from tb_chat_group_msg where groupID=:strGroupID and extend1 =:userId")
    void deleteGroup(String strGroupID, String userId);

    @Query("delete from tb_chat_group_msg where sessionID=:sessionID and extend1 =:userId")
    void deleteBySessionID(String sessionID, String userId);

    @Query("delete from tb_chat_group_msg where sessionID=:sessionID and extend1 =:userId AND id=:id")
    void deleteBySessionIDAndId(String sessionID, long id, String userId);

    @Query("delete from tb_chat_group_msg where sessionID=:sessionID and extend1 =:userId AND msgID=:msgID")
    void deletByMsgID(String sessionID, String msgID, String userId);


    @Query("delete from tb_chat_group_msg where extend1 =:userId")
    void clearData(String userId);
}
