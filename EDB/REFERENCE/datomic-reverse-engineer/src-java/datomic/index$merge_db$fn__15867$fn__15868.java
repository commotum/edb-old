/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$merge_db$fn__15867$fn__15868
extends AFunction {
    Object calc;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"IndexPacingMsec");

    public index$merge_db$fn__15867$fn__15868(Object object) {
        this.calc = object;
    }

    public Object invoke(Object segs) {
        Object v4;
        Object temp__5457__auto__15870;
        Object object = segs;
        segs = null;
        Object object2 = temp__5457__auto__15870 = ((IFn)this_.calc).invoke(object);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__15870;
            temp__5457__auto__15870 = null;
            Object msec = object3;
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, msec);
            Object object4 = msec;
            msec = null;
            index$merge_db$fn__15867$fn__15868 this_ = null;
            Thread.sleep(RT.uncheckedLongCast((Object)((Number)object4)));
            v4 = null;
        } else {
            v4 = null;
        }
        return v4;
    }
}

