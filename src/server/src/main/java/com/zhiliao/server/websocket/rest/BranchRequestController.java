package com.zhiliao.server.websocket.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.exception.ErrorResponseException;
import com.zhiliao.server.service.recommend.CustomDataModel;
import com.zhiliao.server.service.recommend.DataModel;
import com.zhiliao.server.service.recommend.UserNeighbor;
import com.zhiliao.server.service.recommend.UserRecommender;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.Commit;
import com.zhiliao.server.model.User;
import com.zhiliao.server.model.rest.BranchModel;
import com.zhiliao.server.service.BranchService;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping(BranchModel.class)
public class BranchRequestController {
	private static final Logger logger = LoggerFactory.getLogger(BranchRequestController.class);

	@Autowired
	BranchService branchService;

	@Autowired
	UserRecommender recommender;

	@Autowired
	UserNeighbor neighborhood;

	@Autowired
	CustomDataModel dataModel;

	@Autowired
	com.zhiliao.server.service.recommend.UserSimilarity similarity;

	public long post(BranchModel model, User user) throws ErrorResponseException {
		Branch branch = branchService.newBranch(convert(model), user);

		if (branch == null)
			throw new ErrorResponseException(ErrorCode.BadRequest);
		else {
			return branch.getId();
		}
	}

	@Transactional
	public BranchModel get(Long id, User user) {
		Branch branch;

		branch = branchService.getBranch(id);

		return convert(branch);
	}

	@Transactional
	public Iterable<BranchModel> list(BranchModel model, User user) throws ErrorResponseException {
		for (long userId : dataModel.getUsers())
			logger.debug("similarity[{}][{}] = {}", user.getId(), userId, similarity.get(user.getId(), userId));
		logger.debug("neighbor[{}] = {}", user.getId(), neighborhood.get(user.getId()));
		final Iterable<UserRecommender.RecommendEntity> iterable = recommender.recommend(user.getId(), 5);
		List<BranchModel> list = new ArrayList<>();
		for (UserRecommender.RecommendEntity item : iterable) {
				list.add(convert(branchService.getBranch(item.itemId)));
		}
		return list;
	}

	public static BranchModel convert(Branch branch) {
		BranchModel model = new BranchModel();
		model.setId(branch.getId());
		if (branch.getTags() != null)
			model.setTags(branch.getTags());
		model.setHead(branch.head_id);
		model.setTags(branch.getTags());
		model.getTags().size();
		model.setFounder(branch.founder_id);
		return model;
	}

	public static Branch convert(BranchModel model) {
		Branch branch = new Branch();
		branch.head_id = model.getHead();
		branch.setTags(model.getTags());
		branch.setId(model.getId());
		branch.founder_id = model.getFounder();
		return branch;
	}
}
