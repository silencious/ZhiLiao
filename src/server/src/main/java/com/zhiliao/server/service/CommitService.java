package com.zhiliao.server.service;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.Commit;
import com.zhiliao.server.exception.ErrorResponseException;

public interface CommitService {
	public Commit getCommit(Long id) throws ErrorResponseException;
	public Branch newCommit(long branchId, Commit commit) throws ErrorResponseException;
	public Iterable<Commit> listCommit(long branchId) throws ErrorResponseException;
}
