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
import datomic.integrity$_main_STAR_$fn__22560;
import datomic.integrity$_main_STAR_$fn__22562;
import datomic.integrity$_main_STAR_$fn__22566;
import datomic.integrity$_main_STAR_$fn__22572;
import datomic.integrity$_main_STAR_$fn__22574;
import datomic.integrity$_main_STAR_$fn__22580;
import datomic.integrity$_main_STAR_$fn__22582;

public final class integrity$_main_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uri");
    public static final Keyword const__4 = RT.keyword(null, (String)"validate");
    public static final Var const__5 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__7 = RT.var((String)"clojure.pprint", (String)"pprint");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"prn");
    public static final Var const__10 = RT.var((String)"datomic.integrity", (String)"index-sort-root-keys");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__23 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"fulltext"), (Object)RT.keyword(null, (String)"fulltext-hist"));

    public static Object invokeStatic(Object p__22558) {
        Object object;
        Object object2;
        Object object3 = p__22558;
        p__22558 = null;
        Object map__22559 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__22559);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__22559;
            map__22559 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__22559;
            map__22559 = null;
        }
        Object map__225592 = object2;
        Object uri2 = RT.get((Object)map__225592, (Object)const__3);
        Object object6 = map__225592;
        map__225592 = null;
        Object validate = RT.get((Object)object6, (Object)const__4);
        Object uri3 = ((IFn)const__5.getRawRoot()).invoke(uri2);
        ((IFn)const__6.getRawRoot()).invoke((Object)"\nDiagnostics:");
        Object object7 = uri3;
        uri3 = null;
        ((IFn)const__7.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22560(object7)).invoke());
        Object object8 = validate;
        validate = null;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object k;
            Object k2;
            Object object9 = uri2;
            uri2 = null;
            uri3 = ((IFn)const__5.getRawRoot()).invoke(object9);
            ((IFn)const__6.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)"\nValidating database at ", uri3, (Object)".\nThis read-only process will read every segment of the database,\nreporting data to stdout and stacktraces to stderr.\nValidation can take a long time!"));
            ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing log tail identities:");
            ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22562(uri3)).invoke()));
            ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing log segments: ");
            ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22566(uri3)).invoke()));
            Object seq_22568 = ((IFn)const__1.getRawRoot()).invoke(const__10.getRawRoot());
            Object chunk_22569 = null;
            long count_22570 = 0L;
            long i_22571 = 0L;
            while (true) {
                Object temp__5457__auto__22586;
                if (i_22571 < count_22570) {
                    k2 = ((Indexed)chunk_22569).nth(RT.intCast((long)i_22571));
                    ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing segments in ", k2);
                    Object object10 = k2;
                    k2 = null;
                    ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22572(object10, uri3)).invoke()));
                    Object object11 = seq_22568;
                    seq_22568 = null;
                    Object object12 = chunk_22569;
                    chunk_22569 = null;
                    ++i_22571;
                    chunk_22569 = object12;
                    seq_22568 = object11;
                    continue;
                }
                Object object13 = seq_22568;
                seq_22568 = null;
                Object object14 = temp__5457__auto__22586 = ((IFn)const__1.getRawRoot()).invoke(object13);
                if (object14 == null || object14 == Boolean.FALSE) break;
                Object object15 = temp__5457__auto__22586;
                temp__5457__auto__22586 = null;
                Object seq_225682 = object15;
                Object object16 = ((IFn)const__14.getRawRoot()).invoke(seq_225682);
                if (object16 != null && object16 != Boolean.FALSE) {
                    Object c__5719__auto__22585 = ((IFn)const__15.getRawRoot()).invoke(seq_225682);
                    Object object17 = seq_225682;
                    seq_225682 = null;
                    Object object18 = c__5719__auto__22585;
                    Object object19 = c__5719__auto__22585;
                    c__5719__auto__22585 = null;
                    i_22571 = RT.intCast((long)0L);
                    count_22570 = RT.intCast((int)RT.count((Object)object19));
                    chunk_22569 = object18;
                    seq_22568 = ((IFn)const__16.getRawRoot()).invoke(object17);
                    continue;
                }
                k = ((IFn)const__19.getRawRoot()).invoke(seq_225682);
                ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing segments in ", k);
                Object object20 = k;
                k = null;
                ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22574(object20, uri3)).invoke()));
                Object object21 = seq_225682;
                seq_225682 = null;
                i_22571 = 0L;
                count_22570 = 0L;
                chunk_22569 = null;
                seq_22568 = ((IFn)const__20.getRawRoot()).invoke(object21);
            }
            Object seq_22576 = ((IFn)const__1.getRawRoot()).invoke((Object)const__23);
            Object chunk_22577 = null;
            long count_22578 = 0L;
            long i_22579 = 0L;
            while (true) {
                Object temp__5457__auto__22588;
                if (i_22579 < count_22578) {
                    k2 = ((Indexed)chunk_22577).nth(RT.intCast((long)i_22579));
                    ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing segments in ", k2);
                    Object object22 = k2;
                    k2 = null;
                    ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22580(uri3, object22)).invoke()));
                    Object object23 = seq_22576;
                    seq_22576 = null;
                    Object object24 = chunk_22577;
                    chunk_22577 = null;
                    ++i_22579;
                    chunk_22577 = object24;
                    seq_22576 = object23;
                    continue;
                }
                Object object25 = seq_22576;
                seq_22576 = null;
                Object object26 = temp__5457__auto__22588 = ((IFn)const__1.getRawRoot()).invoke(object25);
                if (object26 == null || object26 == Boolean.FALSE) break;
                Object object27 = temp__5457__auto__22588;
                temp__5457__auto__22588 = null;
                Object seq_225762 = object27;
                Object object28 = ((IFn)const__14.getRawRoot()).invoke(seq_225762);
                if (object28 != null && object28 != Boolean.FALSE) {
                    Object c__5719__auto__22587 = ((IFn)const__15.getRawRoot()).invoke(seq_225762);
                    Object object29 = seq_225762;
                    seq_225762 = null;
                    Object object30 = c__5719__auto__22587;
                    Object object31 = c__5719__auto__22587;
                    c__5719__auto__22587 = null;
                    i_22579 = RT.intCast((long)0L);
                    count_22578 = RT.intCast((int)RT.count((Object)object31));
                    chunk_22577 = object30;
                    seq_22576 = ((IFn)const__16.getRawRoot()).invoke(object29);
                    continue;
                }
                k = ((IFn)const__19.getRawRoot()).invoke(seq_225762);
                ((IFn)const__6.getRawRoot()).invoke((Object)"\nMissing segments in ", k);
                Object object32 = k;
                k = null;
                ((IFn)const__9.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)new integrity$_main_STAR_$fn__22582(uri3, object32)).invoke()));
                Object object33 = seq_225762;
                seq_225762 = null;
                i_22579 = 0L;
                count_22578 = 0L;
                chunk_22577 = null;
                seq_22576 = ((IFn)const__20.getRawRoot()).invoke(object33);
            }
            object = ((IFn)const__6.getRawRoot()).invoke((Object)"\nDone!");
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$_main_STAR_.invokeStatic(object2);
    }
}

