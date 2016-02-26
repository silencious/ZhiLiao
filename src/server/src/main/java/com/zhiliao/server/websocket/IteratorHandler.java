package com.zhiliao.server.websocket;

import java.util.*;

import com.zhiliao.message.client.IteratorNextRequest;
import com.zhiliao.message.server.ErrorResponse;
import com.zhiliao.message.server.IteratorInitResponse;
import com.zhiliao.message.server.IteratorNextResponse;
import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.relationship.Follow;
import com.zhiliao.server.model.rest.CommitModel;
import com.zhiliao.server.model.rest.Resource;
import com.zhiliao.server.service.BranchService;
import com.zhiliao.server.service.FollowService;
import com.zhiliao.server.websocket.annotation.MessageMapping;
import com.zhiliao.server.websocket.annotation.SocketAttribute;
import com.zhiliao.server.exception.ErrorResponseException;
import com.zhiliao.server.websocket.rest.BranchRequestController;
import com.zhiliao.server.websocket.rest.CommitRequestController;
import com.zhiliao.server.websocket.rest.FollowRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zhiliao.message.client.IteratorInitRequest;
import com.zhiliao.server.model.Commit;
import com.zhiliao.server.service.CommitService;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Controller
public class IteratorHandler {
	private class BranchIterator implements Iterator<Resource> {
		private Iterator<Branch> iterator;
		public BranchIterator(Iterator<Branch> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Resource next() {
			Branch t = iterator.next();
			t = entityManager.merge(t);
			return BranchRequestController.convert(t);
		}
	};
	private static final Logger logger = LoggerFactory.getLogger(IteratorHandler.class);

	@Autowired
	CommitService commitService;

	 @Autowired
	 BranchService branchService;

	@Autowired
	FollowService followService;

	@PersistenceContext
	EntityManager entityManager;

	 @SocketAttribute("iterators")
	 public Map getIterators() {
		 // we limit number of iterators to 16 per websocket
		 //return new LRUMap(16);
		 return new HashMap<>();
	 }

	@MessageMapping(IteratorInitRequest.class)
	@Transactional
	public void init(MessageSocket socket,
			IteratorInitRequest message) {
		logger.debug("init()");
		long mark = message.getMark();
		Iterator<Resource> iterator;
		long id = message.getId();
		IteratorInitRequest.IteratorSpecification specification = message.getSpecification();
		if (specification instanceof IteratorInitRequest.ByBranchIteratorSpecification) {
			long branch = ((IteratorInitRequest.ByBranchIteratorSpecification) specification).getBranch();
			Iterable<Commit> iterable;
			try {
				iterable = commitService.listCommit(branch);
			} catch (ErrorResponseException e) {
				ErrorResponse response = new ErrorResponse(message.getMark(), e.getError());
				socket.send(response);
				return ;
			}
			if (iterable == null) {
				ErrorResponse response = new ErrorResponse(message.getMark(), ErrorCode.NotFound);
				socket.send(response);
				return ;
			}
			iterator = new Iterator<Resource>() {
				Iterator<Commit> iterator = iterable.iterator();

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public CommitModel next() {
					return CommitRequestController.convert(iterator.next(), branch);
				}
			};
		} else if (specification instanceof IteratorInitRequest.ByTagIteratorSpecification) {
			Set<String> tags = ((IteratorInitRequest.ByTagIteratorSpecification) specification).getTags();
			iterator = new BranchIterator(branchService.findByTags(tags));
		} else if (specification instanceof IteratorInitRequest.ByFollowIteratorSpecification) {
			Long follower = ((IteratorInitRequest.ByFollowIteratorSpecification) specification).getFollower();
			Long followed = ((IteratorInitRequest.ByFollowIteratorSpecification) specification).getFollowed();
			if (follower != null && followed != null)
				iterator = new Iterator<Resource>() {
					Iterator<Follow> iterator = followService.findByFollow(follower, followed);
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Resource next() {
						return FollowRequestController.convert(iterator.next());
					}
				};
			else
				iterator = new BranchIterator(new Iterator<Branch>() {
				Iterator<Follow> iterator = followService.findByFollow(follower, followed);
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Branch next() {
					Follow follow = iterator.next();
					if (follower == null)
						return follow.getFollower();
					else
						return follow.getFollowed();
				}
			});
		}	else {
			ErrorResponse response = new ErrorResponse(mark, ErrorCode.BadRequest);
			socket.send(response);
			return ;
		}
		Map iterators = (Map) socket.getContext().get("iterators");
		iterators.put(id, iterator);
		IteratorInitResponse response = new IteratorInitResponse(mark);
		socket.send(response);
	}

	 @MessageMapping(IteratorNextRequest.class)
	 @Transactional
	 public void next(MessageSocket socket,
					  IteratorNextRequest message) {
		 long id = message.getId();
		 Map iterators = (Map)socket.getContext().get("iterators");
		 Iterator<Resource> iterator = (Iterator<Resource>) iterators.get(id);
		 if (iterator == null) {
			 ErrorResponse response = new ErrorResponse(message.getMark(), ErrorCode.NotFound);
			 socket.send(response);
			 return ;
		 }

		 List<Resource> list = new ArrayList<>();
		 long count = message.getCount();
		 while (count > 0) {
			 if (iterator.hasNext()) {
				 list.add(iterator.next());
			 }
			 else
				 break;
			 --count;
		 }
		 IteratorNextResponse response = new IteratorNextResponse(message.getMark(), message.isSkip() ? null : list);
		 socket.send(response);
	 }
 }
