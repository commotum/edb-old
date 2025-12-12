/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.MapEquivalence
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.MapEquivalence;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;

public final class log$fn__16143$map__GT_LogDir__16163
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");

    public Object invoke(Object m__7585__auto__) {
        Object object;
        if (m__7585__auto__ instanceof MapEquivalence) {
            object = m__7585__auto__;
            m__7585__auto__ = null;
        } else {
            Object object2 = m__7585__auto__;
            m__7585__auto__ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object2);
        }
        log$fn__16143$map__GT_LogDir__16163 this_ = null;
        return LogDir.create((IPersistentMap)object);
    }
}

