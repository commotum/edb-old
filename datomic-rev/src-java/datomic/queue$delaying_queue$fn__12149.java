/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.queue.BlockingConsumer;
import datomic.queue.BlockingProducer;

public final class queue$delaying_queue$fn__12149
extends AFunction {
    Object dest_queue;
    Object delay_queue;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__10;

    public queue$delaying_queue$fn__12149(Object object, Object object2) {
        this.dest_queue = object;
        this.delay_queue = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        while (true) {
            block11: {
                block10: {
                    if (Util.classOf((Object)(v0 = this.delay_queue)) == queue$delaying_queue$fn__12149.__cached_class__0) ** GOTO lbl6
                    if (!(v0 instanceof BlockingConsumer)) {
                        v0 = v0;
                        queue$delaying_queue$fn__12149.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                        // 2 sources

                        v1 = queue$delaying_queue$fn__12149.const__0.getRawRoot().invoke(v0);
                    } else {
                        v1 = obj = ((BlockingConsumer)v0).take();
                    }
                    if (Util.equiv((Object)obj, (Object)this.delay_queue)) break;
                    v2 = obj;
                    obj = null;
                    map__12150 = v2;
                    v3 = ((IFn)queue$delaying_queue$fn__12149.const__2.getRawRoot()).invoke(map__12150);
                    if (v3 != null && v3 != Boolean.FALSE) {
                        v4 = map__12150;
                        map__12150 = null;
                        v5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)queue$delaying_queue$fn__12149.const__3.getRawRoot()).invoke(v4)));
                    } else {
                        v5 = map__12150;
                        map__12150 = null;
                    }
                    map__12150 = v5;
                    item = RT.get((Object)map__12150, (Object)queue$delaying_queue$fn__12149.const__5);
                    v6 = map__12150;
                    map__12150 = null;
                    v7 = timestamp = RT.get((Object)v6, (Object)queue$delaying_queue$fn__12149.const__6);
                    timestamp = null;
                    sleep = Numbers.minus((Object)v7, (long)System.currentTimeMillis());
                    if (Numbers.gt((Object)sleep, (long)0L)) {
                        v8 = sleep;
                        sleep = null;
                        Thread.sleep(RT.longCast((Object)v8));
                    }
                    if (Util.classOf((Object)(v9 = this.dest_queue)) == queue$delaying_queue$fn__12149.__cached_class__1) break block10;
                    if (v9 instanceof BlockingProducer) break block11;
                    v9 = v9;
                    queue$delaying_queue$fn__12149.__cached_class__1 = Util.classOf((Object)v9);
                }
                v10 = item;
                item = null;
                v11 = queue$delaying_queue$fn__12149.const__10.getRawRoot().invoke(v9, v10);
                continue;
            }
            v12 = item;
            item = null;
            v11 = ((BlockingProducer)v9).put(v12);
        }
        return null;
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"take");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"item");
        const__6 = RT.keyword(null, (String)"timestamp");
        const__10 = RT.var((String)"datomic.queue", (String)"put");
    }
}

