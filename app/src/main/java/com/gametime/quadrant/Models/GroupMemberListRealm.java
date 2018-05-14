package com.gametime.quadrant.Models;

import io.realm.RealmModel;

public class GroupMemberListRealm implements RealmModel {
    private Integer id;
    private String userType;
    private String firstName;
    private String lastName;
    private String role;
    private String nick;

    public GroupMemberListRealm(Integer id, String userType, String firstName, String lastName, String role, String nick) {
        this.id = id;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.nick = nick;
    }

    public Integer getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public String getNick() {
        return nick;
    }
}
