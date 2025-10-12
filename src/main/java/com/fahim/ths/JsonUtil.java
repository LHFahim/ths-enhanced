package com.fahim.ths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    public static String toJson(Object o){ return GSON.toJson(o); }
    public static <T> T fromJson(String s, Class<T> c){ return GSON.fromJson(s, c); }
}
