/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class transaction$log_completion_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"remove");
    public static final Var const__4 = RT.var((String)"datomic.transaction", (String)"log-event-map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Keyword const__7 = RT.keyword(null, (String)"read-at");
    public static final Keyword const__8 = RT.keyword(null, (String)"started-at");
    public static final Keyword const__9 = RT.keyword(null, (String)"applied-at");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__12 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__13 = RT.keyword(null, (String)"TransactionNsec");
    public static final Keyword const__14 = RT.keyword(null, (String)"TransactionApplyNsec");
    public static final Var const__15 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__18 = RT.keyword(null, (String)"event");
    public static final Keyword const__19 = RT.keyword((String)"tx", (String)"process");
    public static final Keyword const__20 = RT.keyword(null, (String)"txid");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__22 = RT.keyword(null, (String)"msec");
    public static final Keyword const__23 = RT.keyword(null, (String)"apply-msec");
    public static final Var const__24 = RT.var((String)"datomic.transaction", (String)"loggable-keys");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object ids) {
        long written_at = System.nanoTime();
        Object object = ids;
        ids = null;
        Object seq_15905 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object chunk_15906 = null;
        long count_15907 = 0L;
        long i_15908 = 0L;
        while (true) {
            Object temp__5457__auto__15932;
            Object temp__5457__auto__15933;
            if (i_15908 < count_15907) {
                Object temp__5457__auto__15922;
                Object id = ((Indexed)chunk_15906).nth(RT.intCast((long)i_15908));
                Object object2 = temp__5457__auto__15922 = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), id);
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object object3;
                    Object and__5236__auto__15921;
                    Object object4;
                    Object and__5236__auto__15919;
                    Object object5;
                    Object object6;
                    Object map__15909;
                    Object object7;
                    Object object8 = temp__5457__auto__15922;
                    temp__5457__auto__15922 = null;
                    Object map__159092 = object8;
                    Object object9 = ((IFn)const__5.getRawRoot()).invoke(map__159092);
                    if (object9 != null && object9 != Boolean.FALSE) {
                        Object object10 = map__159092;
                        map__159092 = null;
                        object7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__0.getRawRoot()).invoke(object10)));
                    } else {
                        object7 = map__159092;
                        map__159092 = null;
                    }
                    Object tx_info = map__15909 = object7;
                    Object read_at = RT.get((Object)map__15909, (Object)const__7);
                    Object started_at = RT.get((Object)map__15909, (Object)const__8);
                    Object object11 = map__15909;
                    map__15909 = null;
                    Object applied_at = RT.get((Object)object11, (Object)const__9);
                    long and__5236__auto__15915 = written_at;
                    Number number = Numbers.num((long)and__5236__auto__15915);
                    if (number != null && number != Boolean.FALSE) {
                        Object and__5236__auto__15914;
                        Object object12 = and__5236__auto__15914 = read_at;
                        if (object12 != null && object12 != Boolean.FALSE) {
                            object6 = Numbers.minus((long)written_at, (Object)read_at);
                        } else {
                            object6 = and__5236__auto__15914;
                            and__5236__auto__15914 = null;
                        }
                    } else {
                        object6 = Numbers.num((long)and__5236__auto__15915);
                    }
                    Number tx_nsec = object6;
                    long and__5236__auto__15917 = written_at;
                    Number number2 = Numbers.num((long)and__5236__auto__15917);
                    if (number2 != null && number2 != Boolean.FALSE) {
                        Object and__5236__auto__15916;
                        Object object13 = read_at;
                        read_at = null;
                        Object object14 = and__5236__auto__15916 = object13;
                        if (object14 != null && object14 != Boolean.FALSE) {
                            object5 = ((IFn)const__11.getRawRoot()).invoke((Object)tx_nsec);
                        } else {
                            object5 = and__5236__auto__15916;
                            and__5236__auto__15916 = null;
                        }
                    } else {
                        object5 = Numbers.num((long)and__5236__auto__15917);
                    }
                    Number msec = object5;
                    Object object15 = and__5236__auto__15919 = applied_at;
                    if (object15 != null && object15 != Boolean.FALSE) {
                        Object and__5236__auto__15918;
                        Object object16 = and__5236__auto__15918 = started_at;
                        if (object16 != null && object16 != Boolean.FALSE) {
                            object4 = Numbers.minus((Object)applied_at, (Object)started_at);
                        } else {
                            object4 = and__5236__auto__15918;
                            and__5236__auto__15918 = null;
                        }
                    } else {
                        object4 = and__5236__auto__15919;
                        and__5236__auto__15919 = null;
                    }
                    Object apply_nsec = object4;
                    Object object17 = applied_at;
                    applied_at = null;
                    Object object18 = and__5236__auto__15921 = object17;
                    if (object18 != null && object18 != Boolean.FALSE) {
                        Object and__5236__auto__15920;
                        Object object19 = started_at;
                        started_at = null;
                        Object object20 = and__5236__auto__15920 = object19;
                        if (object20 != null && object20 != Boolean.FALSE) {
                            object3 = ((IFn)const__11.getRawRoot()).invoke(apply_nsec);
                        } else {
                            object3 = and__5236__auto__15920;
                            and__5236__auto__15920 = null;
                        }
                    } else {
                        object3 = and__5236__auto__15921;
                        and__5236__auto__15921 = null;
                    }
                    Object apply_msec = object3;
                    Number number3 = msec;
                    if (number3 != null && number3 != Boolean.FALSE) {
                        Number number4 = tx_nsec;
                        tx_nsec = null;
                        ((IFn)const__12.getRawRoot()).invoke((Object)const__13, (Object)number4);
                    }
                    Object object21 = apply_msec;
                    if (object21 != null && object21 != Boolean.FALSE) {
                        Object object22 = apply_nsec;
                        apply_nsec = null;
                        ((IFn)const__12.getRawRoot()).invoke((Object)const__14, object22);
                    }
                    Logger logger = LoggerFactory.getLogger((String)"datomic.transaction");
                    if (logger.isInfoEnabled()) {
                        Object object23;
                        Object object24;
                        Logger logger2 = logger;
                        logger = null;
                        IFn iFn = (IFn)const__15.getRawRoot();
                        IFn iFn2 = (IFn)const__16.getRawRoot();
                        Object[] objectArray = new Object[4];
                        objectArray[0] = const__18;
                        objectArray[1] = const__19;
                        objectArray[2] = const__20;
                        Object object25 = id;
                        id = null;
                        objectArray[3] = object25;
                        Object object26 = tx_info;
                        tx_info = null;
                        Object G__15910 = ((IFn)const__17.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object26);
                        Number number5 = msec;
                        if (number5 != null && number5 != Boolean.FALSE) {
                            Object object27 = G__15910;
                            G__15910 = null;
                            Number number6 = msec;
                            msec = null;
                            object24 = ((IFn)const__21.getRawRoot()).invoke(object27, (Object)const__22, (Object)number6);
                        } else {
                            object24 = G__15910;
                            G__15910 = null;
                        }
                        Object G__159102 = object24;
                        Object object28 = apply_msec;
                        if (object28 != null && object28 != Boolean.FALSE) {
                            Object object29 = G__159102;
                            G__159102 = null;
                            Object object30 = apply_msec;
                            apply_msec = null;
                            object23 = ((IFn)const__21.getRawRoot()).invoke(object29, (Object)const__23, object30);
                        } else {
                            object23 = G__159102;
                            G__159102 = null;
                        }
                        logger2.info((String)iFn.invoke(iFn2.invoke(object23, const__24.getRawRoot())));
                    }
                }
                Object object31 = seq_15905;
                seq_15905 = null;
                Object object32 = chunk_15906;
                chunk_15906 = null;
                ++i_15908;
                chunk_15906 = object32;
                seq_15905 = object31;
                continue;
            }
            Object object33 = seq_15905;
            seq_15905 = null;
            Object object34 = temp__5457__auto__15933 = ((IFn)const__0.getRawRoot()).invoke(object33);
            if (object34 == null || object34 == Boolean.FALSE) break;
            Object object35 = temp__5457__auto__15933;
            temp__5457__auto__15933 = null;
            Object seq_159052 = object35;
            Object object36 = ((IFn)const__26.getRawRoot()).invoke(seq_159052);
            if (object36 != null && object36 != Boolean.FALSE) {
                Object c__5719__auto__15923 = ((IFn)const__27.getRawRoot()).invoke(seq_159052);
                Object object37 = seq_159052;
                seq_159052 = null;
                Object object38 = c__5719__auto__15923;
                Object object39 = c__5719__auto__15923;
                c__5719__auto__15923 = null;
                i_15908 = RT.intCast((long)0L);
                count_15907 = RT.intCast((int)RT.count((Object)object39));
                chunk_15906 = object38;
                seq_15905 = ((IFn)const__28.getRawRoot()).invoke(object37);
                continue;
            }
            Object id = ((IFn)const__31.getRawRoot()).invoke(seq_159052);
            Object object40 = temp__5457__auto__15932 = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), id);
            if (object40 != null && object40 != Boolean.FALSE) {
                Object object41;
                Object and__5236__auto__15931;
                Object object42;
                Object and__5236__auto__15929;
                Object object43;
                Object object44;
                Object map__15911;
                Object object45;
                Object object46 = temp__5457__auto__15932;
                temp__5457__auto__15932 = null;
                Object map__159112 = object46;
                Object object47 = ((IFn)const__5.getRawRoot()).invoke(map__159112);
                if (object47 != null && object47 != Boolean.FALSE) {
                    Object object48 = map__159112;
                    map__159112 = null;
                    object45 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__0.getRawRoot()).invoke(object48)));
                } else {
                    object45 = map__159112;
                    map__159112 = null;
                }
                Object tx_info = map__15911 = object45;
                Object read_at = RT.get((Object)map__15911, (Object)const__7);
                Object started_at = RT.get((Object)map__15911, (Object)const__8);
                Object object49 = map__15911;
                map__15911 = null;
                Object applied_at = RT.get((Object)object49, (Object)const__9);
                long and__5236__auto__15925 = written_at;
                Number number = Numbers.num((long)and__5236__auto__15925);
                if (number != null && number != Boolean.FALSE) {
                    Object and__5236__auto__15924;
                    Object object50 = and__5236__auto__15924 = read_at;
                    if (object50 != null && object50 != Boolean.FALSE) {
                        object44 = Numbers.minus((long)written_at, (Object)read_at);
                    } else {
                        object44 = and__5236__auto__15924;
                        and__5236__auto__15924 = null;
                    }
                } else {
                    object44 = Numbers.num((long)and__5236__auto__15925);
                }
                Number tx_nsec = object44;
                long and__5236__auto__15927 = written_at;
                Number number7 = Numbers.num((long)and__5236__auto__15927);
                if (number7 != null && number7 != Boolean.FALSE) {
                    Object and__5236__auto__15926;
                    Object object51 = read_at;
                    read_at = null;
                    Object object52 = and__5236__auto__15926 = object51;
                    if (object52 != null && object52 != Boolean.FALSE) {
                        object43 = ((IFn)const__11.getRawRoot()).invoke((Object)tx_nsec);
                    } else {
                        object43 = and__5236__auto__15926;
                        and__5236__auto__15926 = null;
                    }
                } else {
                    object43 = Numbers.num((long)and__5236__auto__15927);
                }
                Number msec = object43;
                Object object53 = and__5236__auto__15929 = applied_at;
                if (object53 != null && object53 != Boolean.FALSE) {
                    Object and__5236__auto__15928;
                    Object object54 = and__5236__auto__15928 = started_at;
                    if (object54 != null && object54 != Boolean.FALSE) {
                        object42 = Numbers.minus((Object)applied_at, (Object)started_at);
                    } else {
                        object42 = and__5236__auto__15928;
                        and__5236__auto__15928 = null;
                    }
                } else {
                    object42 = and__5236__auto__15929;
                    and__5236__auto__15929 = null;
                }
                Object apply_nsec = object42;
                Object object55 = applied_at;
                applied_at = null;
                Object object56 = and__5236__auto__15931 = object55;
                if (object56 != null && object56 != Boolean.FALSE) {
                    Object and__5236__auto__15930;
                    Object object57 = started_at;
                    started_at = null;
                    Object object58 = and__5236__auto__15930 = object57;
                    if (object58 != null && object58 != Boolean.FALSE) {
                        object41 = ((IFn)const__11.getRawRoot()).invoke(apply_nsec);
                    } else {
                        object41 = and__5236__auto__15930;
                        and__5236__auto__15930 = null;
                    }
                } else {
                    object41 = and__5236__auto__15931;
                    and__5236__auto__15931 = null;
                }
                Object apply_msec = object41;
                Number number8 = msec;
                if (number8 != null && number8 != Boolean.FALSE) {
                    Number number9 = tx_nsec;
                    tx_nsec = null;
                    ((IFn)const__12.getRawRoot()).invoke((Object)const__13, (Object)number9);
                }
                Object object59 = apply_msec;
                if (object59 != null && object59 != Boolean.FALSE) {
                    Object object60 = apply_nsec;
                    apply_nsec = null;
                    ((IFn)const__12.getRawRoot()).invoke((Object)const__14, object60);
                }
                Logger logger = LoggerFactory.getLogger((String)"datomic.transaction");
                if (logger.isInfoEnabled()) {
                    Object object61;
                    Object object62;
                    Logger logger3 = logger;
                    logger = null;
                    IFn iFn = (IFn)const__15.getRawRoot();
                    IFn iFn3 = (IFn)const__16.getRawRoot();
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__18;
                    objectArray[1] = const__19;
                    objectArray[2] = const__20;
                    Object object63 = id;
                    id = null;
                    objectArray[3] = object63;
                    Object object64 = tx_info;
                    tx_info = null;
                    Object G__15912 = ((IFn)const__17.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object64);
                    Number number10 = msec;
                    if (number10 != null && number10 != Boolean.FALSE) {
                        Object object65 = G__15912;
                        G__15912 = null;
                        Number number11 = msec;
                        msec = null;
                        object62 = ((IFn)const__21.getRawRoot()).invoke(object65, (Object)const__22, (Object)number11);
                    } else {
                        object62 = G__15912;
                        G__15912 = null;
                    }
                    Object G__159122 = object62;
                    Object object66 = apply_msec;
                    if (object66 != null && object66 != Boolean.FALSE) {
                        Object object67 = G__159122;
                        G__159122 = null;
                        Object object68 = apply_msec;
                        apply_msec = null;
                        object61 = ((IFn)const__21.getRawRoot()).invoke(object67, (Object)const__23, object68);
                    } else {
                        object61 = G__159122;
                        G__159122 = null;
                    }
                    logger3.info((String)iFn.invoke(iFn3.invoke(object61, const__24.getRawRoot())));
                }
            }
            Object object69 = seq_159052;
            seq_159052 = null;
            i_15908 = 0L;
            count_15907 = 0L;
            chunk_15906 = null;
            seq_15905 = ((IFn)const__32.getRawRoot()).invoke(object69);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$log_completion_BANG_.invokeStatic(object2);
    }
}

