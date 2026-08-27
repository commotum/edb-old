/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$write_vals$fn__15250;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$write_vals
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"index", (String)"write-vals");
    public static final Keyword const__3 = RT.keyword(null, (String)"count");
    public static final Keyword const__5 = RT.keyword(null, (String)"bytes");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__10 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__11 = RT.keyword(null, (String)"IndexWriteBatchCount");
    public static final Var const__12 = RT.var((String)"datomic.cluster", (String)"write-vals");
    public static final Keyword const__13 = RT.keyword(null, (String)"index");

    public static Object invokeStatic(Object cs, Object vmap) {
        Logger logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, RT.count((Object)vmap), const__5, ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke((Object)new index$write_vals$fn__15250(), ((IFn)const__9.getRawRoot()).invoke(vmap)))})));
        }
        ((IFn)const__10.getRawRoot()).invoke((Object)const__11, (Object)RT.count((Object)vmap));
        Object object = cs;
        cs = null;
        Object object2 = vmap;
        vmap = null;
        return ((IFn)const__12.getRawRoot()).invoke(object, (Object)const__13, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$write_vals.invokeStatic(object3, object4);
    }
}

