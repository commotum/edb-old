/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fulltext.Root;

public final class fulltext$build_index$fn__14674$fn__14700
extends AFunction {
    Object olookup;
    Object histmap;
    Object cstore;
    Object G__14693;
    Object db;
    Object garbage;
    Object rootmap;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"datomic.fulltext", (String)"separate-history");
    public static final Var const__7 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__8 = RT.var((String)"datomic.fulltext", (String)"do-indexing-job");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"into");

    public fulltext$build_index$fn__14674$fn__14700(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.olookup = object;
        this.histmap = object2;
        this.cstore = object3;
        this.G__14693 = object4;
        this.db = object5;
        this.garbage = object6;
        this.rootmap = object7;
    }

    public Object invoke() {
        Object garbage2;
        Object histmap;
        Object rootmap;
        Object rootmap2 = this.rootmap = null;
        Object histmap2 = this.histmap = null;
        Object garbage3 = this.garbage = null;
        Object G__14693 = this.G__14693 = null;
        while (true) {
            Object vec__14701;
            Object object = rootmap2;
            rootmap2 = null;
            rootmap = object;
            Object object2 = histmap2;
            histmap2 = null;
            histmap = object2;
            Object object3 = garbage3;
            garbage3 = null;
            garbage2 = object3;
            Object object4 = G__14693;
            G__14693 = null;
            Object object5 = vec__14701 = object4;
            vec__14701 = null;
            Object seq__14702 = ((IFn)const__0.getRawRoot()).invoke(object5);
            Object first__14703 = ((IFn)const__1.getRawRoot()).invoke(seq__14702);
            Object object6 = seq__14702;
            seq__14702 = null;
            Object seq__147022 = ((IFn)const__2.getRawRoot()).invoke(object6);
            Object object7 = first__14703;
            first__14703 = null;
            Object vec__14704 = object7;
            Object attrid = RT.nth((Object)vec__14704, (int)RT.intCast((long)0L), null);
            Object object8 = vec__14704;
            vec__14704 = null;
            Object iter2 = RT.nth((Object)object8, (int)RT.intCast((long)1L), null);
            Object object9 = seq__147022;
            seq__147022 = null;
            Object more = object9;
            Object object10 = attrid;
            if (object10 == null || object10 == Boolean.FALSE) break;
            Object object11 = iter2;
            iter2 = null;
            Object vec__14707 = ((IFn)const__6.getRawRoot()).invoke(this.db, ((IFn)const__7.getRawRoot()).invoke(object11), (Object)PersistentVector.EMPTY);
            Object data2 = RT.nth((Object)vec__14707, (int)RT.intCast((long)0L), null);
            Object object12 = vec__14707;
            vec__14707 = null;
            Object histdata = RT.nth((Object)object12, (int)RT.intCast((long)1L), null);
            Object object13 = data2;
            data2 = null;
            Object vec__14710 = ((IFn)const__8.getRawRoot()).invoke(this.cstore, this.olookup, object13, histdata, attrid, RT.get((Object)rootmap, (Object)attrid));
            Object newdirid = RT.nth((Object)vec__14710, (int)RT.intCast((long)0L), null);
            Object object14 = vec__14710;
            vec__14710 = null;
            Object gids = RT.nth((Object)object14, (int)RT.intCast((long)1L), null);
            Object object15 = histdata;
            histdata = null;
            Object vec__14713 = ((IFn)const__8.getRawRoot()).invoke(this.cstore, this.olookup, object15, null, attrid, RT.get((Object)histmap, (Object)attrid));
            Object newhistdirid = RT.nth((Object)vec__14713, (int)RT.intCast((long)0L), null);
            Object object16 = vec__14713;
            vec__14713 = null;
            Object histgids = RT.nth((Object)object16, (int)RT.intCast((long)1L), null);
            Object object17 = rootmap;
            rootmap = null;
            Object object18 = newdirid;
            newdirid = null;
            Object object19 = ((IFn)const__10.getRawRoot()).invoke(object17, attrid, object18);
            Object object20 = histmap;
            histmap = null;
            Object object21 = attrid;
            attrid = null;
            Object object22 = newhistdirid;
            newhistdirid = null;
            Object object23 = garbage2;
            garbage2 = null;
            Object object24 = gids;
            gids = null;
            Object object25 = histgids;
            histgids = null;
            Object object26 = more;
            more = null;
            G__14693 = object26;
            garbage3 = ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), object23, (Object)Tuple.create((Object)object24, (Object)object25));
            histmap2 = ((IFn)const__10.getRawRoot()).invoke(object20, object21, object22);
            rootmap2 = object19;
        }
        Object object = rootmap;
        rootmap = null;
        Object object27 = histmap;
        histmap = null;
        Object object28 = garbage2;
        garbage2 = null;
        return Tuple.create((Object)new Root(object), (Object)new Root(object27), (Object)object28);
    }
}

