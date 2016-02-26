package com.zhiliao.message.client;

/**
 * Created by riaqn on 15-8-20.
 */
public class IteratorRequest extends Request {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "IteratorRequest{" +
                "id=" + id +
                "} " + super.toString();
    }

    //this is the id of the listCommit
    //should be unique on client-side
    protected long id;

    public IteratorRequest() {
    }

    public IteratorRequest(long mark, long id) {

        super(mark);
        this.id = id;
    }
}
