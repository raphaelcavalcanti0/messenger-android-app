package com.rcfin.messenger.models;

public class Message implements Comparable<Message>{

    private String text;
    private long timestamp;
    private String fromId;
    private String toId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    @Override
    public int compareTo(Message o) {

        int comparando = compare(this.getTimestamp(), o.getTimestamp());
        return comparando != 0 ? comparando : compare(this.getTimestamp(), o.getTimestamp());
    }

    private static int compare(long a, long b) {
        return Long.compare(a, b);
    }
}
