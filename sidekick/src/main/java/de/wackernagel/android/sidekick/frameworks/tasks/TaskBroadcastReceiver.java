package de.wackernagel.android.sidekick.frameworks.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * A BroadcastReceiver which listen on TaskService intents.
 * Use this receiver as a sink to handle all intents with a default implementation.
 * The default implementation listen on actions "task.queue.empty" and "task.complete"
 */
public abstract class TaskBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if( TaskService.ACTION_TASK_COMPLETE.equals( action ) ) {
            final Bundle result = intent.getBundleExtra(TaskService.INTENT_EXTRA_RESULT);
            onTaskEnd(
                intent.getIntExtra( TaskService.INTENT_EXTRA_TASK_ID, -1 ),
                intent.getIntExtra( TaskService.INTENT_EXTRA_RESULT_CODE, 0 ),
                result == null ? Bundle.EMPTY : result
            );
        } else if( TaskService.ACTION_TASK_QUEUE_EMPTY.equals( action ) ) {
            onTaskQueueEmpty();
        }

        if( isOrderedBroadcast() ) {
            abortBroadcast();
        }
    }

    abstract public void onTaskEnd( int taskId, int resultCode, Bundle resultData );

    abstract public void onTaskQueueEmpty();
}
