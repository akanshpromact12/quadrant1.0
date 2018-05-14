package com.gametime.quadrant.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupInfo {
    private Integer id;
    private String name;
    private String access;
    private Integer invisible;
    private String requestAcceptType;
    private String boundryType;
    private Object circleRadius;
    private String status;
    private String xmppRoomname;
    private Object expireDate;
    private String groupClass;
    private String description;
    private BoundryCenter boundryCenter;
    private List<PolygonPoint> polygonPoints = null;
    private Owner owner;
    private Integer joinedUsersCount;
    private List<Object> adminUsers = null;

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

    public Integer getInvisible() {
        return invisible;
    }

    public void setInvisible(Integer invisible) {
        this.invisible = invisible;
    }

    public String getRequestAcceptType() {
        return requestAcceptType;
    }

    public void setRequestAcceptType(String requestAcceptType) {
        this.requestAcceptType = requestAcceptType;
    }

    public String getBoundryType() {
        return boundryType;
    }

    public void setBoundryType(String boundryType) {
        this.boundryType = boundryType;
    }

    public Object getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(Object circleRadius) {
        this.circleRadius = circleRadius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getXmppRoomname() {
        return xmppRoomname;
    }

    public void setXmppRoomname(String xmppRoomname) {
        this.xmppRoomname = xmppRoomname;
    }

    public Object getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Object expireDate) {
        this.expireDate = expireDate;
    }

    public String getGroupClass() {
        return groupClass;
    }

    public void setGroupClass(String groupClass) {
        this.groupClass = groupClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BoundryCenter getBoundryCenter() {
        return boundryCenter;
    }

    public void setBoundryCenter(BoundryCenter boundryCenter) {
        this.boundryCenter = boundryCenter;
    }

    public List<PolygonPoint> getPolygonPoints() {
        return polygonPoints;
    }

    public void setPolygonPoints(List<PolygonPoint> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Integer getJoinedUsersCount() {
        return joinedUsersCount;
    }

    public void setJoinedUsersCount(Integer joinedUsersCount) {
        this.joinedUsersCount = joinedUsersCount;
    }

    public List<Object> getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(List<Object> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public class BoundryCenter {
        private Double lat;
        @SerializedName("long")
        private Double _long;

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLong() {
            return _long;
        }

        public void setLong(Double _long) {
            this._long = _long;
        }
    }

    public class Owner {
        private Integer id;
        private String email;
        private String nick;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

    }

    public class PolygonPoint {
        private Double lat;
        @SerializedName("long")
        private Double _long;

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLong() {
            return _long;
        }

        public void setLong(Double _long) {
            this._long = _long;
        }
    }
}