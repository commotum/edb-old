/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class coordination$create_heartbeat
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"alt-host");
    public static final Keyword const__5 = RT.keyword(null, (String)"port");
    public static final Keyword const__6 = RT.keyword(null, (String)"username");
    public static final Keyword const__7 = RT.keyword(null, (String)"password");
    public static final Keyword const__8 = RT.keyword(null, (String)"version");
    public static final Keyword const__9 = RT.keyword(null, (String)"encrypt-channel");
    public static final Keyword const__10 = RT.keyword(null, (String)"timestamp");
    public static final Object const__11 = 2L;

    public static Object invokeStatic(Object p__11682) {
        Object object;
        Object object2 = p__11682;
        p__11682 = null;
        Object map__11683 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__11683);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__11683;
            map__11683 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__11683;
            map__11683 = null;
        }
        Object map__116832 = object;
        Object host = RT.get((Object)map__116832, (Object)const__3);
        Object alt_host = RT.get((Object)map__116832, (Object)const__4);
        Object port = RT.get((Object)map__116832, (Object)const__5);
        Object username = RT.get((Object)map__116832, (Object)const__6);
        Object password = RT.get((Object)map__116832, (Object)const__7);
        Object version2 = RT.get((Object)map__116832, (Object)const__8);
        Object encrypt_channel = RT.get((Object)map__116832, (Object)const__9);
        Object object5 = map__116832;
        map__116832 = null;
        Object timestamp = RT.get((Object)object5, (Object)const__10);
        Object[] objectArray = new Object[9];
        Object object6 = host;
        host = null;
        objectArray[0] = object6;
        Object object7 = alt_host;
        alt_host = null;
        objectArray[1] = object7;
        Object object8 = port;
        port = null;
        objectArray[2] = object8;
        Object object9 = username;
        username = null;
        objectArray[3] = object9;
        Object object10 = password;
        password = null;
        objectArray[4] = object10;
        Object object11 = timestamp;
        timestamp = null;
        objectArray[5] = object11;
        Object object12 = version2;
        version2 = null;
        objectArray[6] = object12;
        Object object13 = encrypt_channel;
        encrypt_channel = null;
        objectArray[7] = object13;
        objectArray[8] = const__11;
        return RT.vector((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$create_heartbeat.invokeStatic(object2);
    }
}

