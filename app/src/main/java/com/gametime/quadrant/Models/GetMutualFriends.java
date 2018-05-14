package com.gametime.quadrant.Models;

public class GetMutualFriends {
    private String fbAccessToken;
    private String friendExternalId;

    public GetMutualFriends(String fbAccessToken, String friendExternalId) {
        this.fbAccessToken = fbAccessToken;
        this.friendExternalId = friendExternalId;
    }

    public String getFbAccessToken() {
        return fbAccessToken;
    }

    public void setFbAccessToken(String fbAccessToken) {
        this.fbAccessToken = fbAccessToken;
    }

    public String getFriendExternalId() {
        return friendExternalId;
    }

    public void setFriendExternalId(String friendExternalId) {
        this.friendExternalId = friendExternalId;
    }
}