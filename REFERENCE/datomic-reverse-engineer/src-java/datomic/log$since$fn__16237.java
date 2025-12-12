/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class log$since$fn__16237
extends AFunction {
    Object since_t;
    Object txes;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"into");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public log$since$fn__16237(Object object, Object object2) {
        this.since_t = object;
        this.txes = object2;
    }

    public Object invoke() {
        Object object;
        block3: {
            Object txes = this_.txes;
            while (true) {
                Object vec__16238;
                Object object2 = vec__16238 = txes;
                vec__16238 = null;
                Object seq__16239 = ((IFn)const__0.getRawRoot()).invoke(object2);
                Object first__16240 = ((IFn)const__1.getRawRoot()).invoke(seq__16239);
                Object object3 = seq__16239;
                seq__16239 = null;
                Object seq__162392 = ((IFn)const__2.getRawRoot()).invoke(object3);
                Object object4 = first__16240;
                first__16240 = null;
                Object tx = object4;
                Object object5 = seq__162392;
                seq__162392 = null;
                Object more = object5;
                Object object6 = tx;
                if (object6 == null || object6 == Boolean.FALSE) break;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object7 = tx;
                tx = null;
                Object object8 = iLookupThunk.get(object7);
                if (iLookupThunk == object8) {
                    __thunk__0__ = __site__0__.fault(object7);
                    object8 = __thunk__0__.get(object7);
                }
                if (Numbers.lt((Object)this_.since_t, (Object)object8)) {
                    Object object9 = txes;
                    txes = null;
                    log$since$fn__16237 this_ = null;
                    object = ((IFn)const__5.getRawRoot()).invoke((Object)PersistentVector.EMPTY, object9);
                    break block3;
                }
                Object object10 = more;
                more = null;
                txes = object10;
            }
            object = PersistentVector.EMPTY;
        }
        return object;
    }
}

