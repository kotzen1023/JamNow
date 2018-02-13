package com.seventhmoon.jamnow.Data;



public class RemoteServerItem {
    private String filename;
    private String name;
    private String urlAddress;
    private String port;
    private String authName;
    private String password;
    private boolean selected;

    public RemoteServerItem (String filename, String name, String urlAddress, String port, String authName, String password) {
        this.filename = filename;
        this.name = name;
        this.urlAddress = urlAddress;
        this.port = port;
        this.authName = authName;
        this.password = password;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlAddress() {
        return urlAddress;
    }

    public void setUrlAddress(String urlAddress) {
        this.urlAddress = urlAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}


