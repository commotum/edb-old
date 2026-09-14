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

public final class index$create_pace_calculator$next_state__15307
extends AFunction {
    Object window_msec;
    long start;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"window");
    public static final Keyword const__4 = RT.keyword(null, (String)"work");
    public static final Object const__10 = 0L;
    public static final Keyword const__11 = RT.keyword(null, (String)"msec");

    public index$create_pace_calculator$next_state__15307(Object object, long l) {
        this.window_msec = object;
        this.start = l;
    }

    public Object invoke(Object p__15306, Object nwork) {
        Object object;
        Object object2;
        Object object3 = p__15306;
        p__15306 = null;
        Object map__15308 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__15308);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__15308;
            map__15308 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__15308;
            map__15308 = null;
        }
        Object map__153082 = object2;
        Object window = RT.get((Object)map__153082, (Object)const__3);
        Object object6 = map__153082;
        map__153082 = null;
        Object work = RT.get((Object)object6, (Object)const__4);
        long now = System.currentTimeMillis();
        long elapsed = now - this.start;
        Number nwind = Numbers.quotient((long)elapsed, (Object)this.window_msec);
        Number msec = Numbers.remainder((long)elapsed, (Object)this.window_msec);
        Object object7 = nwork;
        nwork = null;
        Object object8 = window;
        window = null;
        if (Util.equiv((Object)object8, (Object)nwind)) {
            object = work;
            work = null;
        } else {
            object = const__10;
        }
        Number nwork2 = Numbers.unchecked_add((Object)object7, (Object)object);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__3;
        Number number = nwind;
        nwind = null;
        objectArray[1] = number;
        objectArray[2] = const__4;
        Number number2 = nwork2;
        nwork2 = null;
        objectArray[3] = number2;
        objectArray[4] = const__11;
        Number number3 = msec;
        msec = null;
        objectArray[5] = Numbers.unchecked_minus((Object)this.window_msec, (Object)number3);
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

