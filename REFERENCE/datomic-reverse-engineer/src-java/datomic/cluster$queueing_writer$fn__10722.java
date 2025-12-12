/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
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
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.concurrent.ArrayBlockingQueue;

public final class cluster$queueing_writer$fn__10722
extends AFunction {
    Object done_reason;
    Object progress;
    Object queue;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"realized?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"type");
    public static final Keyword const__5 = RT.keyword(null, (String)"obj");
    public static final Keyword const__6 = RT.keyword(null, (String)"sync-writes");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Keyword const__8 = RT.keyword(null, (String)"synced");
    public static final Keyword const__9 = RT.keyword(null, (String)"create-val");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__13 = RT.keyword(null, (String)"created");
    public static final Keyword const__14 = RT.keyword(null, (String)"finish");
    public static final Keyword const__15 = RT.keyword(null, (String)"done");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"str");

    public cluster$queueing_writer$fn__10722(Object object, Object object2, Object object3) {
        this.done_reason = object;
        this.progress = object2;
        this.queue = object3;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Object invoke() {
        try {
            Object G__10724;
            block7: while (true) {
                Object object;
                Object object2 = ((IFn)const__0.getRawRoot()).invoke(this.done_reason);
                if (object2 != null && object2 != Boolean.FALSE) {
                    return null;
                }
                Object map__10723 = ((ArrayBlockingQueue)this.queue).take();
                Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__10723);
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object e = map__10723;
                    map__10723 = null;
                    object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(e)));
                } else {
                    object = map__10723;
                    map__10723 = null;
                }
                Object map__107232 = object;
                Object type = RT.get(map__107232, (Object)const__4);
                Object e = map__107232;
                map__107232 = null;
                Object obj = RT.get(e, (Object)const__5);
                Object object4 = type;
                type = null;
                G__10724 = object4;
                switch (Util.hash((Object)G__10724) >> 0 & 3) {
                    case 1: {
                        if (G__10724 != const__6) break block7;
                        Object object5 = obj;
                        obj = null;
                        ((IFn)const__7.getRawRoot()).invoke(object5, (Object)const__8);
                        continue block7;
                    }
                    case 2: {
                        Object create_result;
                        if (G__10724 != const__9) break block7;
                        ((IFn)this.progress).invoke((Object)RT.count((Object)this.queue));
                        Object object6 = obj;
                        obj = null;
                        Object object7 = create_result = ((IFn)const__11.getRawRoot()).invoke(object6);
                        create_result = null;
                        if (!Util.equiv((Object)const__13, (Object)object7)) throw (Throwable)new RuntimeException("Queued write failed.");
                        continue block7;
                    }
                    case 3: {
                        if (G__10724 != const__14) break block7;
                        Object object8 = ((IFn)this.done_reason).invoke((Object)const__15);
                        return object8;
                    }
                }
                break;
            }
            Object object = G__10724;
            G__10724 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__16.getRawRoot()).invoke((Object)"No matching clause: ", object));
        }
        catch (Throwable t2) {
            this.done_reason = null;
            Object t2 = null;
            return ((IFn)this.done_reason).invoke((Object)t2);
        }
    }
}

