package com.swagVideo.in.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swagVideo.in.data.models.ClipSection;
import com.swagVideo.in.data.models.Song;
import com.swagVideo.in.data.models.User;

import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class NearBylIst {
    String km,userImage,gif;
    public String video;
    public User user;
    public int viewsCount;
    public int likesCount;
    public int commentsCount;
    public boolean comments;
    public boolean liked;
    public boolean saved;
    public int id;
    @Nullable
    public String location;
    public List<User> mentions;
    public List<String> hashtags;
    @Nullable public Song song;

    public NearBylIst() {
    }

    public NearBylIst(String km, String userImage, String gif) {
        this.km = km;
        this.userImage = userImage;
        this.gif = gif;
    }


    public NearBylIst(String km, String userImage, String gif, String video, User user, int viewsCount, int likesCount, int commentsCount, boolean comments, boolean liked, boolean saved, int id, @Nullable String location,String description) {
        this.km = km;
        this.userImage = userImage;
        this.gif = gif;
        this.video = video;
        this.user = user;
        this.viewsCount = viewsCount;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.comments = comments;
        this.liked = liked;
        this.saved = saved;
        this.id = id;
        this.location = location;
        this.description = description;
    }
    public NearBylIst(String km, String userImage, String gif, String video, User user, int viewsCount, int likesCount, int commentsCount, boolean comments, boolean liked, boolean saved, int id, @Nullable String location, String screenshot, String description) {
        this.km = km;
        this.userImage = userImage;
        this.gif = gif;
        this.video = video;
        this.user = user;
        this.viewsCount = viewsCount;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.comments = comments;
        this.liked = liked;
        this.saved = saved;
        this.id = id;
        this.location = location;
        this.screenshot = screenshot;
        this.description = description;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getGif() {
        return gif;
    }

    public void setGif(String gif) {
        this.gif = gif;
    }



    public String screenshot;
    public String preview;
    @Nullable
    public String description;
    public String language;
    @JsonProperty("private")
    public boolean _private;
    public int duration;

    @Nullable
    public Double latitude;
    @Nullable
    public Double longitude;
    public boolean approved;
    public Date createdAt;
    public Date updatedAt;
    public List<ClipSection> sections;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean is_private() {
        return _private;
    }

    public void set_private(boolean _private) {
        this._private = _private;
    }

    public boolean isComments() {
        return comments;
    }

    public void setComments(boolean comments) {
        this.comments = comments;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Nullable
    public String getLocation() {
        return location;
    }

    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    @Nullable
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@Nullable Double latitude) {
        this.latitude = latitude;
    }

    @Nullable
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@Nullable Double longitude) {
        this.longitude = longitude;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Nullable
    public Song getSong() {
        return song;
    }

    public void setSong(@Nullable Song song) {
        this.song = song;
    }

    public List<ClipSection> getSections() {
        return sections;
    }

    public void setSections(List<ClipSection> sections) {
        this.sections = sections;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<User> getMentions() {
        return mentions;
    }

    public void setMentions(List<User> mentions) {
        this.mentions = mentions;
    }
}
