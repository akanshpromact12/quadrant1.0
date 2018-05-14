package com.gametime.quadrant.Models;

import java.util.List;

public class CommonFriends {
    private Success success;

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public class AllMutualFriends {
        private String name;
        private String token;
        private Picture picture;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Picture getPicture() {
            return picture;
        }

        public void setPicture(Picture picture) {
            this.picture = picture;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    public class Context {
        private List<AllMutualFriends> all_mutual_friends = null;
        private String id;

        public List<AllMutualFriends> getAllMutualFriends() {
            return all_mutual_friends;
        }

        public void setAllMutualFriends(List<AllMutualFriends> all_mutual_friends) {
            this.all_mutual_friends = all_mutual_friends;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public class Picture {
        private Integer height;
        private Boolean isSilhouette;
        private String url;
        private Integer width;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Boolean getIsSilhouette() {
            return isSilhouette;
        }

        public void setIsSilhouette(Boolean isSilhouette) {
            this.isSilhouette = isSilhouette;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

    }

    public class Success {
        private Context context;
        private String id;

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
