package de.wackernagel.android.sidekick.frameworks.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TaskInfo {

    public static final int TRANSPORT_BROADCAST = 0;
    public static final int TRANSPORT_LOCAL_BROADCAST = 1;
    public static final int TRANSPORT_ORDERED_BROADCAST = 2;
    public static final int TRANSPORT_NONE = 3;

    @IntDef({TRANSPORT_BROADCAST, TRANSPORT_LOCAL_BROADCAST, TRANSPORT_ORDERED_BROADCAST, TRANSPORT_NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Transport {}

    private final Class<? extends TaskService> serviceClass;
    private final int taskId;

    private Bundle arguments = Bundle.EMPTY;
    private boolean singleInstance = false;
    private boolean redeliver = false;
    private int resultTransport = TRANSPORT_NONE;

    public static TaskInfo enqueue(@NonNull final Class<? extends TaskService> serviceClass, final int taskId) {
        return new TaskInfo( serviceClass, taskId );
    }

    private TaskInfo( @NonNull final Class<? extends TaskService> serviceClass, final int taskId ) {
        this.serviceClass = serviceClass;
        this.taskId = taskId;
    }

    public TaskInfo arguments( @NonNull final Bundle arguments ) {
        this.arguments = arguments;
        return this;
    }

    public TaskInfo singleInstance() {
        this.singleInstance = true;
        return this;
    }

    public TaskInfo resultTransport( @Transport int resultTransport ) {
        this.resultTransport = resultTransport;
        return this;
    }

    public TaskInfo redeliver() {
        this.redeliver = true;
        return this;
    }

    public void build(@NonNull final Context context) {
        final Intent task = new Intent( context, serviceClass );
        task.putExtra( TaskService.INTENT_EXTRA_TASK_ID, taskId );
        task.putExtra( TaskService.INTENT_EXTRA_ARGUMENTS, arguments );
        task.putExtra( TaskService.INTENT_EXTRA_SINGLE_INSTANCE, singleInstance );
        task.putExtra( TaskService.INTENT_EXTRA_RESULT_TRANSPORT, resultTransport );
        task.putExtra( TaskService.INTENT_EXTRA_REDELIVER, redeliver );
        context.startService( task );
    }

}
