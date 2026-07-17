package com.example.tpdemo.utils

import android.app.Activity
import android.text.TextUtils

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc:
 */
object ActivityManager {

    val allActivities: MutableList<Activity> = ArrayList<Activity>()

    /**
     * 添加activity实例
     */
    fun addActivity(activity: Activity?) {
        allActivities.add(activity!!)
    }

    /**
     * 移除activity实例
     */
    fun removeActivity(activity: Activity?) {
        allActivities.remove(activity!!)
    }

    /**
     * 快速移除activity实例
     */
    fun removeActivityImd(activity: Activity) {
        activity.finish()
        allActivities.remove(activity)
    }

    fun removeAllActivities() {
        for (activity in this.allActivities) {
            activity.finish()
        }
    }

    fun clearActivity() {
        for (activity in this.allActivities
        ) {
            if (!activity.isFinishing()) {
                activity.finish()
            }
        }
    }

    val curActivity: Activity?
        /**
         * 获取当前的activity(顶层)
         */
        get() {
            if (this.allActivities == null || allActivities.size == 0) {
                return null
            }
            return allActivities.get(allActivities.size - 1)
        }

    /**
     * 通过activity name查找activity
     */
    fun getActivity(clazz: Class<*>): Activity? {
        if (this.allActivities == null || allActivities.size == 0) {
            return null
        }
        for (activity in this.allActivities) {
            if (TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                return activity
            }
        }
        return null
    }

    /**
     * 移除XXX activity
     */
    fun removeActivity(clazz: Class<*>) {
        for (activity in this.allActivities) {
            if (TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                activity.finish()
            }
        }
    }

    /**
     * 移除所有的activity，除了XXX
     */
    fun removeActivitiesExcept(clazz: Class<*>) {
        for (activity in this.allActivities) {
            if (!TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                activity.finish()
            }
        }
    }

    fun removeActivitiesExcept(clazz: MutableList<Class<*>>) {
        for (activity in this.allActivities) {
            var inExcept = false
            for (cls in clazz) {
                if (TextUtils.equals(activity.javaClass.getName(), cls.getName())) {
                    inExcept = true
                    break
                }
            }
            if (!inExcept) {
                activity.finish()
            }
        }
    }

    /**
     * 移除clazz栈顶的activity列表
     * 如当前栈的情况是：A-B-C-D,假设A是栈底，D是栈顶，如果传入的clazz是C,那么执行之后，栈内情况是：A-B
     */
    fun removeTopActivities(clazz: Class<*>) {
        //如果不包含对应的activity
        if (!contain(clazz)) {
            return
        }

        for (i in allActivities.indices.reversed()) {
            val activity = allActivities.get(i)
            if (TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                activity.finish()
                break
            } else {
                activity.finish()
            }
        }
    }

    /**
     * 移除clazz栈顶的activity列表
     * 如当前栈的情况是：A-B-C-D,假设A是栈底，D是栈顶，如果传入的clazz是B,那么执行之后，栈内情况是：A-B
     */
    fun removeTopActivitiesExcept(clazz: Class<*>) {
        //如果不包含对应的activity
        if (!contain(clazz)) {
            return
        }

        for (i in allActivities.indices.reversed()) {
            val activity = allActivities.get(i)
            if (TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                break
            } else {
                activity.finish()
            }
        }
    }

    /**
     * 栈内是否包含某个activity
     */
    fun contain(clazz: Class<*>): Boolean {
        for (activity in this.allActivities) {
            if (TextUtils.equals(activity.javaClass.getName(), clazz.getName())) {
                return true
            }
        }
        return false
    }

}
