/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IRef
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2.atom.logged;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IRef;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.core2.atom.logged.LoggedAtom$fn__19780;
import datomic.core2.atom.logged.LoggedAtom$fn__19869;
import datomic.core2.atom.logged.LoggedAtom$fn__20023;
import datomic.core2.atom.logged.LoggedAtom$fn__20135;
import datomic.core2.atom.logged.LoggedAtomImpl;
import datomic.core2.atom.spi.DurableAtom;

public final class LoggedAtom
implements LoggedAtomImpl,
DurableAtom,
AutoCloseable,
IRef,
IType {
    public final Object log;
    public final Object close_ch;
    public final Object state_ref;
    public final Object serialize;
    public final Object deserialize;
    public final Object validator_ref;
    public final Object watches_ref;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"close!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__8 = RT.keyword(null, (String)"anom");
    public static final Keyword const__9 = RT.keyword(null, (String)"value");
    public static final Var const__10 = RT.var((String)"datomic.core2.anomalies", (String)"athrow");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__14 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__15 = RT.keyword((String)"cognitect.anomalies", (String)"incorrect");
    public static final Keyword const__16 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Keyword const__17 = RT.keyword((String)"datomic.core2.atom.logged", (String)"invalid-value");
    public static final Var const__18 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__19 = 1L;
    public static final Var const__20 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public LoggedAtom(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.log = object;
        this.close_ch = object2;
        this.state_ref = object3;
        this.serialize = object4;
        this.deserialize = object5;
        this.validator_ref = object6;
        this.watches_ref = object7;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"log"), Symbol.intern(null, (String)"close-ch"), Symbol.intern(null, (String)"state-ref"), Symbol.intern(null, (String)"serialize"), Symbol.intern(null, (String)"deserialize"), Symbol.intern(null, (String)"validator-ref"), Symbol.intern(null, (String)"watches-ref")});
    }

    @Override
    public Object _sync(Object ch) {
        Object captured_bindings__10231__auto__20173;
        Object c__10230__auto__20172 = ((IFn)const__18.getRawRoot()).invoke(const__19);
        Object object = captured_bindings__10231__auto__20173 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__20173 = null;
        ((IFn)const__20.getRawRoot()).invoke((Object)new LoggedAtom$fn__20135(this.serialize, this.state_ref, this, ch, this.validator_ref, this.deserialize, this.close_ch, object, c__10230__auto__20172, this.log, this.watches_ref));
        Object var1_1 = null;
        return ch;
    }

    @Override
    public Object _swap_vals_BANG_(Object f, Object ch) {
        Object captured_bindings__10231__auto__20175;
        Object c__10230__auto__20174 = ((IFn)const__18.getRawRoot()).invoke(const__19);
        Object object = captured_bindings__10231__auto__20175 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__20175 = null;
        Object object2 = f;
        f = null;
        ((IFn)const__20.getRawRoot()).invoke((Object)new LoggedAtom$fn__20023(object, ch, this.serialize, this.state_ref, c__10230__auto__20174, this.validator_ref, this, object2, this.deserialize, this.close_ch, this.log, this.watches_ref));
        Object var2_2 = null;
        return ch;
    }

    @Override
    public Object _read_latest() {
        Object captured_bindings__10231__auto__20177;
        Object c__10230__auto__20176 = ((IFn)const__18.getRawRoot()).invoke(const__19);
        Object object = captured_bindings__10231__auto__20177 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__20177 = null;
        ((IFn)const__20.getRawRoot()).invoke((Object)new LoggedAtom$fn__19869(this, this.serialize, this.state_ref, this.validator_ref, this.deserialize, this.close_ch, c__10230__auto__20176, this.log, this.watches_ref, object));
        Object var1_1 = null;
        return c__10230__auto__20176;
    }

    @Override
    public Object _validated_v(Object v) {
        Object object;
        Object temp__5802__auto__20178;
        Object object2 = temp__5802__auto__20178 = ((IFn)const__1.getRawRoot()).invoke(this.validator_ref);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object validator;
            Object object3 = temp__5802__auto__20178;
            temp__5802__auto__20178 = null;
            Object object4 = validator = object3;
            validator = null;
            Object object5 = ((IFn)new LoggedAtom$fn__19780(v, object4)).invoke();
            if (object5 != null && object5 != Boolean.FALSE) {
                object = v;
                v = null;
            } else {
                Object[] objectArray = new Object[6];
                objectArray[0] = const__14;
                objectArray[1] = const__15;
                objectArray[2] = const__16;
                objectArray[3] = "Invalid reference state";
                objectArray[4] = const__17;
                Object object6 = v;
                v = null;
                objectArray[5] = object6;
                object = RT.mapUniqueKeys((Object[])objectArray);
            }
        } else {
            object = v;
            Object var1_1 = null;
        }
        return object;
    }

    public IRef removeWatch(Object k) {
        Object object = k;
        k = null;
        ((IFn)const__11.getRawRoot()).invoke(this.watches_ref, const__13.getRawRoot(), object);
        return this;
    }

    public IRef addWatch(Object k, IFn f) {
        Object object = k;
        k = null;
        IFn iFn = f;
        f = null;
        ((IFn)const__11.getRawRoot()).invoke(this.watches_ref, const__12.getRawRoot(), object, (Object)iFn);
        return this;
    }

    public IPersistentMap getWatches() {
        LoggedAtom this_ = null;
        return (IPersistentMap)((IFn)const__1.getRawRoot()).invoke(this_.watches_ref);
    }

    public IFn getValidator() {
        LoggedAtom this_ = null;
        return (IFn)((IFn)const__1.getRawRoot()).invoke(this_.validator_ref);
    }

    public void setValidator(IFn vf) {
        IFn iFn = vf;
        vf = null;
        LoggedAtom this_ = null;
        ((IFn)const__11.getRawRoot()).invoke(this_.validator_ref, (Object)iFn);
    }

    public Object deref() {
        Object object;
        Object object2;
        Object map__19779 = ((IFn)const__1.getRawRoot()).invoke(this_.state_ref);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(map__19779);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__19779);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__19779;
                map__19779 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__4.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__5.getRawRoot()).invoke(map__19779);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__19779;
                    map__19779 = null;
                    object2 = ((IFn)const__6.getRawRoot()).invoke(object7);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__19779;
            map__19779 = null;
        }
        Object map__197792 = object2;
        Object anom2 = RT.get((Object)map__197792, (Object)const__8);
        Object object8 = map__197792;
        map__197792 = null;
        Object value = RT.get((Object)object8, (Object)const__9);
        Object object9 = anom2;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = anom2;
            anom2 = null;
            LoggedAtom this_ = null;
            object = ((IFn)const__10.getRawRoot()).invoke(object10);
        } else {
            object = value;
            value = null;
        }
        return object;
    }

    @Override
    public void close() throws Exception {
        LoggedAtom this_ = null;
        ((IFn)const__0.getRawRoot()).invoke(this_.close_ch);
    }
}

