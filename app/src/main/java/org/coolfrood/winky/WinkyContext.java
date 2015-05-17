package org.coolfrood.winky;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
