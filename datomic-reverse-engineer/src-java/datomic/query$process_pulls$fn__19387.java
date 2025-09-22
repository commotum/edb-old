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

public final class query$process_pulls$fn__19387
extends AFunction {
    Object pull_QMARK_;
    Object in;
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"normalize-pull");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__9 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__10 = RT.keyword((String)"db.error", (String)"pattern-not-bound");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__12 = RT.keyword(null, (String)"db");
    public static final Keyword const__13 = RT.keyword(null, (String)"var");
    public static final Keyword const__14 = RT.keyword(null, (String)"pattern");

    public query$process_pulls$fn__19387(Object object, Object object2) {
        this.pull_QMARK_ = object;
        this.in = object2;
    }

    public Object invoke(Object p1__19380_SHARP_) {
        Object object;
        Object object2 = ((IFn)this.pull_QMARK_).invoke(p1__19380_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Object and__5236__auto__19392;
            Object object4 = p1__19380_SHARP_;
            p1__19380_SHARP_ = null;
            Object vec__19388 = ((IFn)const__0.getRawRoot()).invoke(object4);
            RT.nth((Object)vec__19388, (int)RT.intCast((long)0L), null);
            Object db2 = RT.nth((Object)vec__19388, (int)RT.intCast((long)1L), null);
            Object var = RT.nth((Object)vec__19388, (int)RT.intCast((long)2L), null);
            Object object5 = vec__19388;
            vec__19388 = null;
            Object pattern = RT.nth((Object)object5, (int)RT.intCast((long)3L), null);
            Object object6 = and__5236__auto__19392 = ((IFn)const__6.getRawRoot()).invoke(pattern);
            if (object6 != null && object6 != Boolean.FALSE) {
                object3 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)RT.set((Object[])new Object[]{pattern}), this.in));
            } else {
                object3 = and__5236__auto__19392;
                and__5236__auto__19392 = null;
            }
            if (object3 != null && object3 != Boolean.FALSE) {
                ((IFn)const__9.getRawRoot()).invoke((Object)const__10, ((IFn)const__11.getRawRoot()).invoke((Object)"Pull pattern not found in inputs: ", pattern));
            }
            Object[] objectArray = new Object[6];
            objectArray[0] = const__12;
            Object object7 = db2;
            db2 = null;
            objectArray[1] = object7;
            objectArray[2] = const__13;
            Object object8 = var;
            var = null;
            objectArray[3] = object8;
            objectArray[4] = const__14;
            Object object9 = pattern;
            pattern = null;
            objectArray[5] = object9;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = p1__19380_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}

