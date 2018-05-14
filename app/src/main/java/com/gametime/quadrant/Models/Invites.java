package com.gametime.quadrant.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Invites {
    @SerializedName("groups")
    private List<GroupInv> groups = null;

    public List<GroupInv> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupInv> groups) {
        this.groups = groups;
    }

    public class GroupInv {
        private Integer id;
        private String name;
        private String description;
        private String password;
        private String access;
        private String requestAcceptType;
        private Request request;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAccess() {
            return access;
        }

        public void setAccess(String access) {
            this.access = access;
        }

        public String getRequestAcceptType() {
            return requestAcceptType;
        }

        public void setRequestAcceptType(String requestAcceptType) {
            this.requestAcceptType = requestAcceptType;
        }

        public Request getRequest() {
            return request;
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        public class Request {
            private Integer id;
            private InvitationIime invitationIime;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public InvitationIime getInvitationIime() {
                return invitationIime;
            }

            public void setInvitationIime(InvitationIime invitationIime) {
                this.invitationIime = invitationIime;
            }

            public class InvitationIime {
                private String date;
                private Integer timezoneType;
                private String timezone;

                public String getDate() {
                    return date;
                }

                public void setDate(String date) {
                    this.date = date;
                }

                public Integer getTimezoneType() {
                    return timezoneType;
                }

                public void setTimezoneType(Integer timezoneType) {
                    this.timezoneType = timezoneType;
                }

                public String getTimezone() {
                    return timezone;
                }

                public void setTimezone(String timezone) {
                    this.timezone = timezone;
                }
            }
        }
    }
}