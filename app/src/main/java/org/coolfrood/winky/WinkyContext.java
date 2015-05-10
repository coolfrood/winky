package org.coolfrood.winky;

import android.content.Context;

/**
 * Created by akshat on 5/9/15.
 */
public class WinkyContext
{
    private static WinkApi api = null;

    static WinkApi getApi(Context ctx) {
        if (api == null) {
            api = new WinkApi(ctx);
        }
        return api;
    }
}
