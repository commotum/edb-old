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
import clojure.lang.Var;

public final class index$create_pace_calculator$fn__15310
extends AFunction {
    Object target_work;
    Object next_state;
    Object window_msec;
    Object state;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"work");
    public static final Keyword const__5 = RT.keyword(null, (String)"msec");

    public index$create_pace_calculator$fn__15310(Object object, Object object2, Object object3, Object object4) {
        this.target_work = object;
        this.next_state = object2;
        this.window_msec = object3;
        this.state = object4;
    }

    public Object invoke(Object nwork) {
        Object object;
        Object object2;
        Object object3 = nwork;
        nwork = null;
        Object map__15311 = ((IFn)const__0.getRawRoot()).invoke(this.state, this.next_state, object3);
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__15311);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__15311;
            map__15311 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__15311;
            map__15311 = null;
        }
        Object map__153112 = object2;
        Object work = RT.get((Object)map__153112, (Object)const__4);
        Object object6 = map__153112;
        map__153112 = null;
        Object msec = RT.get((Object)object6, (Object)const__5);
        Numbers.unchecked_minus((Object)this.window_msec, (Object)msec);
        Object object7 = work;
        work = null;
        if (Numbers.gte((Object)object7, (Object)this.target_work)) {
            object = msec;
            msec = null;
        } else {
            object = null;
        }
        return object;
    }
}

