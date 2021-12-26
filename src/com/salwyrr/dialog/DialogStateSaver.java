package com.salwyrr.dialog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DialogStateSaver {

    public final File file;
    private final Properties properties;

    public DialogStateSaver(File file) throws IOException {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();

        this.file = file;
        this.properties = new Properties();
        this.load();
    }

    public void set(String key, String value) {
        this.properties.setProperty(key, value);
        this.save();
    }

    public String get(String key) {
        return this.properties.getProperty(key);
    }

    public String get(String key, String def) {
        String value = this.properties.getProperty(key);
        return value == null ? def : value;
    }

    public void remove(String key) {
        this.properties.remove(key);
        this.save();
    }

    public void save() {
        try {
            this.properties.store(new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8), "Dialog states");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void load() {
        try {
            this.properties.load(new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void clear() {
        this.properties.clear();
    }
}
