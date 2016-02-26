package com.zhiliao.server.websocket.rest;

import com.zhiliao.server.model.rest.Resource;
import org.junit.Test;

import com.zhiliao.message.client.IteratorInitRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class IteratorTest extends RestTest {
	private boolean next(long id, long count) throws ExecutionException, InterruptedException {
		Future<List<Resource>> fut = client.iteratorNext(id, count, false);
		boolean flag = false;
		for (Resource resource : fut.get()) {
			flag = true;
			System.out.println(resource.getId());
		}
		return flag;
	}

	private void all(long id) throws ExecutionException, InterruptedException {
		while (next(id, 4l))
			;
	}

	@Test(timeout=5000)
	public void byBranch() throws Exception {
		IteratorInitRequest.ByBranchIteratorSpecification specification = new IteratorInitRequest.ByBranchIteratorSpecification();
		specification.setBranch(4l);

		client.iteratorInit(8l, specification);
		all(8l);
	}

	@Test(timeout=5000)
	public void byTags() throws InterruptedException, ExecutionException {
		IteratorInitRequest.ByTagIteratorSpecification specification = new IteratorInitRequest.ByTagIteratorSpecification();
		Set<String> tags = new HashSet<>();
		tags.add("linux");
		tags.add("geek");
		specification.setTags(tags);

		client.iteratorInit(1l, specification).get();

		all(1l);
	}

	@Test
	public void byFollower() throws ExecutionException, InterruptedException {
		IteratorInitRequest.ByFollowIteratorSpecification specification = new IteratorInitRequest.ByFollowIteratorSpecification();
		specification.setFollower(4l);
		client.iteratorInit(2l, specification).get();

		all(2l);
	}

	@Test
	public void byFollowed() throws ExecutionException, InterruptedException {
		IteratorInitRequest.ByFollowIteratorSpecification specification = new IteratorInitRequest.ByFollowIteratorSpecification();
		specification.setFollowed(78l);
		client.iteratorInit(3l, specification).get();

		all(3l);
	}

	@Test
	public void byFollow() throws ExecutionException, InterruptedException {
		IteratorInitRequest.ByFollowIteratorSpecification specification = new IteratorInitRequest.ByFollowIteratorSpecification();
		specification.setFollower(4l);
		specification.setFollowed(78l);
		client.iteratorInit(4l, specification).get();

		all(4l);
	}
}
