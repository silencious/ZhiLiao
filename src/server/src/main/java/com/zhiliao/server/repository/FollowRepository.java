package com.zhiliao.server.repository;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.relationship.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by riaqn on 15-8-23.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {
    @Query("select f from Follow f where (:follower is null or f.follower_id = :follower) and " +
            "(:followed is null or f.followed_id = :followed)")
    Slice<Follow> findByFollow(@Param("follower") Long follower_id,
                               @Param("followed") Long followed_id,
                               Pageable pageable);
}
