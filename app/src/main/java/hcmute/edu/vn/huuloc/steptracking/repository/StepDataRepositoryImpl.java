package hcmute.edu.vn.huuloc.steptracking.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.huuloc.steptracking.model.StepData;

public class StepDataRepositoryImpl extends SQLiteOpenHelper implements StepDataRepository {

    private static final String DATABASE_NAME = "fitness_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "step_data";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_DATE = "date";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_STEPS + " INTEGER," +
            COLUMN_DISTANCE + " REAL," +
            COLUMN_DATE + " TEXT" +
            ")";

    public StepDataRepositoryImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void saveStepData(StepData stepData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STEPS, stepData.getSteps());
        values.put(COLUMN_DISTANCE, stepData.getDistance());
        values.put(COLUMN_DATE, stepData.getDate());

        long id = db.insert(TABLE_NAME, null, values);

        db.close();
    }


    @Override
    public void updateStepData(StepData stepData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STEPS, stepData.getSteps());
        values.put(COLUMN_DISTANCE, stepData.getDistance());
        values.put(COLUMN_DATE, stepData.getDate());

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(stepData.getId())};

        db.update(TABLE_NAME, values, selection, selectionArgs);
        db.close();
    }

    @Override
    public void deleteStepData(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @Override
    public StepData getStepData(Long id) {
        StepData stepData = null;

        String[] columns = {COLUMN_ID, COLUMN_STEPS, COLUMN_DISTANCE, COLUMN_DATE};
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(
                     TABLE_NAME,
                     columns,
                     selection,
                     selectionArgs,
                     null,
                     null,
                     null)) {
            if (cursor.moveToFirst()) {
                int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);

                Integer steps = cursor.getInt(stepsIndex);
                Double distance = cursor.getDouble(distanceIndex);
                String dateString = cursor.getString(dateIndex);

                stepData = StepData.builder()
                        .id(id)
                        .steps(steps)
                        .distance(distance)
                        .date(dateString)
                        .build();
            }
        }
        return stepData;
    }

    @Override
    public List<StepData> getAllStepData() {
        List<StepData> stepDataList = new ArrayList<>();

        String[] columns = {COLUMN_ID, COLUMN_STEPS, COLUMN_DISTANCE, COLUMN_DATE};

        try (
                SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = db.query(
                        TABLE_NAME,
                        columns,
                        null,
                        null,
                        null,
                        null,
                        null
                )) {
            while (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);

                Long id = cursor.getLong(idIndex);
                Integer steps = cursor.getInt(stepsIndex);
                Double distance = cursor.getDouble(distanceIndex);
                String dateString = cursor.getString(dateIndex);

                StepData stepData = StepData.builder()
                        .id(id)
                        .steps(steps)
                        .distance(distance)
                        .date(dateString)
                        .build();

                stepDataList.add(stepData);
            }
        }
        return stepDataList;
    }

    @Override
    public StepData getStepDataByDate(String date) {
        StepData stepData = null;

        String[] columns = {COLUMN_ID, COLUMN_STEPS, COLUMN_DISTANCE, COLUMN_DATE};
        String selection = COLUMN_DATE + " = ?";
        String[] selectionArgs = {date};

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(
                     TABLE_NAME,
                     columns,
                     selection,
                     selectionArgs,
                     null,
                     null,
                     null)) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int stepsIndex = cursor.getColumnIndex(COLUMN_STEPS);
                int distanceIndex = cursor.getColumnIndex(COLUMN_DISTANCE);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);

                Long id = cursor.getLong(idIndex);
                Integer steps = cursor.getInt(stepsIndex);
                Double distance = cursor.getDouble(distanceIndex);
                String dateString = cursor.getString(dateIndex);

                stepData = StepData.builder()
                        .id(id)
                        .steps(steps)
                        .distance(distance)
                        .date(dateString)
                        .build();
            }
        }
        return stepData;
    }

    @Override
    public int getTotalSteps() {
        int totalSteps = 0;

        String selectQuery = "SELECT SUM(" + COLUMN_STEPS + ") as total FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            totalSteps = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return totalSteps;
    }
}
