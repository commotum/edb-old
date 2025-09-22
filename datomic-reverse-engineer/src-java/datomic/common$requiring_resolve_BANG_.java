/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$requiring_resolve_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__7 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__8 = RT.keyword((String)"cognitect.anomalies", (String)"not-found");
    public static final Keyword const__9 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object x) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object msg;
            Object or__5238__auto__9132;
            Object object3;
            Object temp__5457__auto__9131;
            Object or__5238__auto__9133;
            Object object4 = or__5238__auto__9133 = ((IFn)const__1.getRawRoot()).invoke(x);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__9133;
                return object;
            }
            Object object5 = temp__5457__auto__9131 = ((IFn)const__2.getRawRoot()).invoke(x);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object nsname;
                Object object6 = temp__5457__auto__9131;
                temp__5457__auto__9131 = null;
                Object object7 = nsname = object6;
                nsname = null;
                ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object7));
                object3 = ((IFn)const__1.getRawRoot()).invoke(x);
            } else {
                object3 = null;
            }
            Object object8 = or__5238__auto__9132 = object3;
            if (object8 != null && object8 != Boolean.FALSE) {
                object = or__5238__auto__9132;
                return object;
            }
            Object object9 = x;
            x = null;
            Object object10 = msg = ((IFn)const__5.getRawRoot()).invoke((Object)"Can't resolve symbol: ", object9);
            Object[] objectArray = new Object[4];
            objectArray[0] = const__7;
            objectArray[1] = const__8;
            objectArray[2] = const__9;
            Object object11 = msg;
            msg = null;
            objectArray[3] = object11;
            throw (Throwable)((IFn)const__6.getRawRoot()).invoke(object10, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        object = x;
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$requiring_resolve_BANG_.invokeStatic(object2);
    }
}

