package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 01-12-2017.
 */

public class CreateInvitation {
    public String gid;
    public String inviteExternalId;

    public CreateInvitation(String gid, String inviteExternalId) {
        this.gid = gid;
        this.inviteExternalId = inviteExternalId;
    }
}
