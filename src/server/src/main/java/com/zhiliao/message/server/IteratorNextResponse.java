package com.zhiliao.message.server;

import com.zhiliao.server.model.rest.Resource;

import java.util.List;

/**
 * Created by riaqn on 15-8-20.
 */
public class IteratorNextResponse extends IteratorResponse {
    private List<Resource> list;

    public List<Resource> getList() {
        return list;
    }

    public void setList(List<Resource> list) {
        this.list = list;
    }

    public IteratorNextResponse(long mark, List<Resource> list) {
        super(mark);
        this.list = list;
    }

    public IteratorNextResponse(){}

    @Override
    public String toString() {
        return "IteratorNextResponse{" +
                "list=" + list +
                "} " + super.toString();
    }
}
