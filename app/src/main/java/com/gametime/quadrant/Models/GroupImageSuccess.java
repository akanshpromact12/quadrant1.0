package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 15-12-2017.
 */

public class GroupImageSuccess {
    private String imageUri;
    private String thumbUri;
    private String imageName;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getImageName() {
        if(getImageUri() != null && !getImageUri().isEmpty()){
            imageName = getImageUri().split("/")[getImageUri().split("/").length-1];
        }

        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
