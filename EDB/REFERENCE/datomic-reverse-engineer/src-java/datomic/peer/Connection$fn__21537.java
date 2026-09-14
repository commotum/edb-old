/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class Connection$fn__21537
extends AFunction {
    Object lockee__5436__auto__;
    Object this;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"cache-keys");
    public static final Var const__2 = RT.var((String)"datomic.peer", (String)"connection-cache");
    public static final Var const__7 = RT.var((String)"datomic.cache", (String)"remove");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");

    public Connection$fn__21537(Object object, Object object2) {
        this.lockee__5436__auto__ = object;
        this.this = object2;
    }

    public Object invoke() {
        Object var10_8;
        try {
            synchronized (this.lockee__5436__auto__) {
                Object seq_21538 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()));
                Object chunk_21539 = null;
                long count_21540 = 0L;
                long i_21541 = 0L;
                while (true) {
                    Object temp__5457__auto__21544;
                    if (i_21541 < count_21540) {
                        Object k = ((Indexed)chunk_21539).nth(RT.intCast((long)i_21541));
                        if (Util.equiv((Object)this.this, (Object)RT.get((Object)const__2.getRawRoot(), (Object)k))) {
                            Object object = k;
                            k = null;
                            ((IFn)const__7.getRawRoot()).invoke(const__2.getRawRoot(), object);
                        }
                        Object object = seq_21538;
                        seq_21538 = null;
                        Object object2 = chunk_21539;
                        chunk_21539 = null;
                        ++i_21541;
                        chunk_21539 = object2;
                        seq_21538 = object;
                        continue;
                    }
                    Object object = seq_21538;
                    seq_21538 = null;
                    Object object3 = temp__5457__auto__21544 = ((IFn)const__0.getRawRoot()).invoke(object);
                    if (object3 == null || object3 == Boolean.FALSE) break;
                    Object object4 = temp__5457__auto__21544;
                    temp__5457__auto__21544 = null;
                    Object seq_215382 = object4;
                    Object object5 = ((IFn)const__9.getRawRoot()).invoke(seq_215382);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        Object c__5719__auto__21543 = ((IFn)const__10.getRawRoot()).invoke(seq_215382);
                        Object object6 = seq_215382;
                        seq_215382 = null;
                        Object object7 = c__5719__auto__21543;
                        Object object8 = c__5719__auto__21543;
                        c__5719__auto__21543 = null;
                        i_21541 = RT.intCast((long)0L);
                        count_21540 = RT.intCast((int)RT.count((Object)object8));
                        chunk_21539 = object7;
                        seq_21538 = ((IFn)const__11.getRawRoot()).invoke(object6);
                        continue;
                    }
                    Object k = ((IFn)const__14.getRawRoot()).invoke(seq_215382);
                    if (Util.equiv((Object)this.this, (Object)RT.get((Object)const__2.getRawRoot(), (Object)k))) {
                        Object object9 = k;
                        k = null;
                        ((IFn)const__7.getRawRoot()).invoke(const__2.getRawRoot(), object9);
                    }
                    Object object10 = seq_215382;
                    seq_215382 = null;
                    i_21541 = 0L;
                    count_21540 = 0L;
                    chunk_21539 = null;
                    seq_21538 = ((IFn)const__15.getRawRoot()).invoke(object10);
                }
                var10_8 = null;
            }
        }
        finally {
            this.lockee__5436__auto__ = null;
            // ** MonitorExit[this.lockee__5436__auto__] (shouldn't be in output)
        }
        {
            return var10_8;
        }
    }
}

