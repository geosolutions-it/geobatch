package it.geosolutions.geobatch.flow.event.consumer.file;

import it.geosolutions.geobatch.flow.event.consumer.EventConsumerDetails;

import java.util.ArrayList;
import java.util.List;

public class FileBasedEventConsumerDetails implements EventConsumerDetails {

	public static class Action {
		private String name;
		
		private List<Progress> progressList = new ArrayList<Progress>();
		
		public Action(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public void addProgress(Progress progress) {
			progressList.add(progress);
		}
		
		public Iterable<Progress> getProgress() {
			return progressList;
		}

	}

	public static class Progress {
		private String task;
		private float progress;
		public Progress(String task, float progress) {
			this.task = task;
			this.progress = progress;
		}
		public String getTask() {
			return task;
		}
		public float getProgress() {
			return progress;
		}
		
	}
	private List<String> eventsList = new ArrayList<String>();
	private List<Progress> progressList = new ArrayList<Progress>();
	private List<Action> actionsList = new ArrayList<Action>();
	
	public void addEvent(String name) {
		eventsList.add(name);
	}
	public void addProgress(Progress progress) {
		progressList.add(progress);
	}
	
	public Iterable<String> getEvents() {
		return eventsList;
	}

	public Iterable<Progress> getProgress() {
		return progressList;
	}
	
	public void addAction(
			Action action) {
		actionsList.add(action);
	}
	
	public Iterable<Action> getActions() {
		return actionsList;
	}
}
