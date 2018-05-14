package com.gametime.quadrant.Models;

import com.gametime.quadrant.Exceptions.GenExceptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Akansh on 15-12-2017.
 */

public class MessageSuccess {
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
    private static final String TAG = "MessageSuccess";

    public MessageSuccess(String uniqueMsgId, String nick, String body, String stanzId, String time, String peer, String xml, String thumbnailUrl, String imageUrl, String imageName) {
        this.uniqueMsgId = uniqueMsgId;
        this.nick = nick;
        this.body = body;
        this.stanzId = stanzId;
        this.time = time;
        this.peer = peer;
        this.xml = xml;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
    }

    public MessageSuccess(String uniqueMsgId, String nick, String body, String stanzId, String time, String peer, String xml, String text) {
        this.uniqueMsgId = uniqueMsgId;
        this.nick = nick;
        this.body = body;
        this.stanzId = stanzId;
        this.time = time;
        this.peer = peer;
        this.xml = xml;
        this.text = text;
    }

    public String getThumbnailUrl() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document parse = documentBuilder.parse(new ByteArrayInputStream(getXml().getBytes()));
            Element element = parse.getDocumentElement();

            if (element.getElementsByTagName("thumbnail").getLength() > 0) {
                Element node = (Element) element.getElementsByTagName("thumbnail").item(0);
                thumbnailUrl = node.getAttribute("thumbnail");
            } else {
                return null;
            }

        } catch (Exception e) {
            GenExceptions.logException(e);
        }

        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getImageUrl() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document parse = documentBuilder.parse(new ByteArrayInputStream(getXml().getBytes()));
            Element element = parse.getDocumentElement();

            if (element.getElementsByTagName("image").getLength() > 0) {
                Element node = (Element) element.getElementsByTagName("image").item(0);
                imageUrl = node.getAttribute("name");
            } else {
                return null;
            }

        } catch (Exception e) {
            GenExceptions.logException(e);
        }

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public String getText() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document parse = documentBuilder.parse(new ByteArrayInputStream(getXml().getBytes()));
            text = parse.getFirstChild().getTextContent();

        } catch (Exception e) {
            GenExceptions.logException(e);
        }

        return text;
    }

    public void setText(String text) {
        this.text = text;
    }



    public String getUniqueMsgId() {
        return uniqueMsgId;
    }

    public void setUniqueMsgId(String uniqueMsgId) {
        this.uniqueMsgId = uniqueMsgId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStanzId() {
        return stanzId;
    }

    public void setStanzId(String stanzId) {
        this.stanzId = stanzId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }


    public String getImageName() {
        if(getImageUrl() != null && !getImageUrl().isEmpty()){
            imageName = getImageUrl().split("/")[getImageUrl().split("/").length-1];
        }
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}