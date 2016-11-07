package de.wackernagel.android.sidekick.frameworks.tasks;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

public abstract class TaskService extends IntentService {
	private static final String TAG = "TaskService";

    public static final String INTENT_EXTRA_TASK_ID = TAG + ":task:id";
	public static final String INTENT_EXTRA_ARGUMENTS = TAG + ":arguments";
	public static final String INTENT_EXTRA_SINGLE_INSTANCE = TAG + ":single:instance";
    public static final String INTENT_EXTRA_RESULT_TRANSPORT = TAG + ":one:way";
    public static final String INTENT_EXTRA_REDELIVER = TAG + ":redeliver";
    public static final String INTENT_EXTRA_RESULT_CODE = TAG + ":result:code";
    public static final String INTENT_EXTRA_RESULT = TAG + ":result";

    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_CONDITION_FAILED = 2;

    public static final String ACTION_TASK_COMPLETE = "task.complete";
    public static final String ACTION_TASK_QUEUE_EMPTY = "task.queue.empty";

    private ArrayList<Integer> queuedTaskId = new ArrayList<>();

	public TaskService() {
		super( TAG );
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        // CHECK FOR SINGLETON TASKS AND SKIP ALREADY QUEUED TASKS
        final int taskId = intent.getIntExtra(INTENT_EXTRA_TASK_ID, -1);
        if( intent.getBooleanExtra(INTENT_EXTRA_SINGLE_INSTANCE, false) ) {
            if( queuedTaskId.contains( taskId ) ) {
                return START_NOT_STICKY;
            }
        }
        queuedTaskId.add(taskId);

        // RESCHEDULE THIS INTENT IF THE SERVICE GOES DOWN
		setIntentRedelivery(intent.getBooleanExtra(INTENT_EXTRA_REDELIVER, false));
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent( Intent intent ) {
		// CREATE TASK
		final int taskId = intent.getIntExtra( INTENT_EXTRA_TASK_ID, -1 );
		final Task task = resolveTask( taskId );

		// EXECUTE TASK
		Bundle result;
        int resultCode;
        Bundle arguments = intent.getBundleExtra(INTENT_EXTRA_ARGUMENTS);
        arguments = arguments != null ? arguments : new Bundle();

        if( task.conditionsComplied( this, arguments ) ) {
            try {
                final Bundle taskResult = task.doInBackground( this, arguments );
                result = taskResult != null ? taskResult : Bundle.EMPTY;
                resultCode = RESULT_OK;
                task.onCompleted( this, result );
                onTaskCompleted(taskId, result);
            } catch( Exception error ) {
                result = Bundle.EMPTY;
                resultCode = RESULT_ERROR;
                task.onException( this, error, result );
                onTaskException(taskId, error, result);
            }
        } else {
            result = Bundle.EMPTY;
            resultCode = RESULT_CONDITION_FAILED;
            task.onConditionFailed( this, result );
            onTaskConditionFailed(taskId, result);
        }

		// DELIVER RESULT
        final int resultTransport = intent.getIntExtra(INTENT_EXTRA_RESULT_TRANSPORT, TaskInfo.TRANSPORT_BROADCAST );
        if( resultTransport != TaskInfo.TRANSPORT_NONE ) {
            final Intent complete = new Intent(ACTION_TASK_COMPLETE);
            complete.putExtra(INTENT_EXTRA_TASK_ID, taskId);
            complete.putExtra(INTENT_EXTRA_RESULT_CODE, resultCode);
            complete.putExtra(INTENT_EXTRA_RESULT, result);

            if( resultTransport == TaskInfo.TRANSPORT_BROADCAST ) {
                sendBroadcast(complete);
            } else if( resultTransport == TaskInfo.TRANSPORT_LOCAL_BROADCAST ) {
                LocalBroadcastManager.getInstance( this ).sendBroadcast( complete );
            } else if( resultTransport == TaskInfo.TRANSPORT_ORDERED_BROADCAST ) {
                sendOrderedBroadcast(complete, null);
            }
        }

        // REMOVE TASK FROM INTERNAL QUEUE INFO
        queuedTaskId.remove(Integer.valueOf(taskId));
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        onEmptyTaskQueue();
    }

    /**
     * This method is called when the task queue is empty.
     * The default implementation sends a ordered broadcast with intent action 'task.queue.empty'.
     */
    public void onEmptyTaskQueue() {
        // TASK QUEUE IS EMPTY
        sendOrderedBroadcast(new Intent(ACTION_TASK_QUEUE_EMPTY), null);
    }

    /**
     * Task was successful completed. Use this method for custom handling.
     *
     * @param taskId as identifier
     * @param result of executed task which is send to foreground
     */
    public void onTaskCompleted( final int taskId, @NonNull final Bundle result ) {
	}

    /**
     * Not all conditions/requirements of the task complied. Use this method for custom handling.
     *
     * @param taskId as identifier
     * @param result which is send to foreground
     */
    public void onTaskConditionFailed(final int taskId, Bundle result) {
    }

    /**
     * During the task execution appeared an exception. Use this method for custom error handling.
     *  @param taskId as identifier
     * @param error which was thrown
     * @param result which is send to foreground
     */
    public void onTaskException(final int taskId, @NonNull final Exception error, @NonNull final Bundle result) {
    }

    /**
     * Map a taskId to a concrete task.
     *
     * @param taskId as identifier
     * @return task to
     */
    @NonNull
    abstract public Task resolveTask( final int taskId );

}
