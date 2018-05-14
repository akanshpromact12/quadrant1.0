package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 11-11-2017.
 */

public class CreateGroup {
    private String access;
    private String boundryType;
    private String description;
    private int free;
    private int invisible;
    private String name;
    private String password;
    private String polygon;

    private String requestAccessType;

    public CreateGroup() {

    }

    public CreateGroup(String access, String boundryType, String description, int free, int invisible, String name, String password, String polygon, String requestAccessType) {
        this.access = access;
        this.boundryType = boundryType;
        this.description = description;
        this.free = free;
        this.invisible = invisible;
        this.name = name;
        this.password = password;
        this.polygon = polygon;
        this.requestAccessType = requestAccessType;
    }
}
