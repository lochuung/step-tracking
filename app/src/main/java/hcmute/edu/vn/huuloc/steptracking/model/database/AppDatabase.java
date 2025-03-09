package hcmute.edu.vn.huuloc.steptracking.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import hcmute.edu.vn.huuloc.steptracking.model.dao.StepDataDao;
import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;

@Database(entities = {StepData.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "fitness_tracker.db";
    private static volatile AppDatabase instance;

    public abstract StepDataDao stepDataDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
