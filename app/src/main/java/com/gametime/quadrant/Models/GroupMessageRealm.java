package com.gametime.quadrant.Models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Akansh on 16-12-2017.
 */

public class GroupMessageRealm extends RealmObject {
    @PrimaryKey
    private String uniqueMsgId;
    private String nick;
    private String body;
    private String stanzId;
    private String time;
    private String peer;
    private String xml;
    private String text;
    private String thumbnailUrl;
    private String imageUrl;
    private String imageName;
    private String groupJid;

    public String getUniqueMsgId() { return uniqueMsgId; }

    public void setUniqueMsgId(String uniqueMsgId) { this.uniqueMsgId = uniqueMsgId; }

    public String getNick() { return nick; }

    public void setNick(String nick) { this.nick = nick; }

    public String getBody() { return body; }

    public void setBody(String body) { this.body = body; }

    public String getStanzId() { return stanzId; }

    public void setStanzId(String stanzId) { this.stanzId = stanzId; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getPeer() { return peer; }

    public void setPeer(String peer) { this.peer = peer; }

    public String getXml() { return xml; }

    public void setXml(String xml) { this.xml = xml; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getThumbnailUrl() { return thumbnailUrl; }

    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageName() { return imageName; }

    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getGroupJid() { return groupJid; }

    public void setGroupJid(String groupJid) { this.groupJid = groupJid; }
}