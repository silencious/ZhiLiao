package com.zhiliao.server.websocket.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.zhiliao.server.model.rest.BranchModel;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BranchTest extends RestTest {
	private long head = 0l;
	@Test
	public void post() throws InterruptedException, IOException, ExecutionException {
		BranchModel model = new BranchModel();
		model.setHead(null);
		Set<String> tags = new HashSet<>();
		tags.add("linux");
		model.setTags(tags);

		Future<Long> future = client.post(model);
		future.get();
	}
	@Test(timeout=5000)
	public void list() throws InterruptedException, ExecutionException {
		BranchModel model = new BranchModel();

		Future<List<BranchModel>> future = client.list(model);
		future.get();
	}
}
