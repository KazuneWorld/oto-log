package com.example.webapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.Friend;
import com.example.webapp.entity.User;

@Mapper
public interface FriendMapper {
    
    // フレンドコードでユーザーを検索する
    User findUserByFriendCode(@Param("friendCode") String friendCode);

    // ユーザーにフレンド申請を送る
    int sendFriendRequest(@Param("requesterId") String requesterId,
                          @Param("receiverId") String receiverId);

    //自分のフレンド一覧を取得する
    List<Friend> findFriendsByUserId(@Param("userId") String userId);

    //自分が送った申請(相手の承認待ち)の一覧を取得する
    List<Friend> findSentFriendRequests(@Param("userId") String userId);

    //自分に届いた申請(自分が承認するか決める)の一覧を取得する
    List<Friend> findReceivedFriendRequests(@Param("userId") String userId);

    //フレンド申請の承認(自分が受信側の申請中レコードのみ)
    int acceptFriendRequest(@Param("userId") String userId,
                            @Param("friendRelationId") int friendRelationId);

    //申請の取り下げ(自分が送った申請中レコードのみ)
    int withdrawFriendRequest(@Param("userId") String userId,
                              @Param("friendRelationId") int friendRelationId);

    //申請の却下(自分に届いた申請中レコードのみ)
    int rejectFriendRequest(@Param("userId") String userId,
                            @Param("friendRelationId") int friendRelationId);

    //フレンドの削除(自分が当事者のフレンド関係のみ)
    int deleteFriend(@Param("userId") String userId,
                     @Param("friendRelationId") int friendRelationId);
}
