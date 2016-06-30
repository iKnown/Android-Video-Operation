package com.iknow.android.videooperation.utils;

import android.content.Context;

/**
 * @Author: J.Chou
 * @Email: who_know_me@163.com
 * @Created: 2016年05月20日 9:23 AM
 * @Description:
 */

public class DeviceHelper {

    private static Context mContext;
    public static void init(Context context) {
        mContext = context;
    }

    public static int dip2px(float dipValue){
        final float scale =mContext.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}
