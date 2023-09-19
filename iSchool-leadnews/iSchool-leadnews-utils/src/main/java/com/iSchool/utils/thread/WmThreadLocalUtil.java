package com.iSchool.utils.thread;

import com.iSchool.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {
    private final static ThreadLocal<WmUser> WM_THREAD_LOCAL=new ThreadLocal<>();

    //将用户存入线程
    public static void setUser(WmUser wmUser){
        WM_THREAD_LOCAL.set(wmUser);
    }

    //从线程中获取
    public static WmUser getUser(){
        return WM_THREAD_LOCAL.get();
    }

    //清理
    public static  void clear(){
        WM_THREAD_LOCAL.remove();
    }
}
