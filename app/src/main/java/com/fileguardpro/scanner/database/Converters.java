package com.fileguardpro.scanner.database;

import androidx.room.TypeConverter;

import com.fileguardpro.scanner.model.Threat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromThreatList(List<Threat> threats) {
        if (threats == null) return null;
        return gson.toJson(threats);
    }

    @TypeConverter
    public static List<Threat> toThreatList(String json) {
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Threat>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
