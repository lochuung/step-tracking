package hcmute.edu.vn.huuloc.steptracking.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;

@Dao
public interface StepDataDao {
    @Insert
    long insert(StepData stepData);

    @Update
    void update(StepData stepData);

    @Delete
    void delete(StepData stepData);

    @Query("SELECT * FROM step_data WHERE id = :id")
    StepData getById(Long id);

    @Query("SELECT * FROM step_data")
    List<StepData> getAll();

    @Query("SELECT * FROM step_data WHERE date = :date")
    StepData getByDate(String date);

    @Query("SELECT SUM(steps) FROM step_data")
    int getTotalSteps();
    
    @Query("DELETE FROM step_data WHERE id = :id")
    void deleteById(Long id);
}
