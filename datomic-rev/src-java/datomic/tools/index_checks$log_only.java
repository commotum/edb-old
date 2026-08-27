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
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.tools.index_checks$log_only$indexed_QMARK___21918;

public final class index_checks$log_only
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.tools.index-checks", (String)"maybe-renamed?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.tools.index-checks", (String)"reporter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Keyword const__6 = RT.keyword(null, (String)"data");
    public static final Var const__7 = RT.var((String)"datomic.tools", (String)"tx-range-from-log");
    public static final Object const__8 = 0L;

    public static Object invokeStatic(Object cr, Object db2, Object progress) {
        index_checks$log_only$indexed_QMARK___21918 indexed_QMARK_ = new index_checks$log_only$indexed_QMARK___21918(db2);
        Object object = db2;
        db2 = null;
        index_checks$log_only$indexed_QMARK___21918 index_checks$log_only$indexed_QMARK___21918 = indexed_QMARK_;
        indexed_QMARK_ = null;
        Object object2 = progress;
        progress = null;
        Object object3 = cr;
        cr = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object), ((IFn)const__0.getRawRoot()).invoke((Object)index_checks$log_only$indexed_QMARK___21918, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object2), ((IFn)const__5.getRawRoot()).invoke((Object)const__6, ((IFn)const__7.getRawRoot()).invoke(object3, const__8, null)))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index_checks$log_only.invokeStatic(object4, object5, object6);
    }
}

