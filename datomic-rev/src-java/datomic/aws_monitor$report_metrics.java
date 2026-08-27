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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_monitor$report_metrics
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.aws-monitor", (String)"partitioned-metrics-requests");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"datomic.cloudwatch", (String)"put-metrics");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object client2, Object dimensions, Object metrics) {
        Object mparts;
        Object object = dimensions;
        dimensions = null;
        Object object2 = metrics;
        metrics = null;
        Object object3 = mparts = ((IFn)const__0.getRawRoot()).invoke(object, object2);
        mparts = null;
        Object seq_23623 = ((IFn)const__1.getRawRoot()).invoke(object3);
        Object chunk_23624 = null;
        long count_23625 = 0L;
        long i_23626 = 0L;
        while (true) {
            Object mpart;
            Object temp__5457__auto__23629;
            if (i_23626 < count_23625) {
                Object mpart2;
                Object object4 = mpart2 = ((Indexed)chunk_23624).nth(RT.intCast((long)i_23626));
                mpart2 = null;
                ((IFn)const__4.getRawRoot()).invoke(client2, object4);
                Object object5 = seq_23623;
                seq_23623 = null;
                Object object6 = chunk_23624;
                chunk_23624 = null;
                ++i_23626;
                chunk_23624 = object6;
                seq_23623 = object5;
                continue;
            }
            Object object7 = seq_23623;
            seq_23623 = null;
            Object object8 = temp__5457__auto__23629 = ((IFn)const__1.getRawRoot()).invoke(object7);
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object object9 = temp__5457__auto__23629;
            temp__5457__auto__23629 = null;
            Object seq_236232 = object9;
            Object object10 = ((IFn)const__6.getRawRoot()).invoke(seq_236232);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object c__5719__auto__23628 = ((IFn)const__7.getRawRoot()).invoke(seq_236232);
                Object object11 = seq_236232;
                seq_236232 = null;
                Object object12 = c__5719__auto__23628;
                Object object13 = c__5719__auto__23628;
                c__5719__auto__23628 = null;
                i_23626 = RT.intCast((long)0L);
                count_23625 = RT.intCast((int)RT.count((Object)object13));
                chunk_23624 = object12;
                seq_23623 = ((IFn)const__8.getRawRoot()).invoke(object11);
                continue;
            }
            Object object14 = mpart = ((IFn)const__11.getRawRoot()).invoke(seq_236232);
            mpart = null;
            ((IFn)const__4.getRawRoot()).invoke(client2, object14);
            Object object15 = seq_236232;
            seq_236232 = null;
            i_23626 = 0L;
            count_23625 = 0L;
            chunk_23624 = null;
            seq_23623 = ((IFn)const__12.getRawRoot()).invoke(object15);
        }
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return aws_monitor$report_metrics.invokeStatic(object4, object5, object6);
    }
}

