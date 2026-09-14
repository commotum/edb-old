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
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class tools$tools_fault
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__4 = RT.keyword((String)"cognitect.anomalies", (String)"fault");
    public static final Keyword const__5 = RT.keyword((String)"datomic.tools", (String)"cause");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__7 = RT.var((String)"datomic.tools", (String)"class-sym");
    public static final Var const__8 = RT.var((String)"datomic.tools", (String)"cause-chain");
    public static final Keyword const__9 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic(Object t) {
        IPersistentMap iPersistentMap;
        String temp__5457__auto__21812;
        Logger logger = LoggerFactory.getLogger((String)"datomic.tools");
        Object ex = t;
        if (logger.isInfoEnabled()) {
            logger.info((String)((IFn)const__0.getRawRoot()).invoke((Object)"Exception running tools"), (Throwable)ex);
            Logger logger2 = logger;
            logger = null;
            Object object = ex;
            ex = null;
            ((IFn)const__1.getRawRoot()).invoke((Object)logger2, object);
        }
        IFn iFn = (IFn)const__2.getRawRoot();
        IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])new Object[]{const__3, const__4, const__5, ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke(t))});
        Object object = t;
        t = null;
        String string = temp__5457__auto__21812 = ((Throwable)object).getMessage();
        if (string != null && string != Boolean.FALSE) {
            String string2 = temp__5457__auto__21812;
            temp__5457__auto__21812 = null;
            String msg = string2;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__9;
            String string3 = msg;
            msg = null;
            objectArray[1] = string3;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iFn.invoke((Object)iPersistentMap2, iPersistentMap);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$tools_fault.invokeStatic(object2);
    }
}

