/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;

public final class coordination$heartbeat__GT_endpoint
extends AFunction {
    public static final Object const__2 = 1L;
    public static final Keyword const__10 = RT.keyword(null, (String)"alt-host");
    public static final Keyword const__11 = RT.keyword(null, (String)"peer-version");
    public static final Keyword const__12 = RT.keyword(null, (String)"password");
    public static final Keyword const__13 = RT.keyword(null, (String)"username");
    public static final Keyword const__14 = RT.keyword(null, (String)"port");
    public static final Keyword const__15 = RT.keyword(null, (String)"host");
    public static final Keyword const__16 = RT.keyword(null, (String)"version");
    public static final Keyword const__17 = RT.keyword(null, (String)"timestamp");
    public static final Keyword const__18 = RT.keyword(null, (String)"encrypt-channel");

    public static Object invokeStatic(Object p__11685) {
        Object object;
        Object object2;
        Object or__5238__auto__11690;
        Object object3 = p__11685;
        p__11685 = null;
        Object vec__11686 = object3;
        Object host = RT.nth((Object)vec__11686, (int)RT.intCast((long)0L), null);
        Object alt_host = RT.nth((Object)vec__11686, (int)RT.intCast((long)1L), null);
        Object port = RT.nth((Object)vec__11686, (int)RT.intCast((long)2L), null);
        Object username = RT.nth((Object)vec__11686, (int)RT.intCast((long)3L), null);
        Object password = RT.nth((Object)vec__11686, (int)RT.intCast((long)4L), null);
        Object timestamp = RT.nth((Object)vec__11686, (int)RT.intCast((long)5L), null);
        Object version2 = RT.nth((Object)vec__11686, (int)RT.intCast((long)6L), null);
        Object encrypt_channel = RT.nth((Object)vec__11686, (int)RT.intCast((long)7L), null);
        Object object4 = vec__11686;
        vec__11686 = null;
        Object peer_version = RT.nth((Object)object4, (int)RT.intCast((long)8L), null);
        Object[] objectArray = new Object[18];
        objectArray[0] = const__10;
        Object object5 = alt_host;
        alt_host = null;
        objectArray[1] = object5;
        objectArray[2] = const__11;
        Object object6 = peer_version;
        peer_version = null;
        Object object7 = or__5238__auto__11690 = object6;
        if (object7 != null && object7 != Boolean.FALSE) {
            object2 = or__5238__auto__11690;
            or__5238__auto__11690 = null;
        } else {
            object2 = const__2;
        }
        objectArray[3] = object2;
        objectArray[4] = const__12;
        Object object8 = password;
        password = null;
        objectArray[5] = object8;
        objectArray[6] = const__13;
        Object object9 = username;
        username = null;
        objectArray[7] = object9;
        objectArray[8] = const__14;
        Object object10 = port;
        port = null;
        objectArray[9] = object10;
        objectArray[10] = const__15;
        Object object11 = host;
        host = null;
        objectArray[11] = object11;
        objectArray[12] = const__16;
        objectArray[13] = version2;
        objectArray[14] = const__17;
        Object object12 = timestamp;
        timestamp = null;
        objectArray[15] = object12;
        objectArray[16] = const__18;
        Object object13 = version2;
        version2 = null;
        if (Util.identical((Object)object13, null)) {
            object = Boolean.TRUE;
        } else {
            object = encrypt_channel;
            encrypt_channel = null;
        }
        objectArray[17] = object;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$heartbeat__GT_endpoint.invokeStatic(object2);
    }
}

