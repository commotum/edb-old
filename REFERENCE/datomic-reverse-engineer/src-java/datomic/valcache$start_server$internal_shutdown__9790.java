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
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.concurrent.ConcurrentHashMap;

public final class valcache$start_server$internal_shutdown__9790
extends AFunction {
    Object ssc;
    Object socket_registry;
    Object shutdown_requested;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"next");

    public valcache$start_server$internal_shutdown__9790(Object object, Object object2, Object object3) {
        this.ssc = object;
        this.socket_registry = object2;
        this.shutdown_requested = object3;
    }

    public Object invoke() {
        ((IFn)const__0.getRawRoot()).invoke(this.shutdown_requested, (Object)Boolean.TRUE);
        ((AbstractInterruptibleChannel)this.ssc).close();
        Object seq_9791 = ((IFn)const__1.getRawRoot()).invoke((Object)((ConcurrentHashMap)this.socket_registry).keySet());
        Object chunk_9792 = null;
        long count_9793 = 0L;
        long i_9794 = 0L;
        while (true) {
            Object sc;
            Object temp__5457__auto__9797;
            if (i_9794 < count_9793) {
                Object sc2;
                Object object = sc2 = ((Indexed)chunk_9792).nth(RT.intCast((long)i_9794));
                sc2 = null;
                ((AbstractInterruptibleChannel)object).close();
                Object object2 = seq_9791;
                seq_9791 = null;
                Object object3 = chunk_9792;
                chunk_9792 = null;
                ++i_9794;
                chunk_9792 = object3;
                seq_9791 = object2;
                continue;
            }
            Object object = seq_9791;
            seq_9791 = null;
            Object object4 = temp__5457__auto__9797 = ((IFn)const__1.getRawRoot()).invoke(object);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5457__auto__9797;
            temp__5457__auto__9797 = null;
            Object seq_97912 = object5;
            Object object6 = ((IFn)const__5.getRawRoot()).invoke(seq_97912);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object c__5719__auto__9796 = ((IFn)const__6.getRawRoot()).invoke(seq_97912);
                Object object7 = seq_97912;
                seq_97912 = null;
                Object object8 = c__5719__auto__9796;
                Object object9 = c__5719__auto__9796;
                c__5719__auto__9796 = null;
                i_9794 = RT.intCast((long)0L);
                count_9793 = RT.intCast((int)RT.count((Object)object9));
                chunk_9792 = object8;
                seq_9791 = ((IFn)const__7.getRawRoot()).invoke(object7);
                continue;
            }
            Object object10 = sc = ((IFn)const__10.getRawRoot()).invoke(seq_97912);
            sc = null;
            ((AbstractInterruptibleChannel)object10).close();
            Object object11 = seq_97912;
            seq_97912 = null;
            i_9794 = 0L;
            count_9793 = 0L;
            chunk_9792 = null;
            seq_9791 = ((IFn)const__11.getRawRoot()).invoke(object11);
        }
        return null;
    }
}

