package com.example.lifetune;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {GoalItem.class}, version = 5, exportSchema = false)
public abstract class GoalDatabase extends RoomDatabase {
    public abstract GoalDao goalDao();

    private static volatile GoalDatabase INSTANCE;

    // Миграция с 3 на 4 версию
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE goals ADD COLUMN category TEXT");
            database.execSQL("ALTER TABLE goals ADD COLUMN repetition TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Какие-то изменения для версии 3, если они были
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE goals ADD COLUMN created_at INTEGER DEFAULT 0");
        }
    };
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE goals ADD COLUMN duration_hours INTEGER DEFAULT 0");
        }
    };
    public static GoalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GoalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GoalDatabase.class,
                                    "goals_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,MIGRATION_4_5)
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
