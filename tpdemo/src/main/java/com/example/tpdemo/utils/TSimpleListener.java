package com.example.tpdemo.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Author: clement
 * Create: 2026/7/17
 * Desc:
 */
public class TSimpleListener<T> {
    public interface ICallback<T> {
        void onFireEvent(T listener, Object... objects);
    }

    private HashMap<T, Boolean> mListener;
    private HashSet<T> mRemoveSet;

    public synchronized int size() {
        return mListener == null ? 0 : mListener.size();
    }

    public synchronized void clear() {
        if (mListener != null) {
            mListener.clear();
        }
    }

    public synchronized void attachListener(T listener) {
        attachListener(listener, false);
    }

    public synchronized void attachListener(T listener, boolean autoDetach) {
        if (listener == null) {
            return;
        }

        if (mListener == null) {
            mListener = new HashMap<>();
        }

        mListener.put(listener, autoDetach);
    }

    public synchronized void detachListener(T listener) {
        if (mListener != null && listener != null) {
            if (mRemoveSet != null) {
                mRemoveSet.add(listener);
            } else {
                mListener.remove(listener);
            }
        }
    }

    public synchronized void fireEvent(ICallback<T> callback, Object...objects) {
        if (mListener == null || callback == null) {
            return;
        }

        mRemoveSet = new HashSet<>();

        for (Map.Entry<T, Boolean> entry : mListener.entrySet()) {
            T listener = entry.getKey();
            callback.onFireEvent(listener, objects);

            if (entry.getValue()) {
                mRemoveSet.add(listener);
            }
        }

        for (T listener : mRemoveSet) {
            mListener.remove(listener);
        }

        mRemoveSet.clear();
        mRemoveSet = null;
    }
}
