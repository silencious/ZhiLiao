package com.zhiliao.server.model.relationship;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.model.User;

import javax.persistence.*;

/**
 * Created by riaqn on 15-8-22.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
public class Prefer {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "user_id")
    public Long userid;

    @ManyToOne
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Branch branch;

    @Column(name = "item_id")
    public Long itemid;


    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public float getPrefs() {

        return prefs;
    }

    public void setPrefs(float prefs) {
        this.prefs = prefs;
    }

    private float prefs;
    private long ts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public User getUser() {

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
