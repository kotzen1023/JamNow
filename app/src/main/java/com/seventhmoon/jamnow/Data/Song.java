package com.seventhmoon.jamnow.Data;

public class Song {
	private String name;
	private String path;
	private int duration;
	private int mark_a;
	private int mark_b;
	private boolean selected;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getMark_a() {
		return mark_a;
	}

	public void setMark_a(int mark_a) {
		this.mark_a = mark_a;
	}

	public int getMark_b() {
		return mark_b;
	}

	public void setMark_b(int mark_b) {
		this.mark_b = mark_b;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
