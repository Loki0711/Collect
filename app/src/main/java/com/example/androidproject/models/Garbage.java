package com.example.androidproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Objects;


public class Garbage implements Parcelable {

    private String id;
    private String addedBy;
    private String garbageType;
    private String status;
    private Double lat;
    private Double lng;
    private Date addedOn;
    private Date pickedOn;
    private String pickedBy;
    private String acceptedBy;

    protected Garbage(Parcel in) {
        id = in.readString();
        addedBy = in.readString();
        garbageType = in.readString();
        status = in.readString();
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }
        if (in.readByte() == 0) {
            lng = null;
        } else {
            lng = in.readDouble();
        }
        pickedBy = in.readString();
        acceptedBy = in.readString();
        addedOn = (java.util.Date) in.readSerializable();
        pickedOn = (java.util.Date) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(addedBy);
        dest.writeString(garbageType);
        dest.writeString(status);
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }
        if (lng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lng);
        }
        dest.writeString(pickedBy);
        dest.writeString(acceptedBy);
        dest.writeSerializable(addedOn);
        dest.writeSerializable(pickedOn);
    }

    public static final Creator<Garbage> CREATOR = new Creator<Garbage>() {
        @Override
        public Garbage createFromParcel(Parcel in) {
            return new Garbage(in);
        }

        @Override
        public Garbage[] newArray(int size) {
            return new Garbage[size];
        }
    };

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }




    public Garbage( ){

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Garbage)) return false;
        Garbage garbage = (Garbage) o;
        return Objects.equals(getId(), garbage.getId()) && Objects.equals(getAddedBy(), garbage.getAddedBy()) && Objects.equals(getGarbageType(), garbage.getGarbageType()) && Objects.equals(getStatus(), garbage.getStatus()) && Objects.equals(getLat(), garbage.getLat()) && Objects.equals(getLng(), garbage.getLng()) && Objects.equals(getAddedOn(), garbage.getAddedOn()) && Objects.equals(getPickedOn(), garbage.getPickedOn()) && Objects.equals(getPickedBy(), garbage.getPickedBy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAddedBy(), getGarbageType(), getStatus(), getLat(), getLng(), getAddedOn(), getPickedOn(), getPickedBy());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getGarbageType() {
        return garbageType;
    }

    public void setGarbageType(String garbageType) {
        this.garbageType = garbageType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Date getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public Date getPickedOn() {
        return pickedOn;
    }

    public void setPickedOn(Date pickedOn) {
        this.pickedOn = pickedOn;
    }

    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(String pickedBy) {
        this.pickedBy = pickedBy;
    }


    @Override
    public int describeContents() {
        return 0;
    }

}
