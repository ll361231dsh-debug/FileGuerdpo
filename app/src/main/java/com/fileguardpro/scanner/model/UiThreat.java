package com.fileguardpro.scanner.model;

public class UiThreat {
    private String name;
    private String level;
    private String path;

    public UiThreat(String name, String level, String path) {
        this.name = name;
        this.level = level;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getPath() {
        return path;
    }
}
