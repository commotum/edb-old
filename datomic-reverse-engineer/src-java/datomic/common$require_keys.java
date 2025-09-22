/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$require_keys
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__4 = RT.keyword(null, (String)"missing");
    public static final Var const__5 = RT.var((String)"clojure.set", (String)"difference");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object m, Object keyseq) {
        Object object = m;
        m = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(object, keyseq);
        if ((long)RT.count((Object)result2) != (long)RT.count((Object)keyseq)) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object2 = keyseq;
            keyseq = null;
            Object object3 = result2;
            result2 = null;
            objectArray[1] = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, object2), ((IFn)const__6.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__7.getRawRoot()).invoke(object3)));
            throw (Throwable)((IFn)const__3.getRawRoot()).invoke((Object)"Missing keys", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        Object object4 = result2;
        result2 = null;
        return object4;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$require_keys.invokeStatic(object3, object4);
    }
}

