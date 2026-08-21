package com.styro.medigo.model;

public class Category {

    private String categoryId;
    private String name;
    private String description;
    private String iconUrl;
    private int itemCount;
    private boolean isActive;

    public Category() {

    }

    public Category(String categoryId, String name, String iconUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconUrl = iconUrl;
        this.isActive = true;
        this.itemCount = 0;
    }


    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}