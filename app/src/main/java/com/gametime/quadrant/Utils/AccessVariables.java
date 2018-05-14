package com.gametime.quadrant.Utils;

import android.os.Environment;

/**
 * Created by Akansh on 15-12-2017.
 */

public interface AccessVariables {
    public static String root = Environment.getExternalStorageDirectory()
            .toString()+"/Pictures/quadrant/images";
}
