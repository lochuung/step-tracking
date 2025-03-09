package hcmute.edu.vn.huuloc.steptracking.repository;

import android.content.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.huuloc.steptracking.model.dao.StepDataDao;
import hcmute.edu.vn.huuloc.steptracking.model.database.AppDatabase;
import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;

public class StepDataRepositoryImpl implements StepDataRepository {

    private final StepDataDao stepDataDao;
    private final ExecutorService executorService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public StepDataRepositoryImpl(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.stepDataDao = database.stepDataDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public long saveStepData(StepData stepData) {
        final long[] id = {0};
        try {
            executorService.submit(() -> {
                id[0] = stepDataDao.insert(stepData);
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id[0];
    }

    @Override
    public void updateStepData(StepData stepData) {
        executorService.execute(() -> stepDataDao.update(stepData));
    }

    @Override
    public void deleteStepData(Long id) {
        executorService.execute(() -> stepDataDao.deleteById(id));
    }

    @Override
    public StepData getStepData(Long id) {
        try {
            return executorService.submit(() -> stepDataDao.getById(id)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StepData> getAllStepData() {
        try {
            return executorService.submit(stepDataDao::getAll).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public StepData getStepDataByDate(LocalDate date) {
        try {
            // Convert LocalDate to String before passing to DAO
            String dateStr = date != null ? date.format(formatter) : null;
            return executorService.submit(() -> stepDataDao.getByDate(dateStr)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getTotalSteps() {
        try {
            return executorService.submit(stepDataDao::getTotalSteps).get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
