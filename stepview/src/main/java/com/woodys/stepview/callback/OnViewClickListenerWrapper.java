package com.woodys.stepview.callback;

import android.view.MotionEvent;

/**
 * Created by woodys on 2018/3/14.
 */

public interface OnViewClickListenerWrapper {
    void onClick(MotionEvent ev, int index);
}
