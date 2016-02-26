package com.zhiliao.server.service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.model.relationship.Follow;
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
import com.zhiliao.server.model.relationship.After;
import com.zhiliao.server.exception.ErrorResponseException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class CommitServiceImpl implements CommitService {
	private static final Logger logger = LoggerFactory.getLogger(CommitServiceImpl.class);
	
	@Autowired
	private CommitRepository commitRepository;
	
	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private FollowRepository followRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Transactional
	public Branch newCommit0(long branchId, Commit commit) throws ErrorResponseException {
		Branch branch = branchRepository.findOne(branchId);
		if (branch == null) {
			throw new ErrorResponseException(ErrorCode.BadRequest);
		}
		if (branch.getHead() != null) {
			After after = new After();
			after.setBound(0);
			after.setChild(commit);
			after.setParent(branch.getHead());

			List<After> afterList = new ArrayList<>();
			afterList.add(after);
			commit.setParents(afterList);
		}

		commit = commitRepository.save(commit);
		
		branch.head_id = commit.getId();
		return branchRepository.save(branch);
	}

	@Override
	public Branch newCommit(long branchId, Commit commit) throws ErrorResponseException {
		try {
			return newCommit0(branchId, commit);
		} catch (DataIntegrityViolationException e) {
			throw new ErrorResponseException(ErrorCode.BadRequest);
		}
	}

	private static <T> T uniqueRemove(PriorityQueue<T> queue) {
		T t = queue.remove();
		Comparator comparator = queue.comparator();

		T t0;
		while ((t0 =  queue.peek()) != null) {
			if (comparator.compare(t0, t) == 0) {
				queue.remove();
			} else
				break;
		}
		return t;
	}
	
	@Override
	@Transactional
	public Iterable<Commit> listCommit(long branchId) throws ErrorResponseException {
		Branch b = branchRepository.findOne(branchId);
		if (b == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		return new Iterable<Commit>() {
			@Override
			public Iterator<Commit> iterator() {
				PriorityQueue<Map.Entry<Long, Commit>> heap = new PriorityQueue<>(1, new Comparator<Map.Entry<Long, Commit>>() {
					@Override
					public int compare(Map.Entry<Long, Commit> t0, Map.Entry<Long, Commit> t1) {
						return t0.getValue().compareTo(t1.getValue());
					}
				});
				Queue<Map.Entry<Long, Branch>> queue = new ConcurrentLinkedQueue<>();
				Set<Branch> set = new HashSet<>();
				queue.add(new AbstractMap.SimpleEntry<Long, Branch>(0l, b));
				set.add(b);
				while (!queue.isEmpty()) {
					Map.Entry<Long, Branch> entry = queue.remove();
					Long bound = entry.getKey();
					Branch branch = entry.getValue();
					Commit head = branch.getHead();
					if (head != null && head.getId() > bound)
						heap.add(new AbstractMap.SimpleEntry<Long, Commit>(bound, head));
					Iterator<Follow> i = new SliceIterator<Follow>() {
						@Override
						protected Slice<Follow> load(Pageable pageable) {
							return followRepository.findByFollow(branch.getId(), null, pageable);
						}
					}.flatten();
					while (i.hasNext()) {
						Follow follow = i.next();
						Branch followed = follow.getFollowed();
						if (!set.contains(followed)) {
							queue.add(new AbstractMap.SimpleEntry<Long, Branch>(Math.min(bound, follow.getBound()),
									follow.getFollowed()));
							set.add(follow.getFollowed());
						}
					}
				}

				return new Iterator<Commit>() {

					@Override
					public boolean hasNext() {
						return !heap.isEmpty();
					}

					@Override
					public Commit next() {
						Map.Entry<Long, Commit> entry = uniqueRemove(heap);
						Long bound = entry.getKey();
						Commit commit = entry.getValue();
						commit = entityManager.merge(commit);
						for (After after : commit.getParents()) {
							Long bound0 = Math.max(bound, after.getBound());
							if (after.getParent().getId() > bound0) {
								logger.debug("{} - {} -> {}", after.getChild().getId(), after.getId(), after.getParent().getId());
								heap.add(new AbstractMap.SimpleEntry<Long, Commit>(bound0, after.getParent()));
							}
						}
						return commit;
					}
				};
			}
		};
	}

	@Override
	public Commit getCommit(Long id) throws ErrorResponseException {
		if (id == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		Commit commit = commitRepository.findOne(id);
		if (commit == null)
			throw new ErrorResponseException(ErrorCode.NotFound);
		return commit;
	}
}
