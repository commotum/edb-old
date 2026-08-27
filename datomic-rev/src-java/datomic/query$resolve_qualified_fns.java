/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class query$resolve_qualified_fns
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"where");
    public static final Keyword const__4 = RT.keyword(null, (String)"group");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"datomic.query", (String)"resolve-qualified-fn");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object p__19429) {
        Object clause;
        Object clause2;
        Object object;
        Object object2 = p__19429;
        p__19429 = null;
        Object map__19430 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__19430);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__19430;
            map__19430 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__19430;
            map__19430 = null;
        }
        Object map__194302 = object;
        Object where = RT.get((Object)map__194302, (Object)const__3);
        Object object5 = map__194302;
        map__194302 = null;
        Object group = RT.get((Object)object5, (Object)const__4);
        Object object6 = where;
        where = null;
        Object seq_19431 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), object6));
        Object chunk_19432 = null;
        long count_19433 = 0L;
        long i_19434 = 0L;
        while (true) {
            Object temp__5457__auto__19441;
            if (i_19434 < count_19433) {
                Object object7 = clause2 = ((Indexed)chunk_19432).nth(RT.intCast((long)i_19434));
                clause2 = null;
                ((IFn)const__9.getRawRoot()).invoke(object7);
                Object object8 = seq_19431;
                seq_19431 = null;
                Object object9 = chunk_19432;
                chunk_19432 = null;
                ++i_19434;
                chunk_19432 = object9;
                seq_19431 = object8;
                continue;
            }
            Object object10 = seq_19431;
            seq_19431 = null;
            Object object11 = temp__5457__auto__19441 = ((IFn)const__1.getRawRoot()).invoke(object10);
            if (object11 == null || object11 == Boolean.FALSE) break;
            Object object12 = temp__5457__auto__19441;
            temp__5457__auto__19441 = null;
            Object seq_194312 = object12;
            Object object13 = ((IFn)const__11.getRawRoot()).invoke(seq_194312);
            if (object13 != null && object13 != Boolean.FALSE) {
                Object c__5719__auto__19440 = ((IFn)const__12.getRawRoot()).invoke(seq_194312);
                Object object14 = seq_194312;
                seq_194312 = null;
                Object object15 = c__5719__auto__19440;
                Object object16 = c__5719__auto__19440;
                c__5719__auto__19440 = null;
                i_19434 = RT.intCast((long)0L);
                count_19433 = RT.intCast((int)RT.count((Object)object16));
                chunk_19432 = object15;
                seq_19431 = ((IFn)const__13.getRawRoot()).invoke(object14);
                continue;
            }
            Object object17 = clause = ((IFn)const__6.getRawRoot()).invoke(seq_194312);
            clause = null;
            ((IFn)const__9.getRawRoot()).invoke(object17);
            Object object18 = seq_194312;
            seq_194312 = null;
            i_19434 = 0L;
            count_19433 = 0L;
            chunk_19432 = null;
            seq_19431 = ((IFn)const__16.getRawRoot()).invoke(object18);
        }
        Object object19 = group;
        group = null;
        Object seq_19435 = ((IFn)const__1.getRawRoot()).invoke(object19);
        Object chunk_19436 = null;
        long count_19437 = 0L;
        long i_19438 = 0L;
        while (true) {
            Object temp__5457__auto__19443;
            if (i_19438 < count_19437) {
                Object object20 = clause2 = ((Indexed)chunk_19436).nth(RT.intCast((long)i_19438));
                clause2 = null;
                ((IFn)const__9.getRawRoot()).invoke(object20);
                Object object21 = seq_19435;
                seq_19435 = null;
                Object object22 = chunk_19436;
                chunk_19436 = null;
                ++i_19438;
                chunk_19436 = object22;
                seq_19435 = object21;
                continue;
            }
            Object object23 = seq_19435;
            seq_19435 = null;
            Object object24 = temp__5457__auto__19443 = ((IFn)const__1.getRawRoot()).invoke(object23);
            if (object24 == null || object24 == Boolean.FALSE) break;
            Object object25 = temp__5457__auto__19443;
            temp__5457__auto__19443 = null;
            Object seq_194352 = object25;
            Object object26 = ((IFn)const__11.getRawRoot()).invoke(seq_194352);
            if (object26 != null && object26 != Boolean.FALSE) {
                Object c__5719__auto__19442 = ((IFn)const__12.getRawRoot()).invoke(seq_194352);
                Object object27 = seq_194352;
                seq_194352 = null;
                Object object28 = c__5719__auto__19442;
                Object object29 = c__5719__auto__19442;
                c__5719__auto__19442 = null;
                i_19438 = RT.intCast((long)0L);
                count_19437 = RT.intCast((int)RT.count((Object)object29));
                chunk_19436 = object28;
                seq_19435 = ((IFn)const__13.getRawRoot()).invoke(object27);
                continue;
            }
            Object object30 = clause = ((IFn)const__6.getRawRoot()).invoke(seq_194352);
            clause = null;
            ((IFn)const__9.getRawRoot()).invoke(object30);
            Object object31 = seq_194352;
            seq_194352 = null;
            i_19438 = 0L;
            count_19437 = 0L;
            chunk_19436 = null;
            seq_19435 = ((IFn)const__16.getRawRoot()).invoke(object31);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$resolve_qualified_fns.invokeStatic(object2);
    }
}

