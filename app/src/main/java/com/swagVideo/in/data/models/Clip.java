package com.swagVideo.in.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Clip implements Parcelable {

    public static final Map<Integer, Boolean> LIKED = new ConcurrentHashMap<>();

    public int id;
    public String video;
    public String screenshot;
    public String preview;
    @Nullable
    public String description;
    public String language;
    @JsonProperty("private")
    public boolean _private;
    public boolean comments;
    public int duration;
    @Nullable
    public String location;
    @Nullable
    public Double latitude;
    @Nullable
    public Double longitude;
    public boolean approved;
    public Date createdAt;
    public Date updatedAt;
    public User user;
    @Nullable public Song song;
    public List<ClipSection> sections;
    public int viewsCount;
    public int likesCount;
    public int commentsCount;
    public boolean liked;
    public boolean saved;
    public List<String> hashtags;
    public List<User> mentions;

    @JsonIgnore
    public boolean ad;

    public Clip() {
    }

    public static Map<Integer, Boolean> getLIKED() {
        return LIKED;
    }

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

    public boolean isAd() {
        return ad;
    }

    public void setAd(boolean ad) {
        this.ad = ad;
    }

    public static Creator<Clip> getCREATOR() {
        return CREATOR;
    }

    protected Clip(Parcel in) {
        id = in.readInt();
        video = in.readString();
        screenshot = in.readString();
        preview = in.readString();
        description = in.readString();
        language = in.readString();
        _private = in.readByte() != 0;
        comments = in.readByte() != 0;
        duration = in.readInt();
        user = in.readParcelable(User.class.getClassLoader());
        song = in.readParcelable(Song.class.getClassLoader());
        sections = in.createTypedArrayList(ClipSection.CREATOR);
        viewsCount = in.readInt();
        likesCount = in.readInt();
        commentsCount = in.readInt();
        liked = in.readByte() != 0;
        saved = in.readByte() != 0;
        hashtags = in.createStringArrayList();
        mentions = in.createTypedArrayList(User.CREATOR);
    }

    public boolean liked() {
        return LIKED.containsKey(id) ? LIKED.get(id) : liked;
    }

    public void liked(boolean followed) {
        LIKED.put(id, this.liked = followed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(video);
        dest.writeString(screenshot);
        dest.writeString(preview);
        dest.writeString(description);
        dest.writeString(language);
        dest.writeByte((byte) (_private ? 1 : 0));
        dest.writeByte((byte) (comments ? 1 : 0));
        dest.writeInt(duration);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(song, flags);
        dest.writeTypedList(sections);
        dest.writeInt(viewsCount);
        dest.writeInt(likesCount);
        dest.writeInt(commentsCount);
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeByte((byte) (saved ? 1 : 0));
        dest.writeStringList(hashtags);
        dest.writeTypedList(mentions);
    }

    public static final Creator<Clip> CREATOR = new Creator<Clip>() {

        @Override
        public Clip createFromParcel(Parcel in) {
            return new Clip(in);
        }

        @Override
        public Clip[] newArray(int size) {
            return new Clip[size];
        }
    };
}
