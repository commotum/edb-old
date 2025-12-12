/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Datom;

public final class extensions$get_some
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.extensions", (String)"ensure-sv-attrid");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"aevt");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object db2, Object e, ISeq attrs) {
        IPersistentVector iPersistentVector;
        block2: {
            Object G__18018;
            ISeq iSeq = attrs;
            attrs = null;
            Object vec__18019 = G__18018 = ((IFn)const__0.getRawRoot()).invoke((Object)iSeq);
            RT.nth((Object)vec__18019, (int)RT.intCast((long)0L), null);
            vec__18019 = null;
            Object object = G__18018;
            G__18018 = null;
            Object G__180182 = object;
            while (true) {
                Object temp__5455__auto__18026;
                Object attrs2;
                Object object2 = G__180182;
                G__180182 = null;
                Object vec__18022 = object2;
                Object attr = RT.nth((Object)vec__18022, (int)RT.intCast((long)0L), null);
                Object object3 = vec__18022;
                vec__18022 = null;
                Object object4 = attrs2 = object3;
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = attr;
                attr = null;
                Object attrid = ((IFn)const__3.getRawRoot()).invoke(db2, object5);
                Object object6 = temp__5455__auto__18026 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(db2, (Object)const__6, (Object)Tuple.create((Object)attrid, (Object)e)));
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = temp__5455__auto__18026;
                    temp__5455__auto__18026 = null;
                    Object d = object7;
                    Object object8 = attrid;
                    attrid = null;
                    Object object9 = d;
                    d = null;
                    iPersistentVector = Tuple.create((Object)object8, (Object)((Datom)object9).v());
                    break block2;
                }
                Object object10 = attrs2;
                attrs2 = null;
                G__180182 = ((IFn)const__7.getRawRoot()).invoke(object10);
            }
            iPersistentVector = null;
        }
        return iPersistentVector;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return extensions$get_some.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

