package com.example.trekerautoapp.app;

import android.app.Application;

import com.example.trekerautoapp.notifications.PartReminderNotifier;
import com.example.trekerautoapp.notifications.PartReminderScheduler;

public class TrekerAutoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PartReminderNotifier.ensureChannel(this);
        PartReminderScheduler.schedule(this);
    }
}
