package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 28-11-2017.
 */

public class JoinGroup {
    private String password;
    private String id;

    public JoinGroup(String password, String id) {
        this.password = password;
        this.id = id;
    }

    public JoinGroup(String id) {
        this.id = id;
    }
}
