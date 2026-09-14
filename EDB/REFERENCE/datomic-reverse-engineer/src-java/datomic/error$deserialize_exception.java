/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.Exceptions;

public final class error$deserialize_exception
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"classname");
    public static final Keyword const__4 = RT.keyword(null, (String)"error");
    public static final Keyword const__5 = RT.keyword(null, (String)"error-data");
    public static final String const__6 = "datomic.impl.Exceptions$IllegalStateExceptionInfo";
    public static final String const__7 = "datomic.impl.Exceptions$IllegalArgumentExceptionInfo";
    public static final String const__8 = "clojure.lang.ExceptionInfo";
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"exception-deserializer");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object p__699) {
        Object object;
        Object error_data;
        Object object2;
        Object object3 = p__699;
        p__699 = null;
        Object map__700 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__700);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__700;
            map__700 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__700;
            map__700 = null;
        }
        Object map__7002 = object2;
        Object classname = RT.get((Object)map__7002, (Object)const__3);
        Object error2 = RT.get((Object)map__7002, (Object)const__4);
        Object object6 = map__7002;
        map__7002 = null;
        Object object7 = error_data = RT.get((Object)object6, (Object)const__5);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = classname;
            classname = null;
            Object G__701 = object8;
            switch (Util.hash((Object)G__701) >> 1 & 3) {
                case 0: {
                    if (!Util.equiv((Object)G__701, (Object)const__6)) break;
                    error2 = null;
                    error_data = null;
                    object = new Exceptions.IllegalStateExceptionInfo((String)error2, (IPersistentMap)error_data);
                    return object;
                }
                case 1: {
                    if (!Util.equiv((Object)G__701, (Object)const__7)) break;
                    error2 = null;
                    error_data = null;
                    object = new Exceptions.IllegalArgumentExceptionInfo((String)error2, (IPersistentMap)error_data);
                    return object;
                }
                case 3: {
                    if (!Util.equiv((Object)G__701, (Object)const__8)) break;
                    Object object9 = error2;
                    error2 = null;
                    Object object10 = error_data;
                    error_data = null;
                    object = ((IFn)const__9.getRawRoot()).invoke(object9, object10);
                    return object;
                }
            }
            error2 = null;
            object = new RuntimeException((String)error2);
            return object;
        }
        Object object11 = classname;
        classname = null;
        Object object12 = error2;
        error2 = null;
        object = ((IFn)((IFn)const__10.getRawRoot()).invoke(object11)).invoke(object12);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$deserialize_exception.invokeStatic(object2);
    }
}

