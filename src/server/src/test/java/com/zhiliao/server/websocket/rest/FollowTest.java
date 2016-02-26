package com.zhiliao.server.websocket.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.zhiliao.server.exception.ErrorCode;
import org.junit.Test;

import com.zhiliao.client.websocket.rest.RestClient.Callback;
import com.zhiliao.server.model.rest.BranchModel;
import com.zhiliao.server.model.rest.FollowModel;
import com.zhiliao.server.model.rest.UserModel;

public class FollowTest extends RestTest {
    @Test(timeout = 5000)
    public void post_and_delete() throws InterruptedException, ExecutionException {
        FollowModel model = new FollowModel();
        model.setFollower(26l);
        model.setFollowed(111l);

        Future<Long> future = client.post(model);
        Long id = future.get();

        model.setId(id);
        Future fut  = client.delete(model);
        fut.get();
    }

    @Test(timeout = 5000)
    public void list_one() throws InterruptedException, ExecutionException {
        FollowModel model = new FollowModel();
        model.setFollowed(3l);

        Future<List<FollowModel>> fut = client.list(model);
        fut.get();
    }


}
