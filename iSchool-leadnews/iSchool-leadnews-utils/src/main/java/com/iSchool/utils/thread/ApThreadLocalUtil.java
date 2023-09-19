package com.iSchool.utils.thread;

import com.iSchool.model.user.pojos.ApUser;

public class ApThreadLocalUtil {
    private final static ThreadLocal<ApUser> WM_THREAD_LOCAL=new ThreadLocal<>();

    //将用户存入线程
    public static void setUser(ApUser apUser){
        WM_THREAD_LOCAL.set(apUser);
    }

    //从线程中获取
    public static ApUser getUser(){
        return WM_THREAD_LOCAL.get();
    }

    //清理
    public static  void clear(){
        WM_THREAD_LOCAL.remove();
    }
}
