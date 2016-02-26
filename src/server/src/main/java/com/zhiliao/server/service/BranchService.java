package com.zhiliao.server.service;

import com.zhiliao.server.exception.ErrorResponseException;
import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface BranchService {
	Branch newBranch(Branch branch, User user) throws ErrorResponseException;
	Branch getBranch(Long id);
	Iterator<Branch> findByTags(Set<String> tags);
	Iterator<Branch> findFollowers(Branch branch);
}
