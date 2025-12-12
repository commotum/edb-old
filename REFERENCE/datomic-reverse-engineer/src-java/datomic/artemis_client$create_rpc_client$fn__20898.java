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

public final class artemis_client$create_rpc_client$fn__20898
extends AFunction {
    Object deserializer;
    Object response_map;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Var const__4 = RT.var((String)"datomic.cache", (String)"remove");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deliver");

    public artemis_client$create_rpc_client$fn__20898(Object object, Object object2) {
        this.deserializer = object;
        this.response_map = object2;
    }

    public Object invoke(Object msg) {
        Object object;
        Object temp__5457__auto__20901;
        Object id;
        Object map__20899;
        Object object2;
        Object object3 = msg;
        msg = null;
        Object map__208992 = ((IFn)this_.deserializer).invoke(object3);
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__208992);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__208992;
            map__208992 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__208992;
            map__208992 = null;
        }
        Object result2 = map__20899 = object2;
        Object object6 = map__20899;
        map__20899 = null;
        Object object7 = id = RT.get((Object)object6, (Object)const__3);
        id = null;
        Object object8 = temp__5457__auto__20901 = ((IFn)const__4.getRawRoot()).invoke(this_.response_map, object7);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object p;
            Object object9 = temp__5457__auto__20901;
            temp__5457__auto__20901 = null;
            Object object10 = p = object9;
            p = null;
            Object object11 = result2;
            result2 = null;
            artemis_client$create_rpc_client$fn__20898 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object10, object11);
        } else {
            object = null;
        }
        return object;
    }
}

