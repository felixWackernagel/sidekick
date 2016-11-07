package de.wackernagel.android.sidekick.frameworks.tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Task {

	/**
	 * Check if all conditions/requirements for the execution of this task are complied.
	 *
	 * @param context from IntentService
	 * @param arguments for task
	 * @return true if all conditions of task complied otherwise false
	 */
	boolean conditionsComplied(@NonNull Context context, @NonNull Bundle arguments);

	/**
	 * Performs a task in background thread.
	 *
	 * @param context from IntentService
	 * @param arguments for task
	 * @return bundle with result data from task
	 */
	@Nullable
	Bundle doInBackground(@NonNull Context context, @NonNull Bundle arguments);

    /**
     * Callback method when the run conditions of this task not fulfilled.
     *
     * @param context from IntentService
     * @param result empty bundle which is send in a broadcast
     */
    void onConditionFailed(@NonNull Context context, @NonNull Bundle result);

    /**
     * Callback method when this task is successful.
     *
     * @param context from IntentService
     * @param result result of this task which is send in a broadcast.
     */
	void onCompleted(@NonNull Context context, @NonNull Bundle result);

    /**
     * Callback method when a exception inside this task occurred.
     *
     * @param context from IntentService
     * @param error which occurred
     * @param result empty bundle which is send in a broadcast
     */
    void onException(@NonNull Context context, @NonNull Exception error, @NonNull Bundle result);

}
