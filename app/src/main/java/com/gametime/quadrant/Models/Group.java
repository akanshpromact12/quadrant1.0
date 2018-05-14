package com.gametime.quadrant.Models;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Akansh on 14-11-2017.
 */

public class Group implements Serializable, Comparable<Group> {
    private Integer id;
    private String name;
    private String access;
    private String description;
    private String requestAcceptType;
    private Integer joinedUsersCount;
    private String groupClass;
    private Boolean joined;
    private String roomJid;
    private Boolean requested;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestAcceptType() {
        return requestAcceptType;
    }

    public void setRequestAcceptType(String requestAcceptType) {
        this.requestAcceptType = requestAcceptType;
    }

    public Integer getJoinedUsersCount() {
        return joinedUsersCount;
    }

    public void setJoinedUsersCount(Integer joinedUsersCount) {
        this.joinedUsersCount = joinedUsersCount;
    }

    public String getGroupClass() {
        return groupClass;
    }

    public void setGroupClass(String groupClass) {
        this.groupClass = groupClass;
    }

    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(Boolean joined) {
        this.joined = joined;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public Boolean getRequested() {
        return requested;
    }

    public void setRequested(Boolean requested) {
        this.requested = requested;
    }

    @Override
    public int compareTo(@NonNull Group group) {
        if (joinedUsersCount > group.getJoinedUsersCount()) {
            return -1;
        } else if (joinedUsersCount < group.getJoinedUsersCount()) {
            return 1;
        } else {
            return 0;
        }
    }
}
