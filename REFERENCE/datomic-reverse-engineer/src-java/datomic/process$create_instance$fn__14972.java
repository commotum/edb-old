/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.process$create_instance$fn__14972$fn__14977;
import datomic.process$create_instance$fn__14972$fn__14979;

public final class process$create_instance$fn__14972
extends AFunction {
    Object shutdown_time;
    Object handlers;
    Object exit_QMARK_;
    Object prom;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__15 = RT.keyword(null, (String)"shutdown");

    public process$create_instance$fn__14972(Object object, Object object2, Object object3, Object object4) {
        this.shutdown_time = object;
        this.handlers = object2;
        this.exit_QMARK_ = object3;
        this.prom = object4;
    }

    public Object invoke() {
        block3: {
            this.prom = null;
            ((IFn)const__0.getRawRoot()).invoke(this.prom, (Object)Boolean.TRUE);
            this.handlers = null;
            Object seq_14973 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.handlers));
            Object chunk_14974 = null;
            long count_14975 = 0L;
            long i_14976 = 0L;
            while (true) {
                Object h;
                Object temp__5457__auto__14983;
                if (i_14976 < count_14975) {
                    Object h3;
                    Object object = h3 = ((Indexed)chunk_14974).nth(RT.intCast((long)i_14976));
                    h3 = null;
                    ((IFn)const__5.getRawRoot()).invoke((Object)new process$create_instance$fn__14972$fn__14977(object));
                    Object object2 = seq_14973;
                    seq_14973 = null;
                    Object object3 = chunk_14974;
                    chunk_14974 = null;
                    ++i_14976;
                    chunk_14974 = object3;
                    seq_14973 = object2;
                    continue;
                }
                Object object = seq_14973;
                seq_14973 = null;
                Object object4 = temp__5457__auto__14983 = ((IFn)const__1.getRawRoot()).invoke(object);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5457__auto__14983;
                temp__5457__auto__14983 = null;
                Object seq_149732 = object5;
                Object object6 = ((IFn)const__7.getRawRoot()).invoke(seq_149732);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object c__5719__auto__14982 = ((IFn)const__8.getRawRoot()).invoke(seq_149732);
                    Object object7 = seq_149732;
                    seq_149732 = null;
                    Object object8 = c__5719__auto__14982;
                    Object object9 = c__5719__auto__14982;
                    c__5719__auto__14982 = null;
                    i_14976 = RT.intCast((long)0L);
                    count_14975 = RT.intCast((int)RT.count((Object)object9));
                    chunk_14974 = object8;
                    seq_14973 = ((IFn)const__9.getRawRoot()).invoke(object7);
                    continue;
                }
                Object object10 = h = ((IFn)const__12.getRawRoot()).invoke(seq_149732);
                h = null;
                ((IFn)const__5.getRawRoot()).invoke((Object)new process$create_instance$fn__14972$fn__14979(object10));
                Object object11 = seq_149732;
                seq_149732 = null;
                i_14976 = 0L;
                count_14975 = 0L;
                chunk_14974 = null;
                seq_14973 = ((IFn)const__13.getRawRoot()).invoke(object11);
            }
            this.shutdown_time = null;
            Thread.sleep(RT.longCast((Object)((Number)this.shutdown_time)));
            Object object = this.exit_QMARK_;
            this.exit_QMARK_ = null;
            if (object == null || object == Boolean.FALSE) break block3;
            System.exit(RT.intCast((long)-1L));
        }
        return const__15;
    }
}

