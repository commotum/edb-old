/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cache$report_val_fn_fail
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__1 = RT.keyword(null, (String)"key");
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"describe-bbuf");
    public static final Keyword const__3 = RT.keyword(null, (String)"class");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object t, Object raw, Object k) {
        Object object;
        Object or__5238__auto__9392;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object[] objectArray = new Object[2];
        objectArray[0] = const__1;
        Object object2 = k;
        k = null;
        objectArray[1] = object2;
        IPersistentMap iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        Object object3 = or__5238__auto__9392 = ((IFn)const__2.getRawRoot()).invoke(raw);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__9392;
            or__5238__auto__9392 = null;
        } else {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__3;
            Object object4 = raw;
            raw = null;
            objectArray2[1] = ((IFn)const__4.getRawRoot()).invoke(object4);
            object = RT.mapUniqueKeys((Object[])objectArray2);
        }
        Object desc = iFn.invoke((Object)iPersistentMap, object);
        Object object5 = ((IFn)const__6.getRawRoot()).invoke((Object)"Unable to convert data: ", desc);
        Object object6 = desc;
        desc = null;
        Object object7 = t;
        t = null;
        throw (Throwable)((IFn)const__5.getRawRoot()).invoke(object5, object6, object7);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cache$report_val_fn_fail.invokeStatic(object4, object5, object6);
    }
}

