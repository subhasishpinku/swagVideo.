package com.swagVideo.in.pojo;

public class SliderItem {
    String description;
    String image;
    int img;

    public SliderItem() {
    }

    public SliderItem(String description, int img) {
        this.description = description;
        this.img = img;
    }
public SliderItem(String description, String image) {
        this.description = description;
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
