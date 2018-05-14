package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 14-05-2018.
 */

public class GroupsPrivate {
    private Integer id;
    private String name;
    private Integer joinedUsersCount;
    private String groupClass;
    private String requestAcceptType;
    private String access;
    private String roomJid;
    private String lastMessage;
    private Boolean joined;
    private Integer offlineMsgCount;

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

    public String getRequestAcceptType() {
        return requestAcceptType;
    }

    public void setRequestAcceptType(String requestAcceptType) {
        this.requestAcceptType = requestAcceptType;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(Boolean joined) {
        this.joined = joined;
    }

    public Integer getOfflineMsgCount() {
        return offlineMsgCount;
    }

    public void setOfflineMsgCount(Integer offlineMsgCount) {
        this.offlineMsgCount = offlineMsgCount;
    }
}
