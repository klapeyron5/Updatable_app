package com.example.me.libupdater.history;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class UpdateEventsRepository {
    private UpdateEventDao updateEventDao;
    private LiveData<List<UpdateEvent>> allUpdateEvents;

    public UpdateEventsRepository(Application application) {
        UpdateEventsRoomDatabase db = UpdateEventsRoomDatabase.getDatabase(application);
        updateEventDao = db.updateEventDao();
        allUpdateEvents = updateEventDao.getAllUpdateEvents();
    }

    public LiveData<List<UpdateEvent>> getAllUpdateEvents() {
        return allUpdateEvents;
    }

    public void insert(UpdateEvent updateEvent) {
        new insertAsynchTask(updateEventDao).execute(updateEvent);
    }

    private static class insertAsynchTask extends AsyncTask<UpdateEvent,Void,Void> {
        private UpdateEventDao updateEventDao;

        insertAsynchTask(UpdateEventDao dao) {
            updateEventDao = dao;
        }

        @Override
        protected Void doInBackground(final UpdateEvent... params) {
            updateEventDao.insert(params[0]);
            return null;
        }
    }
}
