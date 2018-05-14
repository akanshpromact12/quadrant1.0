package com.gametime.quadrant.Models;

/**
 * Created by Akansh on 11-05-2018.
 */

public class ReportUser {
    private String reportingUserId;
    private String reportingMessage;
    private String reportingType;
    private String reportingTimeStamp;
    private String messageXml;
    private String messageTimeStamp;
    private String senderJid;
    private String receiverJid;

    public ReportUser(String reportingUserId, String reportingMessage, String reportingType, String reportingTimeStamp, String messageXml, String messageTimeStamp, String senderJid, String receiverJid) {
        this.reportingUserId = reportingUserId;
        this.reportingMessage = reportingMessage;
        this.reportingType = reportingType;
        this.reportingTimeStamp = reportingTimeStamp;
        this.messageXml = messageXml;
        this.messageTimeStamp = messageTimeStamp;
        this.senderJid = senderJid;
        this.receiverJid = receiverJid;
    }
}
