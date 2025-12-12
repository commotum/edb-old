/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.AsyncWriter;
import datomic.index.DirNode;
import java.util.List;

public final class index$merge_one_index$mkdes__15464$fn__15465
extends AFunction {
    Object pario;
    Object cstore;
    Object xsegs;
    Object olookup;
    Object excise_QMARK_;
    Object write_handlers;
    Object segs_written_ref;
    Object dir;
    Object filter_segids;
    private static Class __cached_class__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__16;

    public index$merge_one_index$mkdes__15464$fn__15465(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.pario = object;
        this.cstore = object2;
        this.xsegs = object3;
        this.olookup = object4;
        this.excise_QMARK_ = object5;
        this.write_handlers = object6;
        this.segs_written_ref = object7;
        this.dir = object8;
        this.filter_segids = object9;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object ret, Object i) {
        block5: {
            block6: {
                block7: {
                    block8: {
                        block4: {
                            segid = RT.aget((Object[])((Object[])((DirNode)this.dir).segids), (int)RT.uncheckedIntCast((Object)i));
                            v0 = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__2.getRawRoot()).invoke(this.filter_segids, segid);
                            if (v0 == null || v0 == Boolean.FALSE) break block4;
                            v1 = ret;
                            ret = null;
                            break block5;
                        }
                        v2 = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__2.getRawRoot()).invoke(this.xsegs, segid);
                        if (v2 == null || v2 == Boolean.FALSE) break block6;
                        v3 = segid;
                        segid = null;
                        pd = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__3.getRawRoot()).invoke(this.excise_QMARK_, ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__4.getRawRoot()).invoke(this.olookup, v3));
                        v4 = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__5.getRawRoot()).invoke(pd);
                        if (v4 == null || v4 == Boolean.FALSE) break block7;
                        v5 = ret;
                        ret = null;
                        v6 = pd;
                        pd = null;
                        ret = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__6.getRawRoot()).invoke(v5, ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__7.getRawRoot()).invoke(this.cstore, this.olookup, v6, (Object)PersistentVector.EMPTY, this.write_handlers, this.segs_written_ref));
                        v7 = this.pario;
                        if (v7 == null || v7 == Boolean.FALSE) break block8;
                        v8 = (IFn)index$merge_one_index$mkdes__15464$fn__15465.const__8.getRawRoot();
                        v9 = this.cstore;
                        if (Util.classOf((Object)v9) == index$merge_one_index$mkdes__15464$fn__15465.__cached_class__0) ** GOTO lbl28
                        if (!(v9 instanceof AsyncWriter)) {
                            v9 = v9;
                            index$merge_one_index$mkdes__15464$fn__15465.__cached_class__0 = Util.classOf((Object)v9);
lbl28:
                            // 2 sources

                            v10 = index$merge_one_index$mkdes__15464$fn__15465.const__9.getRawRoot().invoke(v9);
                        } else {
                            v10 = ((AsyncWriter)v9).sync_writes();
                        }
                        v8.invoke(v10, index$merge_one_index$mkdes__15464$fn__15465.const__10.getRawRoot());
                    }
                    v1 = ret;
                    ret = null;
                    break block5;
                }
                v1 = ret;
                ret = null;
                break block5;
            }
            v11 = index$merge_one_index$mkdes__15464$fn__15465.const__11;
            if (v11 != null && v11 != Boolean.FALSE) {
                v12 = ret;
                ret = null;
                v13 = new Object[8];
                v13[0] = index$merge_one_index$mkdes__15464$fn__15465.const__13;
                v13[1] = ((List)((DirNode)this.dir).keydata).get(RT.uncheckedIntCast((Object)((Number)i)));
                v13[2] = index$merge_one_index$mkdes__15464$fn__15465.const__14;
                v14 = segid;
                segid = null;
                v13[3] = v14;
                v13[4] = index$merge_one_index$mkdes__15464$fn__15465.const__15;
                v13[5] = RT.aget((int[])((int[])((DirNode)this.dir).offsets), (int)RT.uncheckedIntCast((Object)i));
                v13[6] = index$merge_one_index$mkdes__15464$fn__15465.const__16;
                v15 = i;
                i = null;
                v13[7] = RT.aget((int[])((int[])((DirNode)this.dir).counts), (int)RT.uncheckedIntCast((Object)v15));
                this = null;
                v1 = ((IFn)index$merge_one_index$mkdes__15464$fn__15465.const__12.getRawRoot()).invoke(v12, (Object)RT.mapUniqueKeys((Object[])v13));
            } else {
                v1 = null;
            }
        }
        return v1;
    }

    static {
        const__2 = RT.var((String)"clojure.core", (String)"contains?");
        const__3 = RT.var((String)"clojure.core", (String)"remove");
        const__4 = RT.var((String)"datomic.cache", (String)"getx-uncached");
        const__5 = RT.var((String)"clojure.core", (String)"seq");
        const__6 = RT.var((String)"clojure.core", (String)"into");
        const__7 = RT.var((String)"datomic.index", (String)"build-psegs");
        const__8 = RT.var((String)"datomic.common", (String)"bounded-deref");
        const__9 = RT.var((String)"datomic.cluster", (String)"sync-writes");
        const__10 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__11 = RT.keyword(null, (String)"else");
        const__12 = RT.var((String)"clojure.core", (String)"conj");
        const__13 = RT.keyword(null, (String)"key");
        const__14 = RT.keyword(null, (String)"segid");
        const__15 = RT.keyword(null, (String)"offset");
        const__16 = RT.keyword(null, (String)"count");
    }
}

