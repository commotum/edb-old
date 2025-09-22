/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class peer$fn__21679
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"edition-has-feature?");
    public static final Keyword const__1 = RT.keyword((String)"monitor", (String)"metrics");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__3 = RT.var((String)"datomic.config", (String)"property");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.aws-monitor", (String)"cloudwatch-reporter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"datomic.process-monitor");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"resolve");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"datomic.process-monitor", (String)"start-metrics");
    public static final Var const__9 = RT.var((String)"datomic.cast2slf4j", (String)"redirect");
    public static final Var const__10 = RT.var((String)"datomic.domain", (String)"preload-extension-resolver!");

    public static Object invokeStatic() {
        Object object;
        Object object2;
        Object and__5236__auto__21681;
        Object object3 = and__5236__auto__21681 = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"datomic.metricsCallback"), (Object)const__4);
        } else {
            object2 = and__5236__auto__21681;
            Object var0 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__5.getRawRoot()).invoke((Object)const__6);
            ((IFn)((IFn)const__7.getRawRoot()).invoke((Object)const__8)).invoke();
            ((IFn)const__9.getRawRoot()).invoke();
            object = ((IFn)const__10.getRawRoot()).invoke();
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return peer$fn__21679.invokeStatic();
    }
}

