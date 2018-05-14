package com.gametime.quadrant.Models;

import com.gametime.quadrant.ImageUploadStatus;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akansh on 16-12-2017.
 */

public class PrivateMessageRealm extends RealmObject {
    @PrimaryKey
    private String message_id;
    private String message;
    private String message_sender;
    private String message_datetime;
    private Boolean multimedia_identifier;
    private String image_url;
    private String thumb_url;
    private String pm_holder_name;
    private int status = ImageUploadStatus.NONE.getValue();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_sender() {
        return message_sender;
    }

    public void setMessage_sender(String message_sender) {
        this.message_sender = message_sender;
    }

    public String getMessage_datetime() {
        return message_datetime;
    }

    public void setMessage_datetime(String message_datetime) {
        this.message_datetime = message_datetime;
    }

    public Boolean getMultimedia_identifier() {
        return multimedia_identifier;
    }

    public void setMultimedia_identifier(Boolean multimedia_identifier) {
        this.multimedia_identifier = multimedia_identifier;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getPMHolderName() {
        return pm_holder_name;
    }

    public void setPMHolderName(String pm_holder_name) {
        this.pm_holder_name = pm_holder_name;
    }
}
