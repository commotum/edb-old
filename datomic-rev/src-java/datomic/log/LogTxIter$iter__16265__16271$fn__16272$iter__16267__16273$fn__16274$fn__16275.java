/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;

public final class LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274$fn__16275
extends AFunction {
    Object b__16270;
    int size__6024__auto__;
    Object c__6023__auto__;
    Object lookup;
    Object dir;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"getx");

    public LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274$fn__16275(Object object, int n, Object object2, Object object3, Object object4) {
        this.b__16270 = object;
        this.size__6024__auto__ = n;
        this.c__6023__auto__ = object2;
        this.lookup = object3;
        this.dir = object4;
    }

    public Object invoke() {
        for (long i__16269 = (long)RT.intCast((long)0L); i__16269 < (long)this.size__6024__auto__; ++i__16269) {
            Object di;
            Object object = di = ((Indexed)this.c__6023__auto__).nth(RT.intCast((long)i__16269));
            di = null;
            ((IFn)const__3.getRawRoot()).invoke(this.b__16270, ((IFn)const__4.getRawRoot()).invoke(this.lookup, ((LogDir)RT.nth((Object)this.dir, (int)RT.intCast((Object)((Number)object)))).uuid));
        }
        return Boolean.TRUE;
    }
}

