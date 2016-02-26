package com.zhiliao.server.repository;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.relationship.Prefer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

/**
 * Created by riaqn on 15-8-23.
 */
public interface PreferRepository extends CrudRepository<Prefer, Long> {
    Slice<Prefer> findByUserid(Long user_id, Pageable pageable);
    @Query("select p from Prefer p where p.userid = :user_id order by p.itemid")
    Slice<Prefer> findByUseridOrderByItemid(@Param("user_id")Long user_id, Pageable pageable);
    @Query("select p from Prefer p where p.userid = :user_id and p.itemid = :item_id")
    Prefer findByUseridAndItemid(@Param("user_id") Long user_id, @Param("item_id") Long item_id);
    @Query("select p.itemid from Prefer p " +
            "where not exists (select p0 from Prefer p0 where p0.userid = :user_id and p0.itemid = p.itemid)" +
            "group by p.itemid order by count(p.userid) ")
    Slice<Long> findByRandom(@Param("user_id") Long user_id, Pageable pageable);
}
