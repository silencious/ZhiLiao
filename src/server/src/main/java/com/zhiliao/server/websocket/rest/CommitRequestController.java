package com.zhiliao.server.websocket.rest;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.exception.ErrorResponseException;
import com.zhiliao.server.service.recommend.CustomDataModel;
import com.zhiliao.server.service.recommend.DataModel;
import com.zhiliao.server.websocket.MessageSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zhiliao.message.server.Push;
import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.Commit;
import com.zhiliao.server.model.Forward;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.rest.CommitModel;
import com.zhiliao.server.model.rest.ForwardModel;
import com.zhiliao.server.service.BranchService;
import com.zhiliao.server.service.CommitService;
import com.zhiliao.server.service.SubscriptionService;

@Controller
@RequestMapping({
		CommitModel.class,
		ForwardModel.class})
public class CommitRequestController {
	private static final Logger logger = LoggerFactory.getLogger(CommitRequestController.class);
	
	@Autowired
	private SubscriptionService subscriptionService;
	
	@Autowired
	private CommitService commitService;

	@Autowired
	private BranchService branchService;

	@Autowired
	private CustomDataModel dataModel;

	public CommitModel get(Long id) throws ErrorResponseException {
		return convert(commitService.getCommit(id), null);
	}

	public long post(CommitModel model, User user, MessageSocket socket) throws ErrorResponseException {
		Commit commit = convert(model, user);
		Long branchId = model.getBranch();
		if (branchId == null)
			throw new ErrorResponseException(ErrorCode.BadRequest);
		Branch branch;

		if ((branch = commitService.newCommit(branchId, commit)) == null)
			throw new ErrorResponseException(ErrorCode.BadRequest);
		try {
			dataModel.add(user.getId(), branchId, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}


		Iterator<Branch> i = branchService.findFollowers(branch);
		while (i.hasNext()) {
			branchId = i.next().getId();
			//forward to all clients subscribing this branch
			Iterator<Object> j = subscriptionService.list(branchId).iterator();
			while (true) {
				try {
					MessageSocket tosend = (MessageSocket) j.next();
					Push push = new Push(convert(commit, branchId));
					if (tosend != socket)
						if (!tosend.send(push)) {
							j.remove();
						}
				} catch (NoSuchElementException e) {
					break;
				}
			}
		}
		return commit.getId();
	}
	
	public Commit convert(CommitModel model, User user) {
		Commit commit;
		if (model instanceof ForwardModel)
			commit = new Forward();
		else 
			commit = new Commit();
		commit.author_id = user.getId();
		commit.setMsg(model.getMsg());
		commit.replied_id = model.getReplied();
		if (model instanceof ForwardModel) {
			ForwardModel model2 = (ForwardModel)model;
			Forward refer = (Forward)commit;
			refer.refered_id = model2.getRefered();
			refer.originBranch_id = model2.getOriginBranch();
		}
		return commit;
	}
	
	public static CommitModel convert(Commit commit, Long branch) {
		CommitModel model;
		if (commit instanceof Forward)
			model = new ForwardModel();
		else
			model = new CommitModel();
		model.setId(commit.getId());
		model.setDate(commit.getDate().getTimeInMillis());
		model.setMsg(commit.getMsg());
		model.setBranch(branch);
		model.setAuthor(commit.author_id);
		model.setReplied(commit.replied_id);
		if (commit instanceof Forward) {
			Forward refer = (Forward)commit;
			ForwardModel model2 = (ForwardModel)model;
			model2.setRefered(refer.refered_id);
			model2.setOriginBranch(refer.originBranch_id);
		}
		return model;
	}

}
