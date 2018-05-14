package com.gametime.quadrant;

/**
 * Created by Akansh on 05-04-2018.
 */


public enum ImageUploadStatus {
    NONE("NONE", 0),
    INPROGRESS("INPROGRESS", 1),
    SUCCESS("SUCCESS", 2),
    FAILED("FAILED",3);

    private String typeString;
    private int typeInt;

    ImageUploadStatus(String typeString, int typeInt) {
        this.typeString = typeString;
        this.typeInt = typeInt;
    }
    public String getName() { return this.typeString; }

    public int getValue() { return this.typeInt; }
}
