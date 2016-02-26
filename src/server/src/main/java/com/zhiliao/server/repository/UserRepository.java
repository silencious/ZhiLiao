package com.zhiliao.server.repository;

import com.zhiliao.server.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by riaqn on 15-8-23.
 */
public interface UserRepository extends CrudRepository<User, Long> {
    public Slice<User> findAll(Pageable pageable);
    public User findByUsername(String username);
}
