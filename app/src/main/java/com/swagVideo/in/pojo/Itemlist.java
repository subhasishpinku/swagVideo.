package com.swagVideo.in.pojo;

public class Itemlist {
    String img, count,title,userName,userImage;

    public Itemlist(String img, String count, String title, String userName, String userImage) {
        this.img = img;
        this.count = count;
        this.title = title;
        this.userName = userName;
        this.userImage = userImage;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
