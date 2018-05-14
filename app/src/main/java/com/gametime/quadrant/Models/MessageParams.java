package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 28-11-2017.
 */

public class MessageParams {
    private String gid;
    private int limit;
    private String stanzid;
    private String uniqueGid;

    public MessageParams(String gid, int limit) {
        this.gid = gid;
        this.limit = limit;
    }

    public MessageParams(String gid, int limit, String stanzid) {
        this.gid = gid;
        this.limit = limit;
        this.stanzid = stanzid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getStanzid() {
        return stanzid;
    }

    public void setStanzid(String stanzid) {
        this.stanzid = stanzid;
    }

    public String getUniqueGid() {
        return uniqueGid;
    }

    public void setUniqueGid(String uniqueGid) {
        this.uniqueGid = uniqueGid;
    }
}
