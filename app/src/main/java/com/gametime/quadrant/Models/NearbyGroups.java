package com.gametime.quadrant.Models;

import java.util.List;

public class NearbyGroups {
    private List<Group> groups = null;
    private Integer requestsCount;
    private Integer invitationCount;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Integer getRequestsCount() {
        return requestsCount;
    }

    public void setRequestsCount(Integer requestsCount) {
        this.requestsCount = requestsCount;
    }

    public Integer getInvitationCount() {
        return invitationCount;
    }

    public void setInvitationCount(Integer invitationCount) {
        this.invitationCount = invitationCount;
    }


}
