package com.gametime.quadrant.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Requests {
    @SerializedName("groups")
    private List<GroupReq> groups = null;

    public List<GroupReq> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupReq> groups) {
        this.groups = groups;
    }

    public class GroupReq {
        private Integer id;
        private String name;
        private List<Request> requests = null;

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

        public List<Request> getRequests() {
            return requests;
        }

        public void setRequests(List<Request> requests) {
            this.requests = requests;
        }

        public class Request {
            private Integer id;
            private String joinedOn;
            private String requestDate;
            private String firstName;
            private String lastName;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public String getJoinedOn() {
                return joinedOn;
            }

            public void setJoinedOn(String joinedOn) {
                this.joinedOn = joinedOn;
            }

            public String getRequestDate() {
                return requestDate;
            }

            public void setRequestDate(String requestDate) {
                this.requestDate = requestDate;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }
        }
    }
}