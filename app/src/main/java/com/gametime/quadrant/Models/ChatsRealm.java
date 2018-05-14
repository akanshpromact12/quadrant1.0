package com.gametime.quadrant.Models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akansh on 14-05-2018.
 */

public class ChatsRealm extends RealmObject {
    @PrimaryKey
    private String chat_id;
    private String id;
    private String name;
    private Integer joinedUsersCount;
    private String groupClass;
    private String requestAcceptType;
    private String access;
    private String roomJid;
    private String lastMessage;
    private Boolean joined;
    private Integer offlineMsgCount;

    public ChatsRealm() {

    }

    public ChatsRealm(String chat_id, JoinedGroups.Groups groups) {
        this.chat_id = chat_id;
        this.id = groups.getId().toString();
        this.name = groups.getName();
        this.joinedUsersCount = groups.getJoinedUsersCount();
        this.groupClass = groups.getGroupClass();
        this.requestAcceptType = groups.getRequestAcceptType();
        this.access = groups.getAccess();
        this.roomJid = groups.getRoomJid();
        this.lastMessage = groups.getLastMessage();
        this.joined = groups.getJoined();
        this.offlineMsgCount = groups.getOfflineMsgCount();
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
