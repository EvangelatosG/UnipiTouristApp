package com.hfad.ergasia3;

//Class that represents a POI
public class POI {

    String id;
    String title;
    String description;
    String category;
    Double latitude;
    Double longitude;

    //Constructors
    public POI(String id, String title, String description, String category,Double latitude, Double longitude ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public POI(){

    }

    //Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    //Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
