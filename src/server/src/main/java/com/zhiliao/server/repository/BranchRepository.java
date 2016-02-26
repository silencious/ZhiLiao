package com.zhiliao.server.repository;

import com.zhiliao.server.model.Branch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by riaqn on 15-8-23.
 */
public interface BranchRepository extends CrudRepository<Branch, Long> {
    Slice<Branch> findAll(Pageable pageable);

    @Query(value = "select b from Branch b join b.tags tags where tags in :tags group by b.id order by count(tags) DESC")
    Slice<Branch> findByTags(@Param("tags") Set<String> tags, Pageable pageable);
}
