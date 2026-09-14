/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.queue$queue_seq$done__12155;
import datomic.queue$queue_seq$drain__12157;
import datomic.queue$queue_seq$fill__12153;
import java.util.concurrent.LinkedBlockingQueue;

public final class queue$queue_seq
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"fill");
    public static final Keyword const__2 = RT.keyword(null, (String)"done");
    public static final Keyword const__3 = RT.keyword(null, (String)"drain");

    public static Object invokeStatic(Object size) {
        Object object = size;
        size = null;
        LinkedBlockingQueue q2 = new LinkedBlockingQueue(RT.intCast((Object)object));
        queue$queue_seq$fill__12153 fill = new queue$queue_seq$fill__12153(q2);
        queue$queue_seq$done__12155 done = new queue$queue_seq$done__12155(q2);
        LinkedBlockingQueue linkedBlockingQueue = q2;
        q2 = null;
        queue$queue_seq$drain__12157 drain = new queue$queue_seq$drain__12157(linkedBlockingQueue);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        queue$queue_seq$fill__12153 queue$queue_seq$fill__12153 = fill;
        fill = null;
        objectArray[1] = queue$queue_seq$fill__12153;
        objectArray[2] = const__2;
        queue$queue_seq$done__12155 queue$queue_seq$done__12155 = done;
        done = null;
        objectArray[3] = queue$queue_seq$done__12155;
        objectArray[4] = const__3;
        queue$queue_seq$drain__12157 queue$queue_seq$drain__12157 = drain;
        drain = null;
        objectArray[5] = queue$queue_seq$drain__12157;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return queue$queue_seq.invokeStatic(object2);
    }
}

