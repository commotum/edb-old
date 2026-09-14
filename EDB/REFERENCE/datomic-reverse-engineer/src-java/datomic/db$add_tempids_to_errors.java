/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.Exceptions;

public final class db$add_tempids_to_errors
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-data");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"tempids");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__6 = RT.classForName((String)"datomic.impl.Exceptions$IllegalArgumentExceptionInfo");
    public static final Object const__7 = RT.classForName((String)"datomic.impl.Exceptions$IllegalStateExceptionInfo");
    public static final Keyword const__8 = RT.keyword(null, (String)"default");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");

    public static Object invokeStatic(Object f, Object get_tempids) {
        Object object;
        try {
            Object object2 = f;
            f = null;
            object = ((IFn)object2).invoke();
        }
        catch (Throwable t2) {
            Object temp__5455__auto__13976;
            Object object3;
            Object G__13974 = ((IFn)const__0.getRawRoot()).invoke((Object)t2);
            if (Util.identical((Object)G__13974, null)) {
                object3 = null;
            } else {
                Object object4 = G__13974;
                G__13974 = null;
                Object object5 = get_tempids;
                get_tempids = null;
                object3 = ((IFn)const__2.getRawRoot()).invoke(object4, (Object)const__3, ((IFn)object5).invoke());
            }
            Object object6 = temp__5455__auto__13976 = object3;
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = temp__5455__auto__13976;
                temp__5455__auto__13976 = null;
                Object data2 = object7;
                Object cls = ((IFn)const__4.getRawRoot()).invoke((Object)t2);
                String msg = t2.getMessage();
                if (Util.equiv((Object)cls, (Object)const__6)) {
                    String string = msg;
                    msg = null;
                    Object object8 = data2;
                    data2 = null;
                    Object t2 = null;
                    throw (Throwable)new Exceptions.IllegalArgumentExceptionInfo(string, (IPersistentMap)object8, t2);
                }
                Object object9 = cls;
                cls = null;
                if (Util.equiv((Object)object9, (Object)const__7)) {
                    String string = msg;
                    msg = null;
                    Object object10 = data2;
                    data2 = null;
                    Object t2 = null;
                    throw (Throwable)new Exceptions.IllegalStateExceptionInfo(string, (IPersistentMap)object10, t2);
                }
                Keyword keyword = const__8;
                if (keyword != null) {
                    if (keyword != Boolean.FALSE) {
                        String string = msg;
                        msg = null;
                        Object object11 = data2;
                        data2 = null;
                        Object t2 = null;
                        throw (Throwable)((IFn)const__9.getRawRoot()).invoke((Object)string, object11, (Object)t2);
                    }
                }
            } else {
                Object t2 = null;
                throw t2;
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$add_tempids_to_errors.invokeStatic(object3, object4);
    }
}

