/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;
import datomic.log.LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273;

public final class LogTxIter$iter__16265__16271$fn__16272
extends AFunction {
    long didx;
    Object s__16266;
    Object root_val;
    Object iter__16265;
    Object lookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"rest");

    public LogTxIter$iter__16265__16271$fn__16272(long l, Object object, Object object2, Object object3, Object object4) {
        this.didx = l;
        this.s__16266 = object;
        this.root_val = object2;
        this.iter__16265 = object3;
        this.lookup = object4;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__16266 = this_.s__16266 = null;
            while (true) {
                Object fs__6022__auto__16284;
                LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273 iterys__6021__auto__16283;
                Object ri;
                Object xs__6012__auto__16285;
                Object temp__5457__auto__16286;
                Object object2 = temp__5457__auto__16286 = ((IFn)const__0.getRawRoot()).invoke(s__16266);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__16286;
                temp__5457__auto__16286 = null;
                Object object4 = xs__6012__auto__16285 = object3;
                xs__6012__auto__16285 = null;
                Object object5 = ri = ((IFn)const__1.getRawRoot()).invoke(object4);
                ri = null;
                Object dir = ((IFn)const__2.getRawRoot()).invoke(this_.lookup, ((LogDir)RT.nth((Object)this_.root_val, (int)RT.intCast((Object)((Number)object5)))).uuid);
                LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273 logTxIter$iter__16265__16271$fn__16272$iter__16267__16273 = iterys__6021__auto__16283 = new LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273(this_.lookup, dir);
                iterys__6021__auto__16283 = null;
                Object object6 = dir;
                dir = null;
                Object object7 = fs__6022__auto__16284 = ((IFn)const__0.getRawRoot()).invoke(((IFn)logTxIter$iter__16265__16271$fn__16272$iter__16267__16273).invoke(((IFn)const__4.getRawRoot()).invoke((Object)Numbers.num((long)this_.didx), (Object)RT.count((Object)object6))));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = fs__6022__auto__16284;
                    fs__6022__auto__16284 = null;
                    Object object9 = s__16266;
                    s__16266 = null;
                    LogTxIter$iter__16265__16271$fn__16272 this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(object8, ((IFn)this_.iter__16265).invoke(((IFn)const__7.getRawRoot()).invoke(object9)));
                    break block2;
                }
                Object object10 = s__16266;
                s__16266 = null;
                s__16266 = ((IFn)const__7.getRawRoot()).invoke(object10);
            }
            object = null;
        }
        return object;
    }
}

