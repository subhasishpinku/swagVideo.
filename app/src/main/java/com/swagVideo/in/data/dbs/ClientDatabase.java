package com.swagVideo.in.data.dbs;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.swagVideo.in.data.daos.DraftDao;
import com.swagVideo.in.data.entities.Draft;

@Database(entities = {Draft.class}, version = 2, exportSchema = false)
public abstract class ClientDatabase extends RoomDatabase {
    public abstract DraftDao drafts();
}
