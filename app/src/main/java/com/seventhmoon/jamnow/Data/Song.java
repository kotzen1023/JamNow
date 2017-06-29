package com.seventhmoon.jamnow.Data;

import android.graphics.Bitmap;

public class Song {
	private String name;
	private String path;
	private byte channel;
	private int sample_rate;
	private int duration;
	private long duration_u;
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

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public int getSample_rate() {
		return sample_rate;
	}

	public void setSample_rate(int sample_rate) {
		this.sample_rate = sample_rate;
	}

	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public long getDuration_u() {
		return duration_u;
	}

	public void setDuration_u(long duration_u) {
		this.duration_u = duration_u;
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
