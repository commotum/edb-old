/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$selfcheck_index_cli$progress__22123;
import datomic.integrity$selfcheck_index_cli$progress__22125;

public final class integrity$selfcheck_index_cli
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uri");
    public static final Var const__4 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"connect");
    public static final Var const__6 = RT.var((String)"datomic.api", (String)"db");
    public static final AFn const__11 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"vaet"));
    public static final Object const__12 = 0L;
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__17 = RT.var((String)"datomic.integrity", (String)"unfindable-datom-seq");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Keyword const__21 = RT.keyword(null, (String)"datom");
    public static final Keyword const__22 = RT.keyword(null, (String)"index");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object p__22117) {
        Object conn;
        Object uri2;
        Object uri3;
        Object map__22118;
        Object object;
        Object object2 = p__22117;
        p__22117 = null;
        Object map__221182 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__221182);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__221182;
            map__221182 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__221182;
            map__221182 = null;
        }
        Object object5 = map__22118 = object;
        map__22118 = null;
        Object object6 = uri3 = RT.get((Object)object5, (Object)const__3);
        uri3 = null;
        Object object7 = uri2 = ((IFn)const__4.getRawRoot()).invoke(object6);
        uri2 = null;
        Object object8 = conn = ((IFn)const__5.getRawRoot()).invoke(object7);
        conn = null;
        Object db2 = ((IFn)const__6.getRawRoot()).invoke(object8);
        Object seq_22119 = ((IFn)const__1.getRawRoot()).invoke((Object)const__11);
        Object chunk_22120 = null;
        long count_22121 = 0L;
        long i_22122 = 0L;
        while (true) {
            Object problem;
            integrity$selfcheck_index_cli$progress__22125 progress;
            Object temp__5457__auto__22129;
            if (i_22122 < count_22121) {
                Object problem2;
                integrity$selfcheck_index_cli$progress__22123 progress2;
                Object index2 = ((Indexed)chunk_22120).nth(RT.intCast((long)i_22122));
                ((IFn)const__14.getRawRoot()).invoke((Object)"Self-checking ", index2);
                Object count2 = ((IFn)const__15.getRawRoot()).invoke(const__12);
                integrity$selfcheck_index_cli$progress__22123 integrity$selfcheck_index_cli$progress__22123 = progress2 = new integrity$selfcheck_index_cli$progress__22123(count2);
                progress2 = null;
                Object object9 = problem2 = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(db2, index2, (Object)integrity$selfcheck_index_cli$progress__22123));
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object object10 = ((IFn)const__19.getRawRoot()).invoke((Object)"Unable to seek to ", ((IFn)const__20.getRawRoot()).invoke(problem2), (Object)" in ", index2);
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__21;
                    Object object11 = problem2;
                    problem2 = null;
                    objectArray[1] = object11;
                    objectArray[2] = const__22;
                    Object object12 = index2;
                    index2 = null;
                    objectArray[3] = object12;
                    throw (Throwable)((IFn)const__18.getRawRoot()).invoke(object10, (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                Object object13 = count2;
                count2 = null;
                ((IFn)const__23.getRawRoot()).invoke((Object)"\nChecked ", ((IFn)const__24.getRawRoot()).invoke(object13), (Object)" datoms");
                Object object14 = seq_22119;
                seq_22119 = null;
                Object object15 = chunk_22120;
                chunk_22120 = null;
                ++i_22122;
                chunk_22120 = object15;
                seq_22119 = object14;
                continue;
            }
            Object object16 = seq_22119;
            seq_22119 = null;
            Object object17 = temp__5457__auto__22129 = ((IFn)const__1.getRawRoot()).invoke(object16);
            if (object17 == null || object17 == Boolean.FALSE) break;
            Object object18 = temp__5457__auto__22129;
            temp__5457__auto__22129 = null;
            Object seq_221192 = object18;
            Object object19 = ((IFn)const__26.getRawRoot()).invoke(seq_221192);
            if (object19 != null && object19 != Boolean.FALSE) {
                Object c__5719__auto__22128 = ((IFn)const__27.getRawRoot()).invoke(seq_221192);
                Object object20 = seq_221192;
                seq_221192 = null;
                Object object21 = c__5719__auto__22128;
                Object object22 = c__5719__auto__22128;
                c__5719__auto__22128 = null;
                i_22122 = RT.intCast((long)0L);
                count_22121 = RT.intCast((int)RT.count((Object)object22));
                chunk_22120 = object21;
                seq_22119 = ((IFn)const__28.getRawRoot()).invoke(object20);
                continue;
            }
            Object index3 = ((IFn)const__16.getRawRoot()).invoke(seq_221192);
            ((IFn)const__14.getRawRoot()).invoke((Object)"Self-checking ", index3);
            Object count3 = ((IFn)const__15.getRawRoot()).invoke(const__12);
            integrity$selfcheck_index_cli$progress__22125 integrity$selfcheck_index_cli$progress__22125 = progress = new integrity$selfcheck_index_cli$progress__22125(count3);
            progress = null;
            Object object23 = problem = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(db2, index3, (Object)integrity$selfcheck_index_cli$progress__22125));
            if (object23 != null && object23 != Boolean.FALSE) {
                Object object24 = ((IFn)const__19.getRawRoot()).invoke((Object)"Unable to seek to ", ((IFn)const__20.getRawRoot()).invoke(problem), (Object)" in ", index3);
                Object[] objectArray = new Object[4];
                objectArray[0] = const__21;
                Object object25 = problem;
                problem = null;
                objectArray[1] = object25;
                objectArray[2] = const__22;
                Object object26 = index3;
                index3 = null;
                objectArray[3] = object26;
                throw (Throwable)((IFn)const__18.getRawRoot()).invoke(object24, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            Object object27 = count3;
            count3 = null;
            ((IFn)const__23.getRawRoot()).invoke((Object)"\nChecked ", ((IFn)const__24.getRawRoot()).invoke(object27), (Object)" datoms");
            Object object28 = seq_221192;
            seq_221192 = null;
            i_22122 = 0L;
            count_22121 = 0L;
            chunk_22120 = null;
            seq_22119 = ((IFn)const__31.getRawRoot()).invoke(object28);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$selfcheck_index_cli.invokeStatic(object2);
    }
}

