package com.example.trekerautoapp.model;

public class PartItem {
    private String id;
    private String name;
    private String controlType;
    private long intervalKm;
    private long lastServiceMileage;
    private boolean lastServiceMileageKnown;
    private long createdAt;

    public PartItem() {
    }

    public PartItem(
            String name,
            String controlType,
            long intervalKm,
            long lastServiceMileage,
            boolean lastServiceMileageKnown,
            long createdAt
    ) {
        this.name = name;
        this.controlType = controlType;
        this.intervalKm = intervalKm;
        this.lastServiceMileage = lastServiceMileage;
        this.lastServiceMileageKnown = lastServiceMileageKnown;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getControlType() {
        return controlType == null ? "" : controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public long getIntervalKm() {
        return intervalKm;
    }

    public void setIntervalKm(long intervalKm) {
        this.intervalKm = intervalKm;
    }

    public long getLastServiceMileage() {
        return lastServiceMileage;
    }

    public void setLastServiceMileage(long lastServiceMileage) {
        this.lastServiceMileage = lastServiceMileage;
    }

    public boolean isLastServiceMileageKnown() {
        return lastServiceMileageKnown;
    }

    public void setLastServiceMileageKnown(boolean lastServiceMileageKnown) {
        this.lastServiceMileageKnown = lastServiceMileageKnown;
    }

    public boolean hasKnownLastServiceMileage() {
        return lastServiceMileageKnown || lastServiceMileage > 0L;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
