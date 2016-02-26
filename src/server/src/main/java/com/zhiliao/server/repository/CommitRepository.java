package com.zhiliao.server.repository;

import com.zhiliao.server.model.Commit;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by riaqn on 15-8-23.
 */
public interface CommitRepository extends CrudRepository<Commit, Long> {
}
