package com.zhiliao.server.service;

import java.util.*;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.exception.ErrorResponseException;
import com.zhiliao.server.model.relationship.Follow;
import com.zhiliao.server.repository.BranchRepository;
import com.zhiliao.server.repository.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;

@Service
public class BranchServiceImpl implements BranchService {
	private static final Logger logger = LoggerFactory.getLogger(BranchServiceImpl.class);

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private FollowRepository followRepository;

	@Override
	public Branch newBranch(Branch branch, User user) throws ErrorResponseException {
		branch.founder_id = user.getId();
		try {
			branch = branchRepository.save(branch);
		} catch (DataIntegrityViolationException e) {
			throw new ErrorResponseException(ErrorCode.BadRequest);
		}
		return branch;
	}

	@Override
	public Branch getBranch(Long id) {
		if (id == null)
			return null;
		return branchRepository.findOne(id);
	}

	@Override
	public Iterator<Branch> findByTags(Set<String> tags) {
		return new SliceIterator<Branch>() {
			@Override
			protected Slice<Branch> load(Pageable pageable) {
				Slice<Branch> slice = branchRepository.findByTags(tags, pageable);
				logger.debug("slice = {}", slice.getContent());
				return slice;
			}
		}.flatten();
	}

	public Iterator<Branch> findFollowers(Branch branch) {
		Queue<Branch> queue = new LinkedList<>();
		queue.add(branch);
		return new Iterator<Branch>() {

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public Branch next() {
				Branch branch1 = queue.remove();
				Iterator<Follow> i = new SliceIterator<Follow>() {
					@Override
					protected Slice<Follow> load(Pageable pageable) {
						return followRepository.findByFollow(null, branch1.getId(), pageable);
					}
				}.flatten();
				while (i.hasNext()) {
					queue.add(i.next().getFollower());
				}
				return branch1;
			}
		};
	}

}
