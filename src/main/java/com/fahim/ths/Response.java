package com.fahim.ths;

import java.util.Map;

public class Response {
    public boolean ok;
    public String error;
    public Map<String, Object> data;

    public static Response ok(Map<String,Object> data){
        Response r=new Response(); r.ok=true; r.data=data; return r;
    }
    public static Response err(String msg){
        Response r=new Response(); r.ok=false; r.error=msg; return r;
    }
}
