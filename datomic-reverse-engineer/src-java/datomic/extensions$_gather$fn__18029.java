/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.LazilyPersistentVector
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.LazilyPersistentVector;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Datom;
import datomic.db.IDb;
import datomic.extensions$_gather$fn__18029$fn__18030;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;

public final class extensions$_gather$fn__18029
extends AFunction {
    Object attrs;
    int cnt;
    Object db;
    Object eid;
    Object arr;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.extensions", (String)"ensure-sv-attrid");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__7 = RT.keyword(null, (String)"e");
    public static final Keyword const__8 = RT.keyword(null, (String)"a");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"next");

    public extensions$_gather$fn__18029(Object object, int n, Object object2, Object object3, Object object4) {
        this.attrs = object;
        this.cnt = n;
        this.db = object2;
        this.eid = object3;
        this.arr = object4;
    }

    public Object invoke() {
        IPersistentVector iPersistentVector;
        block2: {
            long i = 0L;
            this_.attrs = null;
            Object attrs = ((IFn)const__1.getRawRoot()).invoke(this_.attrs);
            while (true) {
                Object iter2;
                Object temp__5455__auto__18034;
                if (i == (long)this_.cnt) {
                    extensions$_gather$fn__18029 this_ = null;
                    iPersistentVector = LazilyPersistentVector.createOwning((Object[])((Object[])this_.arr));
                    break block2;
                }
                Object attrid = ((IFn)const__3.getRawRoot()).invoke(this_.db, ((IFn)const__4.getRawRoot()).invoke(attrs));
                extensions$_gather$fn__18029$fn__18030 extensions$_gather$fn__18029$fn__18030 = new extensions$_gather$fn__18029$fn__18030(attrid, this_.eid);
                Object object = attrid;
                attrid = null;
                Object object2 = temp__5455__auto__18034 = ((IFn)const__5.getRawRoot()).invoke(this_.db, (Object)extensions$_gather$fn__18029$fn__18030, (Object)((IDb)this_.db).seekEAVT((IDatum)((IFn)const__6.getRawRoot()).invoke(this_.db, (Object)const__7, this_.eid, (Object)const__8, object)));
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5455__auto__18034;
                temp__5455__auto__18034 = null;
                Object object4 = iter2 = object3;
                iter2 = null;
                RT.aset((Object[])((Object[])this_.arr), (int)RT.intCast((long)i), (Object)((Datom)((Iter)object4).get()).v());
                Object object5 = attrs;
                attrs = null;
                attrs = ((IFn)const__12.getRawRoot()).invoke(object5);
                i = Numbers.inc((long)i);
            }
            iPersistentVector = null;
        }
        return iPersistentVector;
    }
}

