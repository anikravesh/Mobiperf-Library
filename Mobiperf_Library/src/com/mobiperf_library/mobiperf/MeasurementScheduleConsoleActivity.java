/* Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiperf_library.mobiperf;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobiperf_library.MeasurementTask;
import com.mobiperf_library.R;
import com.mobiperf_library.UpdateIntent;
import com.mobiperf_library.api.API;
import com.mobiperf_library.exceptions.MeasurementError;
import com.mobiperf_library.measurements.DnsLookupTask;
import com.mobiperf_library.measurements.HttpTask;
import com.mobiperf_library.measurements.PingTask;
import com.mobiperf_library.measurements.TCPThroughputTask;
import com.mobiperf_library.measurements.TracerouteTask;
import com.mobiperf_library.measurements.UDPBurstTask;
import com.mobiperf_library.util.Logger;
import com.mobiperf_library.util.MLabNS;

/**
 * Activity that shows the current measurement schedule of the scheduler
 */
public class MeasurementScheduleConsoleActivity extends Activity {
	public static final String TAB_TAG = "MEASUREMENT_SCHEDULE";

	private SpeedometerApp parent;
	private Console console;
	private API api;
	
	private TaskItemAdapter adapter;
	private ArrayList<TaskItem> taskItems= new ArrayList<TaskItem>();
	private ListView consoleView;
	
	//  private TextView lastCheckinTimeText;
//	private ArrayAdapter<String> consoleContent;
	// Maps the toString() of a measurementTask to its key
//	private HashMap<String, String> taskMap;
//	private int longClickedItemPosition = -1;
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.measurement_schedule);
		
		this.adapter= new TaskItemAdapter(this, R.layout.measurement_schedule, taskItems);
		

//		taskMap = new HashMap<String, String>();
		parent = (SpeedometerApp) this.getParent();
		this.api = API.getAPI(parent, "new mobiperf");
		
		this.console = parent.getConsole();
		
//		consoleContent = new ArrayAdapter<String>(this, R.layout.list_item);
		this.consoleView = (ListView) this.findViewById(R.id.measurementScheduleConsole);
		this.consoleView.setAdapter(adapter);
		
		


		registerForContextMenu(consoleView);
		consoleView.setOnItemLongClickListener(new OnItemLongClickListener() {
			/**
			 * Records which item in the list is selected
			 */
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				longClickedItemPosition = position;
				return false;
			}
		});

		/**
		 * TODO(Hongyi): will we still log those check-in tasks? Now the scheduler
		 *  will not broadcast server task list to the client 
		 *  If so, I suggest we should rewrite API to use intent to send information
		 *  back to the user, including measurement results.
		 *  It is more flexable and scalable 
		 */
		//    // Register activity specific BroadcastReceiver here    
//		    IntentFilter filter = new IntentFilter();
		    //TODO(Ashkan) change it to MobiperfIntent
//		    filter.addAction(UpdateIntent.USER_RESULT_ACTION);
		    
//		    this.receiver = new BroadcastReceiver() {
//				      @Override
//				      public void onReceive(Context context, Intent intent) {
//				    	  if ( intent.getAction().equals(UpdateIntent.USER_RESULT_ACTION) ) {
//				    		  }
//				      }
//		    };
//		    this.registerReceiver(this.receiver, filter);
		//        Logger.d("MeasurementConsole got intent");
		//        /* The content of the console is maintained by the scheduler. We simply hook up the 
		//         * view with the content here. */
		//        updateConsole();
		//      }
		//    };
		//    registerReceiver(receiver, filter);
	}

	/**
	 * Handles context menu creation for the ListView in the console
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.scheduler_console_context_menu, menu);
	}

	@Override
	protected void onResume() {
		updateTasksFromConsole();
		super.onResume();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//    unregisterReceiver(receiver);
	}

	class TaskItemAdapter extends ArrayAdapter<TaskItem> {
		private ArrayList<TaskItem> taskItems;
		public TaskItemAdapter(Context context, int textViewResourceId, ArrayList<TaskItem> items) {
			super(context, textViewResourceId, items);
			this.taskItems = items;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.scheduled_task_list_item, null);
			}
			TaskItem  item = taskItems.get(position);
			if(item!=null){
				String taskId=item.getTaskId();
				ToggleButton pauseButton=(ToggleButton) (v.findViewById(R.id.pausebutton));
				if(console.isPaused(taskId)){
					pauseButton.setChecked(true);
				}
				else{
					pauseButton.setChecked(false);
				}
				pauseButton.setOnClickListener(new View.OnClickListener() {
					private TaskItem taskitem;
					 public void onClick(View v) {
		            	 boolean paused = ((ToggleButton) v).isChecked();
		            	    if (paused) {
		            	        //canceling the task
		            	    	try {
		            	    		MeasurementScheduleConsoleActivity.this.api.cancelTask(taskitem.getTaskId());
		            	    		console.addToPausedTasks(taskitem.getTaskId());
		            	    		console.persistState();
		            	    	} catch (MeasurementError e) {
									Logger.e(e.toString());
						            Toast.makeText(MeasurementScheduleConsoleActivity.this, R.string.cancelUserMeasurementFailureToast,
						              Toast.LENGTH_LONG).show();
								}
		            	    }else{
		            	    	//creating another task
		            	    	console.removeFromPausedTasks(taskitem.getTaskId());
		            	    	console.persistState();
		            	    	String taskDesc=taskitem.getDescription();
		            	    	MeasurementTask newTask = null;
		            	    	int measurementType = -1;
		            	        Map<String, String> params = new HashMap<String, String>();
		            	    	if(taskDesc.startsWith(TracerouteTask.TYPE)){
		            	    		measurementType=API.Traceroute;
		            	    		params.put("target", taskDesc.substring(taskDesc.indexOf(',')+1));
		            			}else if(taskDesc.startsWith(PingTask.TYPE)){
		            				measurementType=API.Ping;
		            				params.put("target", taskDesc.substring(taskDesc.indexOf(',')+1));
		            			}else if(taskDesc.startsWith(DnsLookupTask.TYPE)){
		            				measurementType=API.DNSLookup;
		            				params.put("target", taskDesc.substring(taskDesc.indexOf(',')+1));
		            			}else if(taskDesc.startsWith(HttpTask.TYPE)){
		            				measurementType=API.HTTP;
		            				params.put("url", taskDesc.substring(taskDesc.indexOf(',')+1));
		            				params.put("method", "get");
		            			}else if(taskDesc.startsWith(UDPBurstTask.TYPE)){
		            				measurementType=API.UDPBurst;
		            				params.put("target", MLabNS.TARGET);
		            		        params.put("direction", taskDesc.substring(taskDesc.indexOf(',')+1));
		            			}else if(taskDesc.startsWith(TCPThroughputTask.TYPE)){
		            				measurementType=API.TCPThroughput;
		            				params.put("target", MLabNS.TARGET);
		            	            params.put("dir_up", taskDesc.substring(taskDesc.indexOf(',')+1));
		            			}
		            	    	newTask = api.createTask(measurementType,
		            	    	          Calendar.getInstance().getTime(),
		            	    	          null,
		            	    	          MobiperfConfig.DEFAULT_USER_MEASUREMENT_INTERVAL_SEC,
		            	    	          MobiperfConfig.DEFAULT_USER_MEASUREMENT_COUNT,
		            	    	          API.USER_PRIORITY,
		            	    	          MobiperfConfig.DEFAULT_CONTEXT_INTERVAL,
		            	    	          params);

		            	    	if (newTask != null) {
		            	    		try {
		            	    			MeasurementScheduleConsoleActivity.this.api.addTask(newTask);
		            	    		} catch (MeasurementError e) {
		            	    			Logger.e(e.toString());
		            	    			Toast.makeText(MeasurementScheduleConsoleActivity.this, R.string.userMeasurementFailureToast,
		            	    					Toast.LENGTH_LONG).show();
		            	    		}
		            	    	}
		            	    	console.removeUserTask(taskitem.getTaskId());
		            	    	console.addUserTask(newTask.getTaskId(), taskDesc);
		            	    	console.persistState();
		            	    	
		            	    	
		            	    	
		            	    	
		            	    }
		             }

					public 	OnClickListener init(TaskItem ti) {
						taskitem=ti;
						return this;
					}
		         }.init(item));
				Button cancelButton=(Button)(v.findViewById(R.id.cancelbutton));
				cancelButton.setOnClickListener(new View.OnClickListener() {
					private TaskItem taskitem;
					 public void onClick(View v) {
						 try {
							MeasurementScheduleConsoleActivity.this.api.cancelTask(taskitem.getTaskId());
							console.removeUserTask(taskitem.getTaskId());
							console.persistState();
							TaskItemAdapter.this.remove(taskitem);
							TaskItemAdapter.this.notifyDataSetChanged();
							
						} catch (MeasurementError e) {
							Logger.e(e.toString());
				            Toast.makeText(MeasurementScheduleConsoleActivity.this, R.string.cancelUserMeasurementFailureToast,
				              Toast.LENGTH_LONG).show();
						}
					 }

					public 	OnClickListener init(TaskItem ti) {
						taskitem=ti;
						return this;
					}
		         }.init(item));
				TextView text= (TextView) (v.findViewById(R.id.taskdesc));
				text.setText(item.toString());
				

			}

			return v;
		}
	}

	class TaskItem{
		private String description;
		private String taskId;
		public void setTaskId(String id){
			this.taskId=id;
		}
		public String getTaskId(){
			return this.taskId;
		}
		public void setDescription(String desc){
			this.description=desc;
		}
		public String getDescription(){
			return this.description;
		}
		public TaskItem(){

		}
		public TaskItem(String taskId, String desc){
			this.description=desc;
			this.taskId=taskId;
		}
		@Override
		public String toString() {
			String result="";
			if(description.startsWith(TracerouteTask.TYPE)){
				result+="["+TracerouteTask.TYPE+"]\ntarget: "+description.substring(description.indexOf(',')+1);
			}else if(description.startsWith(PingTask.TYPE)){
				result+="["+PingTask.TYPE+"]\ntarget: "+description.substring(description.indexOf(',')+1);
			}else if(description.startsWith(DnsLookupTask.TYPE)){
				result+="["+DnsLookupTask.TYPE+"]\ntarget: "+description.substring(description.indexOf(',')+1);
			}else if(description.startsWith(HttpTask.TYPE)){
				result+="["+HttpTask.TYPE+"]\ntarget: "+description.substring(description.indexOf(',')+1);
			}else if(description.startsWith(UDPBurstTask.TYPE)){
				result+="["+UDPBurstTask.TYPE+"]\ndirection: "+description.substring(description.indexOf(',')+1);
			}else if(description.startsWith(TCPThroughputTask.TYPE)){
				result+="["+TCPThroughputTask.TYPE+"]\ndirection: "+description.substring(description.indexOf(',')+1);
			}
			return result;
		}
	}
	
	
	private synchronized void updateTasksFromConsole(){
		if (console != null) {
			taskItems.clear();
			final List<String> user_tasks=console.getUserTasks();
			for(String taskStr: user_tasks){
				String taskId=taskStr.substring(0, taskStr.indexOf(','));
				String taskDesc=taskStr.substring(taskStr.indexOf(',')+1);
				taskItems.add(new TaskItem(taskId,taskDesc));
			}
			runOnUiThread(new Runnable() {
		        public void run() { adapter.notifyDataSetChanged(); }
		      });
			
			
		}
	}

	//  /**
	//   * Handles the deletion of the measurement tasks when the user clicks the context menu
	//   */
	//  /**
	//   * TODO(Hongyi): Currently the server scheduled task doesn't have client key,
	//   * so it cannot be cancelled by any client.
	//   * Shall we allow user to cancel server task by cancelTask(taskId, null)?
	//   */
	//  @Override
	//  public boolean onContextItemSelected(MenuItem item) {
	//    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	//    switch (item.getItemId()) {
	//      case R.id.ctxMenuDeleteTask:
	//        scheduler = parent.getScheduler();
	//        if (scheduler != null) {
	//          String selectedTaskString = consoleContent.getItem(longClickedItemPosition);
	//          String taskKey = taskMap.get(selectedTaskString);
	//          if (taskKey != null) {
	//            scheduler.removeTaskByKey(taskKey);
	//          }
	//        }
	//        updateConsole();
	//        return true;
	//      default:
	//    }
	//    return false;
	//  }
	//
	//  /**
	//   * TODO(Hongyi): Better to track last check-in time in this class rather
	//   * than in scheduler.
	//   */
	//  private void updateLastCheckinTime() {
	//    Logger.i("updateLastCheckinTime() called");
	//    scheduler = parent.getScheduler();
	//    if (scheduler != null) {
	//      Date lastCheckin = scheduler.getLastCheckinTime();
	//      if (lastCheckin != null) {
	//        lastCheckinTimeText.setText("Last checkin " + lastCheckin);
	//      } else {
	//        lastCheckinTimeText.setText("No checkins yet");
	//      }
	//    }
	//  }
	//
	//  /**
	//   * TODO(Hongyi): If we return check-in task list by intent, we don't need
	//   * those function in scheduler
	//   */
	//  private void updateConsole() {
	//    Logger.i("updateConsole() called");
	//    scheduler = parent.getScheduler();
	//    if (scheduler != null) {
	//      AbstractCollection<MeasurementTask> tasks = scheduler.getTaskQueue();
	//      consoleContent.clear();
	//      taskMap.clear();
	//      for (MeasurementTask task : tasks) {
	//        String taskStr = task.toString();
	//        consoleContent.add(taskStr);
	//        taskMap.put(taskStr, task.getDescription().key);
	//      }
	//    }
	//    updateLastCheckinTime();
	//  }
	//
	//  /**
	//   * TODO(Hongyi): shall we expose the forced check-in function in API?
	//   */
	//  private void doCheckin() {
	//    Logger.i("doCheckin() called");
	//    scheduler = parent.getScheduler();
	//    if (scheduler != null) {
	//      lastCheckinTimeText.setText("Checking in...");
	//      scheduler.handleCheckin(true);
	//    }
	//  }
	//  
}
