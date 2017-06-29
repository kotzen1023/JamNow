package com.seventhmoon.jamnow.Media;



public class MediaInfo_WAV {
    private String mime;
    private int durationUs;
    private int channel_count;
    private int channel_mask;
    private int sample_rate;

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public int getDurationUs() {
        return durationUs;
    }

    public void setDurationUs(int durationUs) {
        this.durationUs = durationUs;
    }

    public int getChannel_count() {
        return channel_count;
    }

    public void setChannel_count(int channel_count) {
        this.channel_count = channel_count;
    }

    public int getChannel_mask() {
        return channel_mask;
    }

    public void setChannel_mask(int channel_mask) {
        this.channel_mask = channel_mask;
    }

    public int getSample_rate() {
        return sample_rate;
    }

    public void setSample_rate(int sample_rate) {
        this.sample_rate = sample_rate;
    }
}
