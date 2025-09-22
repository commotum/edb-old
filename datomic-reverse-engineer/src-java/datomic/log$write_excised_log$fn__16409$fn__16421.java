/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.log$write_excised_log$fn__16409$fn__16421$fn__16426;
import datomic.log.LogDir;

public final class log$write_excised_log$fn__16409$fn__16421
extends AFunction {
    Object ts;
    Object excise_QMARK_;
    Object lookup;
    Object segids;
    Object cs;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"write-excise-val");
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"fressianed-leaf");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"conj");

    public log$write_excised_log$fn__16409$fn__16421(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.ts = object;
        this.excise_QMARK_ = object2;
        this.lookup = object3;
        this.segids = object4;
        this.cs = object5;
    }

    public Object invoke(Object p__16420, Object direntry) {
        IPersistentVector iPersistentVector;
        Object object = p__16420;
        p__16420 = null;
        Object vec__16422 = object;
        Object m = RT.nth((Object)vec__16422, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16422;
        vec__16422 = null;
        Object newdir = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        long t = ((LogDir)direntry).t;
        Object segid = ((LogDir)direntry).uuid;
        Object object3 = ((IFn)const__3.getRawRoot()).invoke(this.segids, segid);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object newseg;
            Object seg = ((IFn)const__4.getRawRoot()).invoke(this.lookup, segid);
            Object newsid = ((IFn)const__5.getRawRoot()).invoke();
            Object object4 = seg;
            seg = null;
            Object object5 = newseg = ((IFn)const__6.getRawRoot()).invoke((Object)new log$write_excised_log$fn__16409$fn__16421$fn__16426(this.ts, this.excise_QMARK_), object4);
            newseg = null;
            ((IFn)const__7.getRawRoot()).invoke(this.cs, newsid, ((IFn)const__8.getRawRoot()).invoke(object5));
            Object object6 = m;
            m = null;
            Object object7 = segid;
            segid = null;
            Object object8 = ((IFn)const__9.getRawRoot()).invoke(object6, object7, newsid);
            Object object9 = newdir;
            newdir = null;
            Object object10 = direntry;
            direntry = null;
            Object object11 = newsid;
            newsid = null;
            iPersistentVector = Tuple.create((Object)object8, (Object)((IFn)const__10.getRawRoot()).invoke(object9, (Object)new LogDir(((LogDir)object10).t, object11)));
        } else {
            Object object12 = m;
            m = null;
            Object object13 = newdir;
            newdir = null;
            Object object14 = direntry;
            direntry = null;
            iPersistentVector = Tuple.create((Object)object12, (Object)((IFn)const__10.getRawRoot()).invoke(object13, object14));
        }
        return iPersistentVector;
    }
}

