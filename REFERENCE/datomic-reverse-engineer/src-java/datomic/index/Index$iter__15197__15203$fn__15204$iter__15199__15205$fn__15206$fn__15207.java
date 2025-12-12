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
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;

public final class Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206$fn__15207
extends AFunction {
    Object c__6023__auto__;
    Object b__15202;
    Object d;
    int size__6024__auto__;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Keyword const__4 = RT.keyword(null, (String)"key");
    public static final Keyword const__6 = RT.keyword(null, (String)"seg");
    public static final Keyword const__8 = RT.keyword(null, (String)"count");

    public Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206$fn__15207(Object object, Object object2, Object object3, int n) {
        this.c__6023__auto__ = object;
        this.b__15202 = object2;
        this.d = object3;
        this.size__6024__auto__ = n;
    }

    public Object invoke() {
        for (long i__15201 = (long)((int)0L); i__15201 < (long)this.size__6024__auto__; ++i__15201) {
            Object di = ((Indexed)this.c__6023__auto__).nth(RT.uncheckedIntCast((long)i__15201));
            Object[] objectArray = new Object[6];
            objectArray[0] = const__4;
            objectArray[1] = RT.nth((Object)((DirNode)this.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)di)));
            objectArray[2] = const__6;
            objectArray[3] = RT.aget((Object[])((Object[])((DirNode)this.d).segids), (int)RT.uncheckedIntCast((Object)di));
            objectArray[4] = const__8;
            Object object = di;
            di = null;
            objectArray[5] = RT.aget((int[])((int[])((DirNode)this.d).counts), (int)RT.uncheckedIntCast((Object)object));
            ((IFn)const__3.getRawRoot()).invoke(this.b__15202, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return Boolean.TRUE;
    }
}

