package com.gametime.quadrant.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CreatedGroups {
    @SerializedName("groups")
    private List<Groupsd> groups = null;

    public List<Groupsd> getGroups() {
        return groups;
    }

    public void setGroups(List<Groupsd> groups) {
        this.groups = groups;
    }

    public class BoundryCenter implements Serializable {

    }

    public class Groupsd implements Serializable {
        private Integer id;
        private String name;
        private String access;
        private Integer invisible;
        private String requestAcceptType;
        private Integer free;
        private String boundryType;
        private Object circleRadius;
        private BoundryCenter boundryCenter;
        private Polygon polygon;
        private String status;
        private String groupClass;
        private String roomJid;
        private Boolean joined;
        private Integer joinedUsersCount;

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

        public Integer getFree() {
            return free;
        }

        public void setFree(Integer free) {
            this.free = free;
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

        public BoundryCenter getBoundryCenter() {
            return boundryCenter;
        }

        public void setBoundryCenter(BoundryCenter boundryCenter) {
            this.boundryCenter = boundryCenter;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        public void setPolygon(Polygon polygon) {
            this.polygon = polygon;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getGroupClass() {
            return groupClass;
        }

        public void setGroupClass(String groupClass) {
            this.groupClass = groupClass;
        }

        public String getRoomJid() {
            return roomJid;
        }

        public void setRoomJid(String roomJid) {
            this.roomJid = roomJid;
        }

        public Boolean getJoined() {
            return joined;
        }

        public void setJoined(Boolean joined) {
            this.joined = joined;
        }

        public Integer getJoinedUsersCount() {
            return joinedUsersCount;
        }

        public void setJoinedUsersCount(Integer joinedUsersCount) {
            this.joinedUsersCount = joinedUsersCount;
        }
    }

    public class Polygon implements Serializable {

    }
}