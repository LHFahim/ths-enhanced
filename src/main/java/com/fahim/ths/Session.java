package com.fahim.ths;

import java.util.Map;

public class Session {
    private static Map<String,Object> currentUser;

    public static void setCurrentUser(Map<String,Object> user) {
        currentUser = user;
    }
    public static Map<String,Object> getCurrentUser() {
        return currentUser;
    }

    public static Integer currentUserId() {
        if (currentUser == null) return null;
        Object v = currentUser.get("id");
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch(Exception e){ return null; }
    }

    public static String currentUserRole() {
        return currentUser == null ? null : String.valueOf(currentUser.get("role"));
    }
}
