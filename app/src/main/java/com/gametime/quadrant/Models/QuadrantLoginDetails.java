package com.gametime.quadrant.Models;

public class QuadrantLoginDetails {
    private Boolean success;
    private Integer id;
    private String token;
    private XmppUserDetails xmppUserDetails;
    private String resourceHostDomain;
    private Settings settings;
    private LongLiveToken longLiveToken;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public XmppUserDetails getXmppUserDetails() {
        return xmppUserDetails;
    }

    public void setXmppUserDetails(XmppUserDetails xmppUserDetails) {
        this.xmppUserDetails = xmppUserDetails;
    }

    public String getResourceHostDomain() {
        return resourceHostDomain;
    }

    public void setResourceHostDomain(String resourceHostDomain) {
        this.resourceHostDomain = resourceHostDomain;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public LongLiveToken getLongLiveToken() {
        return longLiveToken;
    }

    public void setLongLiveToken(LongLiveToken longLiveToken) {
        this.longLiveToken = longLiveToken;
    }

    public class XmppUserDetails {
        private String jid;
        private String password;
        private String nick;
        private String userType;

        public String getJid() {
            return jid;
        }

        public void setJid(String jid) {
            this.jid = jid;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }
    }

    public class Settings {
        private Integer groupRangeAccessStatus;
        private Integer groupRangeAccess;

        public Integer getGroupRangeAccessStatus() {
            return groupRangeAccessStatus;
        }

        public void setGroupRangeAccessStatus(Integer groupRangeAccessStatus) {
            this.groupRangeAccessStatus = groupRangeAccessStatus;
        }

        public Integer getGroupRangeAccess() {
            return groupRangeAccess;
        }

        public void setGroupRangeAccess(Integer groupRangeAccess) {
            this.groupRangeAccess = groupRangeAccess;
        }
    }

    public class LongLiveToken {
        private String fbToken;
        private String tokenType;
        private String expiresAt;

        public String getFbToken() {
            return fbToken;
        }

        public void setFbToken(String fbToken) {
            this.fbToken = fbToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}