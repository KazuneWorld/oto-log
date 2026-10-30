package com.example.webapp.mapper;

import com.example.webapp.entity.PushSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** プッシュ通知の購読情報のMapper。SQLは PushSubscriptionMapper.xml にある。 */
@Mapper
public interface PushSubscriptionMapper {

    /** 購読情報を登録する */
    void insert(PushSubscription subscription);

    /** endpointが一致する購読情報を削除する(送信先が無効になった時や、再登録の前に使う)。削除した件数を返す */
    int deleteByEndpoint(@Param("endpoint") String endpoint);

    /** endpointが一致する購読情報を取得する */
    PushSubscription findByEndpoint(@Param("endpoint") String endpoint);

    /** 自分の購読情報だけを削除する(通知をオフにした時)。削除した件数を返す */
    int deleteByEndpointAndUserId(@Param("endpoint") String endpoint, @Param("userId") String userId);

    /** ユーザーの購読情報の一覧(登録順) */
    List<PushSubscription> findByUserId(@Param("userId") String userId);

    /** ユーザーの購読情報の件数 */
    int countByUserId(@Param("userId") String userId);
}
