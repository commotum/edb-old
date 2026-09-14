/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.log.ddb;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.log.ddb.Log$fn__20770$G__20651__20771;
import datomic.core2.log.ddb.Log$fn__20770$G__20652__20773;
import datomic.core2.log.ddb.Log$fn__20770$G__20653__20775;
import datomic.core2.log.ddb.Log$fn__20770$G__20654__20777;
import datomic.core2.log.ddb.Log$fn__20770$G__20655__20779;
import datomic.core2.log.ddb.Log$fn__20770$G__20656__20781;
import datomic.core2.log.ddb.Log$fn__20770$G__20657__20783;
import datomic.core2.log.ddb.Log$fn__20770$G__20658__20785;
import datomic.core2.log.ddb.Log$fn__20770$G__20659__20787;
import datomic.core2.log.ddb.Log$fn__20770$G__20660__20789;
import datomic.core2.log.ddb.Log$fn__20770$G__20661__20791;
import datomic.core2.log.ddb.Log$fn__20770$G__20662__20793;
import datomic.core2.log.ddb.Log$fn__20770$G__20663__20795;
import datomic.core2.log.ddb.Log$fn__20770$state_machine__9975__auto____20797;

public final class Log$fn__20770
extends AFunction {
    Object limit;
    Object query;
    Object map__20643;
    Object ch;
    Object direction;
    Object next_r;
    Object table;
    Object _;
    Object c__10230__auto__;
    Object chunk_size;
    Object opts;
    Object p;
    Object t;
    Object captured_bindings__10231__auto__;
    Object client;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public Log$fn__20770(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.limit = object;
        this.query = object2;
        this.map__20643 = object3;
        this.ch = object4;
        this.direction = object5;
        this.next_r = object6;
        this.table = object7;
        this._ = object8;
        this.c__10230__auto__ = object9;
        this.chunk_size = object10;
        this.opts = object11;
        this.p = object12;
        this.t = object13;
        this.captured_bindings__10231__auto__ = object14;
        this.client = object15;
    }

    public Object invoke() {
        Object state__10233__auto__20854;
        Log$fn__20770$state_machine__9975__auto____20797 f__10232__auto__20853;
        this_.t = null;
        Log$fn__20770$G__20651__20771 G__20651 = new Log$fn__20770$G__20651__20771(this_.t);
        this_.query = null;
        Log$fn__20770$G__20652__20773 G__20652 = new Log$fn__20770$G__20652__20773(this_.query);
        Log$fn__20770$G__20653__20775 G__20653 = new Log$fn__20770$G__20653__20775(this_.chunk_size);
        this_.direction = null;
        Log$fn__20770$G__20654__20777 G__20654 = new Log$fn__20770$G__20654__20777(this_.direction);
        Log$fn__20770$G__20655__20779 G__20655 = new Log$fn__20770$G__20655__20779(this_.p);
        this_.next_r = null;
        Log$fn__20770$G__20656__20781 G__20656 = new Log$fn__20770$G__20656__20781(this_.next_r);
        Log$fn__20770$G__20657__20783 G__20657 = new Log$fn__20770$G__20657__20783(this_.table);
        this_.limit = null;
        Log$fn__20770$G__20658__20785 G__20658 = new Log$fn__20770$G__20658__20785(this_.limit);
        Log$fn__20770$G__20659__20787 G__20659 = new Log$fn__20770$G__20659__20787(this_._);
        Log$fn__20770$G__20660__20789 G__20660 = new Log$fn__20770$G__20660__20789(this_.client);
        this_.ch = null;
        Log$fn__20770$G__20661__20791 G__20661 = new Log$fn__20770$G__20661__20791(this_.ch);
        this_.map__20643 = null;
        Log$fn__20770$G__20662__20793 G__20662 = new Log$fn__20770$G__20662__20793(this_.map__20643);
        this_.opts = null;
        Log$fn__20770$G__20663__20795 G__20663 = new Log$fn__20770$G__20663__20795(this_.opts);
        Log$fn__20770$G__20652__20773 log$fn__20770$G__20652__20773 = G__20652;
        G__20652 = null;
        Log$fn__20770$G__20663__20795 log$fn__20770$G__20663__20795 = G__20663;
        G__20663 = null;
        Log$fn__20770$G__20657__20783 log$fn__20770$G__20657__20783 = G__20657;
        G__20657 = null;
        Log$fn__20770$G__20659__20787 log$fn__20770$G__20659__20787 = G__20659;
        G__20659 = null;
        Log$fn__20770$G__20662__20793 log$fn__20770$G__20662__20793 = G__20662;
        G__20662 = null;
        Log$fn__20770$G__20653__20775 log$fn__20770$G__20653__20775 = G__20653;
        G__20653 = null;
        Log$fn__20770$G__20661__20791 log$fn__20770$G__20661__20791 = G__20661;
        G__20661 = null;
        Log$fn__20770$G__20658__20785 log$fn__20770$G__20658__20785 = G__20658;
        G__20658 = null;
        Log$fn__20770$G__20651__20771 log$fn__20770$G__20651__20771 = G__20651;
        G__20651 = null;
        Log$fn__20770$G__20655__20779 log$fn__20770$G__20655__20779 = G__20655;
        G__20655 = null;
        Log$fn__20770$G__20660__20789 log$fn__20770$G__20660__20789 = G__20660;
        G__20660 = null;
        Log$fn__20770$G__20654__20777 log$fn__20770$G__20654__20777 = G__20654;
        G__20654 = null;
        Log$fn__20770$G__20656__20781 log$fn__20770$G__20656__20781 = G__20656;
        G__20656 = null;
        Log$fn__20770$state_machine__9975__auto____20797 log$fn__20770$state_machine__9975__auto____20797 = f__10232__auto__20853 = new Log$fn__20770$state_machine__9975__auto____20797((Object)log$fn__20770$G__20652__20773, (Object)log$fn__20770$G__20663__20795, (Object)log$fn__20770$G__20657__20783, (Object)log$fn__20770$G__20659__20787, (Object)log$fn__20770$G__20662__20793, (Object)log$fn__20770$G__20653__20775, (Object)log$fn__20770$G__20661__20791, (Object)log$fn__20770$G__20658__20785, (Object)log$fn__20770$G__20651__20771, (Object)log$fn__20770$G__20655__20779, (Object)log$fn__20770$G__20660__20789, (Object)log$fn__20770$G__20654__20777, (Object)log$fn__20770$G__20656__20781);
        f__10232__auto__20853 = null;
        Object statearr_20851 = ((IFn)log$fn__20770$state_machine__9975__auto____20797).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20851, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20851, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20851;
        statearr_20851 = null;
        Object object2 = state__10233__auto__20854 = object;
        state__10233__auto__20854 = null;
        Log$fn__20770 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

