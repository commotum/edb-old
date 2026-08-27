/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$join_project_coll_with$fn__18131
extends AFunction {
    Object join_map;
    Object bindings;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public datalog$join_project_coll_with$fn__18131(Object object, Object object2) {
        this.join_map = object;
        this.bindings = object2;
    }

    public Object invoke() {
        Object seq_18127 = ((IFn)const__0.getRawRoot()).invoke(this.join_map);
        Object chunk_18128 = null;
        long count_18129 = 0L;
        long i_18130 = 0L;
        while (true) {
            Object temp__5457__auto__18140;
            if (i_18130 < count_18129) {
                Object vec__18132 = ((Indexed)chunk_18128).nth(RT.uncheckedIntCast((long)i_18130));
                Object k = RT.nth((Object)vec__18132, (int)RT.uncheckedIntCast((long)0L), null);
                Object object = vec__18132;
                vec__18132 = null;
                Object v = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
                Object object2 = k;
                k = null;
                Object object3 = v;
                v = null;
                RT.aset((Object[])((Object[])this.bindings), (int)RT.uncheckedIntCast((Object)object2), (Object)object3);
                Object object4 = seq_18127;
                seq_18127 = null;
                Object object5 = chunk_18128;
                chunk_18128 = null;
                ++i_18130;
                chunk_18128 = object5;
                seq_18127 = object4;
                continue;
            }
            Object object = seq_18127;
            seq_18127 = null;
            Object object6 = temp__5457__auto__18140 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__18140;
            temp__5457__auto__18140 = null;
            Object seq_181272 = object7;
            Object object8 = ((IFn)const__8.getRawRoot()).invoke(seq_181272);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__18139 = ((IFn)const__9.getRawRoot()).invoke(seq_181272);
                Object object9 = seq_181272;
                seq_181272 = null;
                Object object10 = c__5719__auto__18139;
                Object object11 = c__5719__auto__18139;
                c__5719__auto__18139 = null;
                i_18130 = (int)0L;
                count_18129 = RT.count((Object)object11);
                chunk_18128 = object10;
                seq_18127 = ((IFn)const__10.getRawRoot()).invoke(object9);
                continue;
            }
            Object vec__18135 = ((IFn)const__12.getRawRoot()).invoke(seq_181272);
            Object k = RT.nth((Object)vec__18135, (int)RT.uncheckedIntCast((long)0L), null);
            Object object12 = vec__18135;
            vec__18135 = null;
            Object v = RT.nth((Object)object12, (int)RT.uncheckedIntCast((long)1L), null);
            Object object13 = k;
            k = null;
            Object object14 = v;
            v = null;
            RT.aset((Object[])((Object[])this.bindings), (int)RT.uncheckedIntCast((Object)object13), (Object)object14);
            Object object15 = seq_181272;
            seq_181272 = null;
            i_18130 = 0L;
            count_18129 = 0L;
            chunk_18128 = null;
            seq_18127 = ((IFn)const__13.getRawRoot()).invoke(object15);
        }
        return null;
    }
}

