/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class connector$endpoint_error
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"alt-host");
    public static final Keyword const__5 = RT.keyword(null, (String)"port");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"redact");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"password")});

    public static Object invokeStatic(Object p__21140, Object cause) {
        Object object;
        Object map__21141;
        Object object2;
        Object object3 = p__21140;
        p__21140 = null;
        Object map__211412 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__211412);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__211412;
            map__211412 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__211412;
            map__211412 = null;
        }
        Object endpoint = map__21141 = object2;
        Object host = RT.get((Object)map__21141, (Object)const__3);
        Object alt_host = RT.get((Object)map__21141, (Object)const__4);
        Object object6 = map__21141;
        map__21141 = null;
        Object port = RT.get((Object)object6, (Object)const__5);
        IFn iFn = (IFn)const__6.getRawRoot();
        IFn iFn2 = (IFn)const__7.getRawRoot();
        Object object7 = host;
        host = null;
        Object object8 = alt_host;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = alt_host;
            alt_host = null;
            object = ((IFn)const__7.getRawRoot()).invoke((Object)" or ALT_HOST ", object9);
        } else {
            object = "";
        }
        Object object10 = port;
        port = null;
        Object object11 = endpoint;
        endpoint = null;
        Object object12 = cause;
        cause = null;
        return iFn.invoke(iFn2.invoke((Object)"Error communicating with HOST ", object7, object, (Object)" on PORT ", object10), ((IFn)const__8.getRawRoot()).invoke(object11, (Object)const__10), object12);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$endpoint_error.invokeStatic(object3, object4);
    }
}

