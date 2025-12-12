/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$flatten_listy$listy_QMARK___19262;

public final class query$flatten_listy
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"complement");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"tree-seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object x) {
        query$flatten_listy$listy_QMARK___19262 listy_QMARK_ = new query$flatten_listy$listy_QMARK___19262();
        Object object = ((IFn)const__1.getRawRoot()).invoke((Object)listy_QMARK_);
        query$flatten_listy$listy_QMARK___19262 query$flatten_listy$listy_QMARK___19262 = listy_QMARK_;
        listy_QMARK_ = null;
        Object object2 = x;
        x = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)query$flatten_listy$listy_QMARK___19262, const__4.getRawRoot(), object2)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$flatten_listy.invokeStatic(object2);
    }
}

