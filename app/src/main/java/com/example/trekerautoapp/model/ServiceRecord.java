package com.example.trekerautoapp.model;

public class ServiceRecord {
    private String id;
    private String partId;
    private String partName;
    private String actionType;
    private long serviceDate;
    private String serviceDateText;
    private long mileage;
    private String comment;
    private double cost;
    private long createdAt;

    public ServiceRecord() {
    }

    public ServiceRecord(
            String partId,
            String partName,
            String actionType,
            long serviceDate,
            String serviceDateText,
            long mileage,
            String comment,
            double cost,
            long createdAt
    ) {
        this.partId = partId;
        this.partName = partName;
        this.actionType = actionType;
        this.serviceDate = serviceDate;
        this.serviceDateText = serviceDateText;
        this.mileage = mileage;
        this.comment = comment;
        this.cost = cost;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartId() {
        return partId == null ? "" : partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName == null ? "" : partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getActionType() {
        return actionType == null ? "" : actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public long getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(long serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getServiceDateText() {
        return serviceDateText == null ? "" : serviceDateText;
    }

    public void setServiceDateText(String serviceDateText) {
        this.serviceDateText = serviceDateText;
    }

    public long getMileage() {
        return mileage;
    }

    public void setMileage(long mileage) {
        this.mileage = mileage;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
