package com.zhiliao.server.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.repository.BranchRepository;
import com.zhiliao.server.repository.CommitRepository;
import com.zhiliao.server.repository.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.Commit;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.relationship.After;
import com.zhiliao.server.model.relationship.Follow;
import com.zhiliao.server.exception.ErrorResponseException;

@Service
public class FollowServiceImpl implements FollowService {
	private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);
	@Autowired
	BranchRepository branchRepository;

	@Autowired
	FollowRepository followRepository;

	@Autowired
	CommitRepository commitRepository;

	@Override
	public long addFollow(Follow follow, User user) throws ErrorResponseException {
		Branch follower = branchRepository.findOne(follow.follower_id);
		if (!follower.founder_id.equals(user.getId()))
			throw new ErrorResponseException(ErrorCode.Forbidden);
		Branch followed = branchRepository.findOne(follow.followed_id);
		follow.setBound(followed.head_id);
		try {
			follow = followRepository.save(follow);
		} catch (DataIntegrityViolationException e) {
			throw new ErrorResponseException(ErrorCode.Conflict);
		}
		return follow.getId();
	}

	@Override
	public Follow getFollow(long id) throws ErrorResponseException {
		Follow follow = followRepository.findOne(id);
		if (follow == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		return follow;
	}

	@Override
	@Transactional
	public void deleteFollow(long id, User user) throws ErrorResponseException {
		Follow follow = followRepository.findOne(id);
		if (follow == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		if (!follow.getFollower().founder_id.equals(user.getId()))
			throw new ErrorResponseException(ErrorCode.Forbidden);

		Branch follower = follow.getFollower();
		Branch followed = follow.getFollowed();
		Commit head = new Commit();
		
		head.setMsg("Unfollow");
		
		List<After> parents = new ArrayList<After>();
		if (follower.getHead() != null)
			parents.add(new After(head, follower.getHead(), 0));
		if (followed.getHead() != null)
			parents.add(new After(head, followed.getHead(), follow.getBound()));
		head.setParents(parents);
		head = commitRepository.save(head);
		
		follower.head_id = head.getId();
		branchRepository.save(follower);
		followRepository.delete(follow);
	}

	@Override
	public Iterator<Follow> findByFollow(Long follower, Long followed) {
		return new SliceIterator<Follow>() {
			@Override
			protected Slice<Follow> load(Pageable pageable) {
				return followRepository.findByFollow(follower, followed, pageable);
			}
		}.flatten();
	}
}
