(do
  (clojure.core/in-ns (.withMeta 'datomic.db {:author "Rich Hickey"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.db {:author "Rich Hickey"}))
    {:doc "db core data structures", :author "Rich Hickey"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core :exclude ['resolve 'compare 'qualified-symbol?])
      (clojure.core/use 'datomic.btset ['datomic.common :only ['compare]])
      (clojure.core/require
        ['clojure.set :as 'set]
        ['clojure.pprint :as 'pp]
        ['clojure.string :as 'str]
        ['clojure.edn :as 'edn]
        'datomic.validators
        ['datomic.common :as 'common :refer (clojure.core/list 'qualified-symbol?)]
        ['datomic.config :as 'config]
        ['datomic.core2.thread :as 'thread]
        ['datomic.math :as 'math]
        ['datomic.measure.io-stats :as 'io-stats]
        ['datomic.measure.io-trace :as 'io-trace]
        ['datomic.io :as 'io]
        ['datomic.iter :as 'iter :refer (clojure.core/list 'iget 'inext)]
        ['datomic.fressian :as 'fressian]
        ['datomic.janino :as 'janino]
        ['datomic.monitor :as 'monitor]
        ['datomic.error :as 'error]
        ['datomic.slf4j :as 'logger]
        ['datomic.fulltext-index :as 'ftindex])
      (clojure.core/import 'datomic.Database)
      (clojure.core/import 'datomic.Datom)
      (clojure.core/import 'datomic.Database$Predicate)
      (clojure.core/import 'datomic.btset.IDataSet)
      (clojure.core/import 'datomic.iter.Iter)
      (clojure.core/import 'datomic.impl.Circular)
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'java.util.Comparator)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.HashMap)
      (clojure.core/import 'java.util.HashSet)
      (clojure.core/import 'java.util.Map)
      (clojure.core/import 'java.util.Map$Entry)
      (clojure.core/import 'java.util.Date)
      (clojure.core/import 'java.util.UUID)
      (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
      (clojure.core/import 'java.util.concurrent.TimeUnit)
      (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
      (clojure.core/import 'java.util.concurrent.Executor)
      (clojure.core/import 'java.util.concurrent.atomic.LongAdder)
      (clojure.core/import 'java.util.concurrent.atomic.AtomicBoolean)
      (clojure.core/import 'java.net.URI)
      (clojure.core/import 'org.fressian.handlers.WriteHandlerLookup)
      (clojure.core/import 'org.fressian.handlers.IWriteHandlerLookup)))
  (when-not (.equals (.withMeta 'datomic.db {:author "Rich Hickey"}) 'clojure.core)
    (dosync
      (commute
        (deref #'clojure.core/*loaded-libs*)
        conj
        (.withMeta 'datomic.db {:author "Rich Hickey"})))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core :exclude ['resolve 'compare 'qualified-symbol?])
        (clojure.core/use 'datomic.btset ['datomic.common :only ['compare]])
        (clojure.core/require
          ['clojure.set :as 'set]
          ['clojure.pprint :as 'pp]
          ['clojure.string :as 'str]
          ['clojure.edn :as 'edn]
          'datomic.validators
          ['datomic.common :as 'common :refer (clojure.core/list 'qualified-symbol?)]
          ['datomic.config :as 'config]
          ['datomic.core2.thread :as 'thread]
          ['datomic.math :as 'math]
          ['datomic.measure.io-stats :as 'io-stats]
          ['datomic.measure.io-trace :as 'io-trace]
          ['datomic.io :as 'io]
          ['datomic.iter :as 'iter :refer (clojure.core/list 'iget 'inext)]
          ['datomic.fressian :as 'fressian]
          ['datomic.janino :as 'janino]
          ['datomic.monitor :as 'monitor]
          ['datomic.error :as 'error]
          ['datomic.slf4j :as 'logger]
          ['datomic.fulltext-index :as 'ftindex])
        (clojure.core/import 'datomic.Database)
        (clojure.core/import 'datomic.Datom)
        (clojure.core/import 'datomic.Database$Predicate)
        (clojure.core/import 'datomic.btset.IDataSet)
        (clojure.core/import 'datomic.iter.Iter)
        (clojure.core/import 'datomic.impl.Circular)
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'java.util.Comparator)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.HashMap)
        (clojure.core/import 'java.util.HashSet)
        (clojure.core/import 'java.util.Map)
        (clojure.core/import 'java.util.Map$Entry)
        (clojure.core/import 'java.util.Date)
        (clojure.core/import 'java.util.UUID)
        (clojure.core/import 'java.util.concurrent.LinkedBlockingQueue)
        (clojure.core/import 'java.util.concurrent.TimeUnit)
        (clojure.core/import 'java.util.concurrent.ThreadPoolExecutor)
        (clojure.core/import 'java.util.concurrent.Executor)
        (clojure.core/import 'java.util.concurrent.atomic.LongAdder)
        (clojure.core/import 'java.util.concurrent.atomic.AtomicBoolean)
        (clojure.core/import 'java.net.URI)
        (clojure.core/import 'org.fressian.handlers.WriteHandlerLookup)
        (clojure.core/import 'org.fressian.handlers.IWriteHandlerLookup))))
  (set! *warn-on-reflection* true)
  (set! *unchecked-math* true)
  (def BOOT-IDS
   {:db.type/instant 25,
    :db/excise 15,
    :db.type/boolean 24,
    :db.unique/identity 38,
    :db/fn 52,
    :db.type/bytes 27,
    :db/index 44,
    :db/unique 42,
    :db.part/user 4,
    :db.lang/clojure 48,
    :db.excise/beforeT 17,
    :db.part/db 0,
    :db.bootstrap/part 53,
    :db.sys/reId 9,
    :db/valueType 40,
    :db.type/string 23,
    :db.type/keyword 21,
    :db/txInstant 50,
    :db.type/ref 20,
    :db/noHistory 45,
    :db/isComponent 43,
    :db/lang 46,
    :db/fulltext 51,
    :db/system-tx 7,
    :db.unique/value 37,
    :db/retract 2,
    :db.lang/java 49,
    :db.part/tx 3,
    :db/cardinality 41,
    :db.excise/before 18,
    :db/ident 10,
    :db/code 47,
    :db/add 1,
    :db.type/long 22,
    :db.cardinality/many 36,
    :db.install/valueType 12,
    :db.alter/attribute 19,
    :db.install/function 14,
    :db.install/partition 11,
    :db.install/attribute 13,
    :db.type/fn 26,
    :db.cardinality/one 35,
    :db.excise/attrs 16,
    :fressian/tag 39,
    :db.sys/partiallyIndexed 8})
  (def max-boot-id (apply max (vals datomic.db/BOOT-IDS)))
  (reset-meta! #'max-boot-id (assoc {:private true, :column 1} :name 'max-boot-id :ns *ns*))
  (def PART_SYS (datomic.db/BOOT-IDS :db.part/db))
  (reset-meta! #'PART_SYS (assoc {:const true, :column 1} :name 'PART_SYS :ns *ns*))
  (def OP_ASSERT (datomic.db/BOOT-IDS :db/add))
  (reset-meta! #'OP_ASSERT (assoc {:const true, :column 1} :name 'OP_ASSERT :ns *ns*))
  (def OP_RETRACT (datomic.db/BOOT-IDS :db/retract))
  (reset-meta! #'OP_RETRACT (assoc {:const true, :column 1} :name 'OP_RETRACT :ns *ns*))
  (def OP_EXCISE (datomic.db/BOOT-IDS :db/excise))
  (reset-meta! #'OP_EXCISE (assoc {:const true, :column 1} :name 'OP_EXCISE :ns *ns*))
  (def SYS_RE_ID (datomic.db/BOOT-IDS :db.sys/reId))
  (reset-meta! #'SYS_RE_ID (assoc {:const true, :column 1} :name 'SYS_RE_ID :ns *ns*))
  (def SYS_PARTIALLY_INDEXED (datomic.db/BOOT-IDS :db.sys/partiallyIndexed))
  (reset-meta!
    #'SYS_PARTIALLY_INDEXED
    (assoc {:const true, :column 1} :name 'SYS_PARTIALLY_INDEXED :ns *ns*))
  (def EXCISE_ATTRS (datomic.db/BOOT-IDS :db.excise/attrs))
  (reset-meta! #'EXCISE_ATTRS (assoc {:const true, :column 1} :name 'EXCISE_ATTRS :ns *ns*))
  (def EXCISE_BEFORE_T (datomic.db/BOOT-IDS :db.excise/beforeT))
  (reset-meta! #'EXCISE_BEFORE_T (assoc {:const true, :column 1} :name 'EXCISE_BEFORE_T :ns *ns*))
  (def EXCISE_BEFORE (datomic.db/BOOT-IDS :db.excise/before))
  (reset-meta! #'EXCISE_BEFORE (assoc {:const true, :column 1} :name 'EXCISE_BEFORE :ns *ns*))
  (def PART_TX (datomic.db/BOOT-IDS :db.part/tx))
  (reset-meta! #'PART_TX (assoc {:const true, :column 1} :name 'PART_TX :ns *ns*))
  (def PART_USER (datomic.db/BOOT-IDS :db.part/user))
  (reset-meta! #'PART_USER (assoc {:const true, :column 1} :name 'PART_USER :ns *ns*))
  (def RESERVED_PARTITIONS 17)
  (reset-meta!
    #'RESERVED_PARTITIONS
    (assoc {:const true, :column 1} :name 'RESERVED_PARTITIONS :ns *ns*))
  (def MAX_PARTITION 16383)
  (reset-meta! #'MAX_PARTITION (assoc {:const true, :column 1} :name 'MAX_PARTITION :ns *ns*))
  (def PART_PENDING 16)
  (reset-meta! #'PART_PENDING (assoc {:const true, :column 1} :name 'PART_PENDING :ns *ns*))
  (def IMPLICIT_PARTITION_START (long (bit-shift-left 1 19)))
  (reset-meta!
    #'IMPLICIT_PARTITION_START
    (assoc {:const true, :column 1} :name 'IMPLICIT_PARTITION_START :ns *ns*))
  (def IDENT (datomic.db/BOOT-IDS :db/ident))
  (reset-meta! #'IDENT (assoc {:const true, :column 1} :name 'IDENT :ns *ns*))
  (def UNIQUE_IDENTITY (datomic.db/BOOT-IDS :db.unique/identity))
  (reset-meta! #'UNIQUE_IDENTITY (assoc {:const true, :column 1} :name 'UNIQUE_IDENTITY :ns *ns*))
  (def UNIQUE_VALUE (datomic.db/BOOT-IDS :db.unique/value))
  (reset-meta! #'UNIQUE_VALUE (assoc {:const true, :column 1} :name 'UNIQUE_VALUE :ns *ns*))
  (def INSTALL_PARTITION (datomic.db/BOOT-IDS :db.install/partition))
  (reset-meta!
    #'INSTALL_PARTITION
    (assoc {:const true, :column 1} :name 'INSTALL_PARTITION :ns *ns*))
  (def INSTALL_VTYPE (datomic.db/BOOT-IDS :db.install/valueType))
  (reset-meta! #'INSTALL_VTYPE (assoc {:const true, :column 1} :name 'INSTALL_VTYPE :ns *ns*))
  (def INSTALL_ATTRIBUTE (datomic.db/BOOT-IDS :db.install/attribute))
  (reset-meta!
    #'INSTALL_ATTRIBUTE
    (assoc {:const true, :column 1} :name 'INSTALL_ATTRIBUTE :ns *ns*))
  (def INSTALL_FUNCTION (datomic.db/BOOT-IDS :db.install/function))
  (reset-meta!
    #'INSTALL_FUNCTION
    (assoc {:const true, :column 1} :name 'INSTALL_FUNCTION :ns *ns*))
  (def ALTER_ATTRIBUTE (datomic.db/BOOT-IDS :db.alter/attribute))
  (reset-meta! #'ALTER_ATTRIBUTE (assoc {:const true, :column 1} :name 'ALTER_ATTRIBUTE :ns *ns*))
  (def SYSTEM_TX (datomic.db/BOOT-IDS :db/system-tx))
  (reset-meta! #'SYSTEM_TX (assoc {:const true, :column 1} :name 'SYSTEM_TX :ns *ns*))
  (def ATTR_LANG (datomic.db/BOOT-IDS :db/lang))
  (reset-meta! #'ATTR_LANG (assoc {:const true, :column 1} :name 'ATTR_LANG :ns *ns*))
  (def ATTR_CODE (datomic.db/BOOT-IDS :db/code))
  (reset-meta! #'ATTR_CODE (assoc {:const true, :column 1} :name 'ATTR_CODE :ns *ns*))
  (def DB_FN (datomic.db/BOOT-IDS :db/fn))
  (reset-meta! #'DB_FN (assoc {:const true, :column 1} :name 'DB_FN :ns *ns*))
  (def LANG_CLOJURE (datomic.db/BOOT-IDS :db.lang/clojure))
  (reset-meta! #'LANG_CLOJURE (assoc {:const true, :column 1} :name 'LANG_CLOJURE :ns *ns*))
  (def LANG_JAVA (datomic.db/BOOT-IDS :db.lang/java))
  (reset-meta! #'LANG_JAVA (assoc {:const true, :column 1} :name 'LANG_JAVA :ns *ns*))
  (def TYPE_REF (datomic.db/BOOT-IDS :db.type/ref))
  (reset-meta! #'TYPE_REF (assoc {:const true, :column 1} :name 'TYPE_REF :ns *ns*))
  (def TYPE_KEYWORD (datomic.db/BOOT-IDS :db.type/keyword))
  (reset-meta! #'TYPE_KEYWORD (assoc {:const true, :column 1} :name 'TYPE_KEYWORD :ns *ns*))
  (def TYPE_BYTES (datomic.db/BOOT-IDS :db.type/bytes))
  (reset-meta! #'TYPE_BYTES (assoc {:const true, :column 1} :name 'TYPE_BYTES :ns *ns*))
  (def TYPE_LONG (datomic.db/BOOT-IDS :db.type/long))
  (reset-meta! #'TYPE_LONG (assoc {:const true, :column 1} :name 'TYPE_LONG :ns *ns*))
  (def CARDINALITY_ONE (datomic.db/BOOT-IDS :db.cardinality/one))
  (reset-meta! #'CARDINALITY_ONE (assoc {:const true, :column 1} :name 'CARDINALITY_ONE :ns *ns*))
  (def CARDINALITY_MANY (datomic.db/BOOT-IDS :db.cardinality/many))
  (reset-meta!
    #'CARDINALITY_MANY
    (assoc {:const true, :column 1} :name 'CARDINALITY_MANY :ns *ns*))
  (def ATTR_INSTANT (datomic.db/BOOT-IDS :db/txInstant))
  (reset-meta! #'ATTR_INSTANT (assoc {:const true, :column 1} :name 'ATTR_INSTANT :ns *ns*))
  (def UNIQUE (datomic.db/BOOT-IDS :db/unique))
  (reset-meta! #'UNIQUE (assoc {:const true, :column 1} :name 'UNIQUE :ns *ns*))
  (def CARDINALITY (datomic.db/BOOT-IDS :db/cardinality))
  (reset-meta! #'CARDINALITY (assoc {:const true, :column 1} :name 'CARDINALITY :ns *ns*))
  (def VALUE_TYPE (datomic.db/BOOT-IDS :db/valueType))
  (reset-meta! #'VALUE_TYPE (assoc {:const true, :column 1} :name 'VALUE_TYPE :ns *ns*))
  (def IS_COMPONENT (datomic.db/BOOT-IDS :db/isComponent))
  (reset-meta! #'IS_COMPONENT (assoc {:const true, :column 1} :name 'IS_COMPONENT :ns *ns*))
  (def INDEX (datomic.db/BOOT-IDS :db/index))
  (reset-meta! #'INDEX (assoc {:const true, :column 1} :name 'INDEX :ns *ns*))
  (def NO_HISTORY (datomic.db/BOOT-IDS :db/noHistory))
  (reset-meta! #'NO_HISTORY (assoc {:const true, :column 1} :name 'NO_HISTORY :ns *ns*))
  (def FULLTEXT (datomic.db/BOOT-IDS :db/fulltext))
  (reset-meta! #'FULLTEXT (assoc {:const true, :column 1} :name 'FULLTEXT :ns *ns*))
  (def BASE_TX 1000)
  (reset-meta! #'BASE_TX (assoc {:const true, :column 1} :name 'BASE_TX :ns *ns*))
  (def SCHEMA-LIMIT (long (java.lang.Math/pow (unchecked-double 2) (unchecked-double 20))))
  (reset-meta! #'SCHEMA-LIMIT (assoc {:const true, :column 1} :name 'SCHEMA-LIMIT :ns *ns*))
  (def SYSTEM_ATTRS #{7 50 9 8})
  (def TUPLE_SCHEMA_LEVEL 5)
  (defn supports-tuples? ([db] (<= datomic.db/TUPLE_SCHEMA_LEVEL (:schema-level db))))
  (def MAX_T (long (quot java.lang.Long/MAX_VALUE 4)))
  (reset-meta! #'MAX_T (assoc {:const true, :column 1} :name 'MAX_T :ns *ns*))
  (definterface IDatumImpl (^long eidx []))
  (clojure.core/import 'datomic.db.IDatumImpl)
  (definterface IElementImpl (^java.lang.Object id []) (^java.lang.Object kw []))
  (clojure.core/import 'datomic.db.IElementImpl)
  (definterface
    IDb
    (^java.lang.Object getNextT [])
    (^java.lang.Object keywordOf [^java.lang.Object arg0])
    (^java.lang.Object idOf [^java.lang.Object arg0])
    (^java.lang.Object getAsOfT [])
    (^java.lang.Object getSinceT [])
    (^java.lang.Object getRaw [])
    (^java.lang.Object getFilter [])
    (^datomic.iter.Iter seekEAVT [^datomic.impl.db.IDatum arg0])
    (^datomic.iter.Iter seekAVET [^datomic.impl.db.IDatum arg0])
    (^datomic.iter.Iter seekAEVT [^datomic.impl.db.IDatum arg0])
    (^datomic.iter.Iter seekRAET [^datomic.impl.db.IDatum arg0])
    (^clojure.lang.IFn getFn [^java.lang.Object arg0]))
  (clojure.core/import 'datomic.db.IDb)
  (definterface
    IDbImpl
    (^java.lang.Object eq [^java.lang.Object arg0])
    (^java.lang.Object elementAt [^java.lang.Object arg0])
    (^java.lang.Object addKeyword [^java.lang.Object arg0 ^java.lang.Object arg1])
    (^java.lang.Object addElement [^datomic.db.IElementImpl arg0])
    (^java.lang.Object growElements [^java.lang.Object arg0])
    (^java.lang.Object addData
      [^java.lang.Object arg0 ^java.util.ArrayList arg1 ^java.lang.Object arg2])
    (^java.lang.Object acceptData [^java.lang.Object arg0])
    (^java.lang.Object acceptDataCheck [^java.lang.Object arg0 ^java.lang.Object arg1])
    (^java.lang.Object getRawId []))
  (clojure.core/import 'datomic.db.IDbImpl)
  (defn make-eid
    (^long [^long part ^long id] (bit-or (bit-shift-left part 42) (bit-and id 4398046511103))))
  (defn eid->part (^long [^long eid] (bit-shift-right (bit-and eid 4611686018427387903) 42)))
  (defn eid->eidx
    (^long [^long eid]
      (let [ret (bit-and eid 4398046511103)]
        (if (= (bit-and ret 2199023255552) 0) ret (bit-or ret -4398046511104)))))
  (defn partition-eid
    (^long [^long eid]
      (let [partbits (datomic.db/eid->part eid)]
        (if (< partbits 524288) partbits (datomic.db/make-eid partbits 0)))))
  (defn get-part (^long [datum] (datomic.db/eid->part (.getE ^datomic.impl.db.IDatum datum))))
  (defn get-eidx (^long [datum] (datomic.db/eid->eidx (.getE ^datomic.impl.db.IDatum datum))))
  (defn implicit-part
    (^long [^long id]
      (.longValue
        (if (and (< id 524288) (>= id 0))
          (long (datomic.db/make-eid (bit-or id 524288) 0))
          (error/arg
            :db.error/implicit-part-out-of-range
            (str (long id) " out of implicit part range"))))))
  (defn implicit-part-id
    ([^long part]
      (let [partbits (datomic.db/eid->part part)]
        (when (and (>= partbits 524288) (zero? (datomic.db/eid->eidx part)))
          (long (bit-xor partbits 524288))))))
  (defn make-tempid
    (^long [^long part ^long id] (bit-or -9223372036854775808 (datomic.db/make-eid part id))))
  (reset-meta!
    #'make-tempid
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'part {:tag 'long}) (.withMeta 'id {:tag 'long})] {:tag 'long})),
       :column 1}
      :name
      'make-tempid
      :ns
      *ns*))
  (defn tempid? ([^long eid] (not (zero? (bit-and -4611686018427387904 eid)))))
  (declare datomic.db/->DbId)
  (declare datomic.db/map->DbId)
  (defrecord
    DbId
    [part idx]
    java.lang.Object
    (^java.lang.String toString
      [this]
      (str
        "#db/id["
        (:part this)
        (let [temp__5457__auto__ (:idx this)]
          (when temp__5457__auto__ (let [idx temp__5457__auto__] (str " " idx))))
        "]")))
  (clojure.core/import 'datomic.db.DbId)
  (defn ->DbId ([part idx] (datomic.db.DbId. part idx)))
  (defn map->DbId
    ([m__7585__auto__]
      (DbId/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defmethod
    print-method
    datomic.db.DbId
    fn__12434
    ([dbid w] (.write ^java.io.Writer w (str dbid)) nil))
  (defmethod print-dup datomic.db.DbId fn__12436 ([o w] (print-method o w)))
  (def BASE_TEMPID 1000001)
  (reset-meta! #'BASE_TEMPID (assoc {:const true, :column 1} :name 'BASE_TEMPID :ns *ns*))
  (def BASE_TRANSACTOR_TEMPID 1000000000001)
  (reset-meta!
    #'BASE_TRANSACTOR_TEMPID
    (assoc {:const true, :column 1} :name 'BASE_TRANSACTOR_TEMPID :ns *ns*))
  (def nextid (java.util.concurrent.atomic.AtomicLong. 1000001))
  (reset-meta!
    #'nextid
    (assoc
      {:tag java.util.concurrent.atomic.AtomicLong, :private true, :column 1}
      :name
      'nextid
      :ns
      *ns*))
  (defn use-transactor-tempid-range ([] (.set datomic.db/nextid 1000000000001) nil))
  (defn next-id ([] (long (.getAndIncrement datomic.db/nextid))))
  (reset-meta!
    #'next-id
    (assoc {:private true, :arglists (clojure.core/list []), :column 1} :name 'next-id :ns *ns*))
  (defn id-literal
    ([literal]
      (datomic.db.DbId.
        (nth literal (unchecked-int 0))
        (or (nth literal (unchecked-int 1) nil) (- (datomic.db/next-id))))))
  (declare datomic.db/resolve-id)
  (defn tempid
    ([db part]
      (let [pid (datomic.db/resolve-id db part)]
        (if pid
          (datomic.db/tempid (unchecked-long ^java.lang.Number pid))
          (error/arg :db.error/not-a-partition (str "Can't find partition with id: " part)))))
    ([^long part] (long (datomic.db/make-tempid part (unchecked-long (datomic.db/next-id))))))
  (defonce LocalizeTempid {})
  (defprotocol LocalizeTempid (local-id [_ db procargs local-tempids]))
  (defonce Symbolish {})
  (defprotocol Symbolish (sym-name [s]) (sym-namespace [s]))
  (defn system-eid ([db ident] (get (:system-eids db) ident)))
  (reset-meta!
    #'system-eid
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'ident]), :column 1}
      :name
      'system-eid
      :ns
      *ns*))
  (defn add-system-eids
    ([db]
      (assoc
        db
        :system-eids
        (into
          {}
          (comp
            (map
              (fn fn__12495
                ([ident]
                  (let [temp__5457__auto__ (.entid ^datomic.Database db ident)]
                    (when temp__5457__auto__ (let [eid temp__5457__auto__] [ident eid]))))))
            (remove nil?))
          [:db.type/tuple :db.attr/preds :db/ensure]))))
  (reset-meta!
    #'add-system-eids
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]), :column 1}
      :name
      'add-system-eids
      :ns
      *ns*))
  (extend nil datomic.db/Symbolish {:sym-name (fn fn__12499 ([_] nil))})
  (extend java.lang.Object datomic.db/Symbolish {:sym-name (fn fn__12501 ([_] nil))})
  (extend
    clojure.lang.Named
    datomic.db/Symbolish
    {:sym-name (fn fn__12503 ([k] (name k))), :sym-namespace (fn fn__12505 ([k] (namespace k)))})
  (def caching-normalize
   (memoize
     (fn fn__12507
       ([x]
         (if (string? x)
           (if (and
                 (seq x)
                 (= (char (.charAt ^java.lang.String x (unchecked-int 0))) (char (.charValue \:))))
             (keyword (subs x 1))
             (error/arg
               :db.error/not-a-keyword
               (str "Cannot interpret as a keyword: " x ", no leading :")))
           (let [temp__5455__auto__ (datomic.db/sym-name x)]
             (if temp__5455__auto__
               (let [name temp__5455__auto__] (keyword (datomic.db/sym-namespace x) name))
               x)))))))
  (defn to-kw ([kw] (when kw (if (keyword? kw) kw (datomic.db/caching-normalize kw)))))
  (defn normalize-kw
    ([kw]
      (let [temp__5457__auto__ (datomic.db/to-kw kw)]
        (when temp__5457__auto__
          (let [result temp__5457__auto__]
            (when-not (keyword? result)
              (error/arg
                :db.error/not-a-keyword
                (str "Cannot interpret as a keyword: " kw " of class: " (class kw))))
            result)))))
  (defn require-kw
    ([kw]
      (or
        (datomic.db/normalize-kw kw)
        (error/arg :db.error/nil-keyword "Nil where a keyword was expected"))))
  (deftype
    Datum
    [^long e ^int a v ^long tOp]
    java.lang.Object
    datomic.db.IDatumImpl
    datomic.Datom
    clojure.lang.ILookup
    datomic.impl.db.IDatum
    clojure.lang.Counted
    clojure.lang.Indexed
    (valAt
      [this k not_found]
      (let [G__12520 k]
        (case
          G__12520
          :a
          (java.lang.Integer/valueOf (int a))
          :e
          (long e)
          :added
          (.added this)
          :tx
          (long (.getTx this))
          :v
          v
          not_found)))
    (valAt
      [this k]
      (let [G__12519 k]
        (case
          G__12519
          :a
          (java.lang.Integer/valueOf (int a))
          :e
          (long e)
          :added
          (.added this)
          :tx
          (long (.getTx this))
          :v
          v)))
    (get [this ^int i] (.nth this (int i)))
    (^boolean added [this] (boolean (.isAssertion this)))
    (tx [this] (long (.getTx this)))
    (v [this] v)
    (a [this] (java.lang.Integer/valueOf (int a)))
    (e [this] (long e))
    (nth
      [this ^int i _]
      (let [G__12518 i]
        (case
          G__12518
          0
          (long (.getE this))
          1
          (java.lang.Integer/valueOf (int (.getA this)))
          2
          (.getV this)
          3
          (long (.getTx this))
          4
          (.added this))))
    (nth
      [this ^int i]
      (let [G__12517 i]
        (case
          G__12517
          0
          (long (.getE this))
          1
          (java.lang.Integer/valueOf (int (.getA this)))
          2
          (.getV this)
          3
          (long (.getTx this))
          4
          (.added this))))
    (^int count [this] (int 5))
    (^long eidx [this] (datomic.db/eid->eidx e))
    (^boolean getBooleanV [this] (.booleanValue ^java.lang.Boolean v))
    (^float getFloatV [this] (.floatValue ^java.lang.Number v))
    (^int getIntV [this] (.intValue ^java.lang.Number v))
    (^double getDoubleV [this] (.doubleValue ^java.lang.Number v))
    (^long getLongV [this] (.longValue ^java.lang.Number v))
    (^long getTx [this] (datomic.db/make-eid 3 (bit-shift-right tOp 1)))
    (^long getT [this] (bit-shift-right tOp 1))
    (getV [this] v)
    (^int getA [this] a)
    (^long getE [this] e)
    (^int getP [this] (datomic.db/eid->part e))
    (^boolean isAssertion [this] (clojure.lang.Numbers/isPos (long (bit-and tOp 1))))
    (^int hashCode
      [this]
      (bit-xor
        (bit-xor (bit-xor (hash (long e)) (hash (java.lang.Integer/valueOf (int a)))) (hash v))
        (hash (long tOp))))
    (^boolean equals
      [this o]
      (or
        (identical? this o)
        (let [o o]
          (and
            (instance? datomic.impl.db.IDatum o)
            (= (long (.getT this)) (long (.getT ^datomic.impl.db.IDatum o)))
            (= (long e) (long (.getE ^datomic.impl.db.IDatum o)))
            (= (long a) (long (.getA ^datomic.impl.db.IDatum o)))
            (zero? (common/compare v (.getV ^datomic.impl.db.IDatum o)))
            (=
              (boolean (.isAssertion this))
              (boolean (.isAssertion ^datomic.impl.db.IDatum o))))))))
  (clojure.core/import 'datomic.db.Datum)
  (defn ->Datum
    ([e a v tOp]
      (datomic.db.Datum.
        (unchecked-long ^java.lang.Number e)
        (unchecked-int ^java.lang.Number a)
        v
        (unchecked-long ^java.lang.Number tOp))))
  (defmethod
    print-method
    datomic.db.Datum
    fn__12531
    ([d w]
      (.write ^java.io.Writer w "#datom[")
      (print-method (long (.getE ^datomic.impl.db.IDatum d)) w)
      (.write ^java.io.Writer w " ")
      (print-method (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))) w)
      (.write ^java.io.Writer w " ")
      (print-method (.getV ^datomic.impl.db.IDatum d) w)
      (.write ^java.io.Writer w " ")
      (print-method (long (.getTx ^datomic.impl.db.IDatum d)) w)
      (.write ^java.io.Writer w " ")
      (print-method (.isAssertion ^datomic.impl.db.IDatum d) w)
      (.write ^java.io.Writer w "]")
      nil))
  (defn dget ([iter] (iter/iget iter)))
  (reset-meta!
    #'dget
    (assoc
      {:tag datomic.impl.db.IDatum,
       :arglists (clojure.core/list [(.withMeta 'iter {:tag 'Iter})]),
       :column 1}
      :name
      'dget
      :ns
      *ns*))
  (defn assertion-policy-compare
    ([a b]
      (cond
        (= (.isAssertion ^datomic.impl.db.IDatum a) (.isAssertion ^datomic.impl.db.IDatum b)) 0
        (.isAssertion ^datomic.impl.db.IDatum a) -1
        :else (do 1))))
  (reset-meta!
    #'assertion-policy-compare
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'a {:tag 'IDatum}) (.withMeta 'b {:tag 'IDatum})]),
       :column 1}
      :name
      'assertion-policy-compare
      :ns
      *ns*))
  (def eavt-cmp
   (reify
     java.util.Comparator
     (^int compare
       [this x y]
       (.intValue
         (let [x x y y]
           (cond
             (< (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) -1
             (> (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) 1
             (< (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) -1
             (> (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) 1
             :else (do
                     (let [c (common/compare
                               (.getV ^datomic.impl.db.IDatum x)
                               (.getV ^datomic.impl.db.IDatum y))]
                       (cond
                         (not (zero? c)) (long c)
                         (> (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) -1
                         (< (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) 1
                         :else (do (datomic.db/assertion-policy-compare x y)))))))))))
  (def avet-cmp
   (reify
     java.util.Comparator
     (^int compare
       [this x y]
       (.intValue
         (let [x x y y]
           (cond
             (< (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) -1
             (> (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) 1
             :else (do
                     (let [c (common/compare
                               (.getV ^datomic.impl.db.IDatum x)
                               (.getV ^datomic.impl.db.IDatum y))]
                       (cond
                         (not (zero? c)) (long c)
                         (< (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) -1
                         (> (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) 1
                         (> (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) -1
                         (< (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) 1
                         :else (do (datomic.db/assertion-policy-compare x y)))))))))))
  (def aevt-cmp
   (reify
     java.util.Comparator
     (^int compare
       [this x y]
       (.intValue
         (let [x x y y]
           (cond
             (< (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) -1
             (> (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) 1
             (< (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) -1
             (> (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) 1
             :else (do
                     (let [c (common/compare
                               (.getV ^datomic.impl.db.IDatum x)
                               (.getV ^datomic.impl.db.IDatum y))]
                       (cond
                         (not (zero? c)) (long c)
                         (> (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) -1
                         (< (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) 1
                         :else (do (datomic.db/assertion-policy-compare x y)))))))))))
  (def raet-cmp
   (reify
     java.util.Comparator
     (^int compare
       [this x y]
       (.intValue
         (let [x x
               y y
               c (common/compare
                   (.getV ^datomic.impl.db.IDatum x)
                   (.getV ^datomic.impl.db.IDatum y))]
           (cond
             (not (zero? c)) (long c)
             (< (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) -1
             (> (.getA ^datomic.impl.db.IDatum x) (.getA ^datomic.impl.db.IDatum y)) 1
             (< (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) -1
             (> (.getE ^datomic.impl.db.IDatum x) (.getE ^datomic.impl.db.IDatum y)) 1
             (> (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) -1
             (< (.getT ^datomic.impl.db.IDatum x) (.getT ^datomic.impl.db.IDatum y)) 1
             :else (do (datomic.db/assertion-policy-compare x y))))))))
  (defn asserting-datum
    ([^long e ^long a v ^long t]
      (datomic.db.Datum. (long e) (unchecked-int a) v (long (bit-or (bit-shift-left t 1) 1)))))
  (defn retracting-datum
    ([^long e ^long a v ^long t]
      (datomic.db.Datum. (long e) (unchecked-int a) v (long (bit-shift-left t 1)))))
  (declare datomic.db/datum)
  (declare datomic.db/attr-index-range)
  (declare datomic.db/require-attrid)
  (declare datomic.db/require-attr)
  (declare datomic.db/datom-error-desc)
  (declare datomic.db/entity-error-desc)
  (declare datomic.db/update-schema-level)
  (definterface IAttributeImpl (^java.lang.Object hasAVET []))
  (clojure.core/import 'datomic.db.IAttributeImpl)
  (declare datomic.db/->Attribute)
  (declare datomic.db/map->Attribute)
  (defrecord
    Attribute
    [id
     kw
     vtypeid
     cardinality
     isComponent
     unique
     index
     storageHasAVET
     needsAVET
     noHistory
     fulltext]
    datomic.db.IAttributeImpl
    datomic.db.IElementImpl
    (hasAVET [this] (and storageHasAVET needsAVET))
    (id [this] id)
    (kw [this] kw))
  (clojure.core/import 'datomic.db.Attribute)
  (defn ->Attribute
    ([id
      kw
      vtypeid
      cardinality
      isComponent
      unique
      index
      storageHasAVET
      needsAVET
      noHistory
      fulltext]
      (datomic.db.Attribute.
        id
        kw
        vtypeid
        cardinality
        isComponent
        unique
        index
        storageHasAVET
        needsAVET
        noHistory
        fulltext)))
  (defn map->Attribute
    ([m__7585__auto__]
      (Attribute/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn attribute
    ([db attrid]
      (let [temp__5457__auto__ (.elementAt ^datomic.db.IDbImpl db attrid)]
        (when temp__5457__auto__
          (let [attr temp__5457__auto__] (when (instance? datomic.db.Attribute attr) attr))))))
  (defn resolve-lookup-ref
    ([db x]
      (when-not (= 2 (count x))
        (error/arg :db.error/invalid-lookup-ref (str "Invalid list form: " x)))
      (let [a (.get ^java.util.List x (unchecked-int 0))
            v (.get ^java.util.List x (unchecked-int 1))
            aid (datomic.db/require-attrid db a)
            attr (datomic.db/attribute db aid)]
        (when (= 27 (.-vtypeid ^datomic.db.Attribute attr))
          (error/arg
            :db.error/lookup-ref-not-supported
            (str "Lookup ref not supported for " (.kw ^datomic.db.Attribute attr))))
        (if (.-unique ^datomic.db.Attribute attr)
          (if (= aid 10)
            (.idOf ^datomic.db.IDb db v)
            (let [temp__5457__auto__ (iter/iget (datomic.db/attr-index-range db a v nil))]
              (when temp__5457__auto__
                (let [datum temp__5457__auto__]
                  (when (= v (.v ^datomic.Datom datum)) (.e ^datomic.Datom datum))))))
          (error/arg
            :db.error/lookup-ref-attr-not-unique
            (str "Attribute values not unique: " (.kw ^datomic.db.Attribute attr)))))))
  (defn partbits
    ([db kw_or_parteid]
      (let [temp__5455__auto__ (datomic.db/resolve-id db kw_or_parteid)]
        (if temp__5455__auto__
          (let [id temp__5455__auto__
                part (datomic.db/eid->part (unchecked-long ^java.lang.Number id))
                eidx (datomic.db/eid->eidx (unchecked-long ^java.lang.Number id))]
            (cond (= part 0) (long eidx) (= eidx 0) (do (long part))))
          (error/arg :db.error/not-a-db-id (str "Invalid db/id: " kw_or_parteid))))))
  (defn resolve-dbid
    ([db x]
      (let [map__12605 x
            map__12605 (if (seq? map__12605)
                         (clojure.lang.PersistentHashMap/create (seq map__12605))
                         map__12605)
            part (get map__12605 :part)
            idx (get map__12605 :idx)
            part (datomic.db/partbits db part)]
        (if (neg? idx)
          (long
            (datomic.db/make-tempid
              (unchecked-long ^java.lang.Number part)
              (unchecked-long ^java.lang.Number idx)))
          (long
            (datomic.db/make-eid
              (unchecked-long ^java.lang.Number part)
              (unchecked-long ^java.lang.Number idx)))))))
  (defn extended-resolve-id
    ([db x]
      (cond
        (instance? datomic.db.DbId x) (datomic.db/resolve-dbid db x)
        (instance? java.util.List x) (datomic.db/resolve-lookup-ref db x)
        (integer? x) (long x)
        :else (do (.idOf ^datomic.db.IDb db x)))))
  (defn resolve-id
    ([db x]
      (cond
        (instance? java.lang.Long x) x
        (keyword? x) (.idOf ^datomic.db.IDb db x)
        :else (do (datomic.db/extended-resolve-id db x)))))
  (defn string-tempid? ([s] (and (string? s) (not (.startsWith ^java.lang.String s ":")))))
  (defn require-id
    ([db x procargs]
      (or
        (when (datomic.db/string-tempid? x) x)
        (datomic.db/resolve-id db x)
        (error/arg
          :db.error/not-an-entity
          (str
            "Unable to resolve entity: "
            x
            " in datom "
            (datomic.db/datom-error-desc db (drop 1 procargs)))
          {:entity x, :datom (datomic.db/datom-error-desc db (drop 1 procargs))})))
    (^long [db x]
      (.longValue
        (or
          (datomic.db/resolve-id db x)
          (error/arg :db.error/not-an-entity (str "Unable to resolve entity: " x) {:entity x})))))
  (defn require-tuple-ids
    ([db attr tup]
      (if (or (nil? attr) (nil? tup))
        tup
        (let [resolve (fn resolve
                        ([id]
                          (if (nil? id)
                            id
                            (or
                              (if (datomic.db/string-tempid? id) id (datomic.db/resolve-id db id))
                              (error/arg
                                :db.error/not-an-entity
                                (str "Unable to resolve entity: " id " in tuple")
                                {:tuple tup})))))]
          (cond
            (= :db.type/ref (:tupleType attr)) (mapv resolve tup)
            (and
              (some (fn fn__12619 ([p1__12615#] (= :db.type/ref p1__12615#))) (:tupleTypes attr))
              (= (long (count (:tupleTypes attr))) (long (count tup)))) (mapv
                                                                          (fn 
                                                                            fn__12621
                                                                            ([type v]
                                                                              (if
                                                                                (=
                                                                                  type
                                                                                  :db.type/ref)
                                                                                (^clojure.lang.IFn resolve
                                                                                  v)
                                                                                v)))
                                                                          (:tupleTypes attr)
                                                                          tup)
            :default (do tup))))))
  (reset-meta!
    #'require-tuple-ids
    (assoc
      {:private true,
       :arglists (clojure.core/list ['db (.withMeta 'attr {:tag 'Attribute}) 'tup]),
       :column 1}
      :name
      'require-tuple-ids
      :ns
      *ns*))
  (defn maybe-require-ids
    ([db a v reverse?]
      (cond
        reverse? (and v (long (datomic.db/require-id db v)))
        (or (nil? a) (nil? v)) v
        :else (do
                (let [aid (datomic.db/require-attrid db a)
                      attr (datomic.db/attribute db aid)
                      vtypeid (.-vtypeid ^datomic.db.Attribute attr)]
                  (cond
                    (= 20 vtypeid) (long (datomic.db/require-id db v))
                    (= (datomic.db/system-eid db :db.type/tuple) vtypeid) (datomic.db/require-tuple-ids
                                                                            db
                                                                            attr
                                                                            v)
                    :default (do v)))))))
  (extend
    java.lang.String
    datomic.db/LocalizeTempid
    {:local-id
     (fn fn__12629
       ([this db procargs local_tempids]
         (if (.startsWith this ":")
           (datomic.db/local-id (datomic.db/to-kw this) db procargs local_tempids)
           (let [temp__5455__auto__ (get local_tempids this)]
             (if temp__5455__auto__
               (let [id temp__5455__auto__] id)
               (let [local_id (datomic.db/tempid (if (= this "datomic.tx") 3 16))]
                 (.put ^java.util.Map local_tempids this local_id)
                 local_id))))))})
  (extend
    java.lang.Object
    datomic.db/LocalizeTempid
    {:local-id
     (fn fn__12632
       ([this db procargs _]
         (if procargs
           (long (datomic.db/require-id db this))
           (datomic.db/require-id db this procargs))))})
  (defn resolve-kw
    ([db x] (if (instance? java.lang.Number x) (.keywordOf ^datomic.db.IDb db x) x)))
  (defn datum
    ([db & p__12635]
      (let [map__12636 p__12635
            map__12636 (if (seq? map__12636)
                         (clojure.lang.PersistentHashMap/create (seq map__12636))
                         map__12636)
            asserting (get map__12636 :asserting true)
            e (get map__12636 :e)
            a (get map__12636 :a)
            v (get map__12636 :v)
            t (get map__12636 :t)
            e (datomic.db/resolve-id db (or e (long java.lang.Long/MIN_VALUE)))
            a (or (datomic.db/resolve-id db a) -1)]
        (if t
          (if asserting
            (datomic.db/asserting-datum
              (unchecked-long ^java.lang.Number e)
              (unchecked-long ^java.lang.Number a)
              v
              (unchecked-long ^java.lang.Number t))
            (datomic.db/retracting-datum
              (unchecked-long ^java.lang.Number e)
              (unchecked-long ^java.lang.Number a)
              v
              (unchecked-long ^java.lang.Number t)))
          (datomic.db.Datum.
            (unchecked-long ^java.lang.Number e)
            (unchecked-int ^java.lang.Number a)
            v
            (long (- (quot java.lang.Long/MAX_VALUE 4) (if asserting 0 1))))))))
  (defn filter-retractions
    ([iter]
      (let [eat_past (fn eat_past
                       ([d i]
                         (when i
                           (let [j (iter/inext i) n (datomic.db/dget j)]
                             (if (and
                                   n
                                   (=
                                     (long (.getE ^datomic.impl.db.IDatum d))
                                     (long (.getE ^datomic.impl.db.IDatum n)))
                                   (=
                                     (long (.getA ^datomic.impl.db.IDatum d))
                                     (long (.getA ^datomic.impl.db.IDatum n)))
                                   (zero?
                                     (common/compare
                                       (.getV ^datomic.impl.db.IDatum d)
                                       (.getV ^datomic.impl.db.IDatum n)))
                                   (<=
                                     (.getT ^datomic.impl.db.IDatum n)
                                     (.getT ^datomic.impl.db.IDatum d)))
                               (recur d j)
                               j)))))
            skip (fn skip
                   ([i]
                     (let [temp__5457__auto__ (datomic.db/dget i)]
                       (when temp__5457__auto__
                         (let [d temp__5457__auto__]
                           (if (.isAssertion ^datomic.impl.db.IDatum d)
                             i
                             (recur (^clojure.lang.IFn eat_past d i))))))))
            iter (^clojure.lang.IFn skip iter)
            iter_atom (atom iter)
            next_skip (fn next_skip
                        ([p1__12640#] (^clojure.lang.IFn skip (iter/inext p1__12640#))))]
        (when iter
          (reify
            datomic.iter.Iter
            (next [this] (when (swap! iter_atom next_skip) this))
            (get [this] (iter/iget (deref iter_atom))))))))
  (declare datomic.db/->Partition)
  (declare datomic.db/map->Partition)
  (defrecord Partition [id kw] datomic.db.IElementImpl (id [this] id) (kw [this] kw))
  (clojure.core/import 'datomic.db.Partition)
  (defn ->Partition ([id kw] (datomic.db.Partition. id kw)))
  (defn map->Partition
    ([m__7585__auto__]
      (Partition/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn explicit-partition
    ([db eid]
      (let [temp__5457__auto__ (.elementAt ^datomic.db.IDbImpl db eid)]
        (when temp__5457__auto__
          (let [part temp__5457__auto__] (when (instance? datomic.db.Partition part) part))))))
  (defn valid-partbits?
    ([db ^long partbits]
      (or (>= partbits 524288) (datomic.db/explicit-partition db (long partbits)))))
  (declare datomic.db/->ValueType)
  (declare datomic.db/map->ValueType)
  (defrecord ValueType [id kw fressian-tag] datomic.db.IElementImpl (id [this] id) (kw [this] kw))
  (clojure.core/import 'datomic.db.ValueType)
  (defn ->ValueType ([id kw fressian_tag] (datomic.db.ValueType. id kw fressian_tag)))
  (defn map->ValueType
    ([m__7585__auto__]
      (ValueType/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn value-type
    ([db attrid]
      (let [temp__5457__auto__ (.elementAt ^datomic.db.IDbImpl db attrid)]
        (when temp__5457__auto__
          (let [vt temp__5457__auto__] (when (instance? datomic.db.ValueType vt) vt))))))
  (defn require-attrid
    ([db x]
      (let [id (datomic.db/require-id db x)]
        (if (datomic.db/attribute db (long id))
          (long id)
          (error/arg :db.error/not-an-attribute (str x " is not an attribute."))))))
  (defn require-attr ([db x] (.elementAt ^datomic.db.IDbImpl db (datomic.db/require-attrid db x))))
  (deftype
    AttrInfo
    [attr vtype_kw]
    clojure.lang.ILookup
    datomic.Attribute
    (^boolean hasFulltext [this] (.booleanValue (:fulltext this)))
    (^boolean hasNoHistory [this] (.booleanValue (:no-history this)))
    (^boolean hasAVET [this] (.booleanValue (:has-avet this)))
    (^boolean isIndexed [this] (.booleanValue (:indexed this)))
    (unique [this] (:unique this))
    (^boolean isComponent [this] (.booleanValue (:is-component this)))
    (cardinality [this] (:cardinality this))
    (valueType [this] (:value-type this))
    (ident [this] (:ident this))
    (id [this] (:id this))
    (valAt
      [this k not_found]
      (let [G__12720 k]
        (case
          G__12720
          :is-component
          (get attr :isComponent false)
          :indexed
          (get attr :index false)
          :has-avet
          (.hasAVET ^datomic.db.Attribute attr)
          :unique
          (let [pred__12721 = expr__12722 (:unique attr)]
            (if (^clojure.lang.IFn pred__12721 38 expr__12722)
              :db.unique/identity
              (if (^clojure.lang.IFn pred__12721 37 expr__12722)
                :db.unique/value
                (when-not (^clojure.lang.IFn pred__12721 nil expr__12722)
                  (throw
                    (java.lang.IllegalArgumentException. (str "No matching clause: " expr__12722)))
                  (bit-and (bit-shift-right (clojure.lang.Util/hash G__12720) 8) 15)))))
          :fulltext
          (get attr :fulltext false)
          :valueType
          vtype_kw
          :no-history
          (get attr :noHistory false)
          :id
          (:id attr)
          :cardinality
          (let [pred__12723 = expr__12724 (:cardinality attr)]
            (if (^clojure.lang.IFn pred__12723 35 expr__12724)
              :db.cardinality/one
              (if (^clojure.lang.IFn pred__12723 36 expr__12724)
                :db.cardinality/many
                (do
                  (throw
                    (java.lang.IllegalArgumentException. (str "No matching clause: " expr__12724)))
                  (bit-and (bit-shift-right (clojure.lang.Util/hash G__12720) 8) 15)))))
          :ident
          (:kw attr)
          not_found)))
    (valAt
      [this k]
      (let [G__12715 k]
        (case
          G__12715
          :is-component
          (get attr :isComponent false)
          :indexed
          (get attr :index false)
          :has-avet
          (.hasAVET ^datomic.db.Attribute attr)
          :fulltext
          (get attr :fulltext false)
          :value-type
          vtype_kw
          :cardinality
          (let [pred__12716 = expr__12717 (:cardinality attr)]
            (if (^clojure.lang.IFn pred__12716 35 expr__12717)
              :db.cardinality/one
              (if (^clojure.lang.IFn pred__12716 36 expr__12717)
                :db.cardinality/many
                (do
                  (throw
                    (java.lang.IllegalArgumentException. (str "No matching clause: " expr__12717)))
                  (bit-and (bit-shift-right (clojure.lang.Util/hash G__12715) 1) 31)))))
          :no-history
          (get attr :noHistory false)
          :id
          (:id attr)
          :ident
          (:kw attr)
          :unique
          (let [pred__12718 = expr__12719 (:unique attr)]
            (if (^clojure.lang.IFn pred__12718 38 expr__12719)
              :db.unique/identity
              (if (^clojure.lang.IFn pred__12718 37 expr__12719)
                :db.unique/value
                (when-not (^clojure.lang.IFn pred__12718 nil expr__12719)
                  (throw
                    (java.lang.IllegalArgumentException. (str "No matching clause: " expr__12719)))
                  (bit-and (bit-shift-right (clojure.lang.Util/hash G__12715) 1) 31)))))))))
  (clojure.core/import 'datomic.db.AttrInfo)
  (defn ->AttrInfo ([attr vtype_kw] (datomic.db.AttrInfo. attr vtype_kw)))
  (defmethod
    print-method
    datomic.db.AttrInfo
    fn__12729
    ([ai w]
      (.write ^java.io.Writer w "#AttrInfo{")
      (.write ^java.io.Writer w ":id ")
      (print-method (:id ai) w)
      (.write ^java.io.Writer w " :ident ")
      (print-method (:ident ai) w)
      (.write ^java.io.Writer w " :value-type ")
      (print-method (:value-type ai) w)
      (.write ^java.io.Writer w " :cardinality ")
      (print-method (:cardinality ai) w)
      (.write ^java.io.Writer w " :indexed ")
      (print-method (:indexed ai) w)
      (.write ^java.io.Writer w " :has-avet ")
      (print-method (:has-avet ai) w)
      (.write ^java.io.Writer w " :unique ")
      (print-method (:unique ai) w)
      (.write ^java.io.Writer w " :is-component ")
      (print-method (:is-component ai) w)
      (.write ^java.io.Writer w " :no-history ")
      (print-method (:no-history ai) w)
      (.write ^java.io.Writer w " :fulltext ")
      (print-method (:fulltext ai) w)
      (.write ^java.io.Writer w "}")
      nil))
  (defn attr-info
    ([db attrid]
      (let [temp__5457__auto__ (let [G__12731 (datomic.db/resolve-id db attrid)]
                                 (when-not (nil? G__12731) (datomic.db/attribute db G__12731)))]
        (when temp__5457__auto__
          (let [attr temp__5457__auto__]
            (datomic.db.AttrInfo.
              attr
              (:kw (.elementAt ^datomic.db.IDbImpl db (:vtypeid attr)))))))))
  (declare datomic.db/->Function)
  (declare datomic.db/map->Function)
  (defrecord Function [id kw lang code f] datomic.db.IElementImpl (id [this] id) (kw [this] kw))
  (clojure.core/import 'datomic.db.Function)
  (defn ->Function ([id kw lang code f] (datomic.db.Function. id kw lang code f)))
  (defn map->Function
    ([m__7585__auto__]
      (Function/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (declare datomic.db/->IndexSet)
  (declare datomic.db/map->IndexSet)
  (defrecord IndexSet [^IDataSet eavt ^IDataSet avet ^IDataSet aevt ^IDataSet raet fulltext])
  (clojure.core/import 'datomic.db.IndexSet)
  (defn ->IndexSet
    ([eavt avet aevt raet fulltext] (datomic.db.IndexSet. eavt avet aevt raet fulltext)))
  (defn map->IndexSet
    ([m__7585__auto__]
      (IndexSet/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (def mem-index-set
   (datomic.db.IndexSet.
     (datomic.btset/btset datomic.db/eavt-cmp)
     (datomic.btset/btset datomic.db/avet-cmp)
     (datomic.btset/btset datomic.db/aevt-cmp)
     (datomic.btset/btset datomic.db/raet-cmp)
     nil))
  (defn windowed
    ([db whilep iter]
      ((if (.getRaw ^datomic.db.IDb db) identity datomic.db/filter-retractions)
        (let [asof (.getAsOfT ^datomic.db.IDb db)
              since (.getSinceT ^datomic.db.IDb db)
              iter (if whilep (iter/take-while whilep iter) iter)
              iter (if (or asof since)
                     (iter/filter
                       (fn fn__12800
                         ([d]
                           (and
                             (or (nil? asof) (<= (.getT ^datomic.impl.db.IDatum d) asof))
                             (or (nil? since) (> (.getT ^datomic.impl.db.IDatum d) since)))))
                       iter)
                     iter)
              iter (if (.getFilter ^datomic.db.IDb db)
                     (iter/filter
                       (partial (.getFilter ^datomic.db.IDb db) (assoc db :filt nil))
                       iter)
                     iter)]
          iter))))
  (defn seek-datoms
    ([db index components]
      (let [vec__12815 (let [G__12818 index]
                         (case
                           G__12818
                           :aevt
                           (let [vec__12819 components
                                 a (nth vec__12819 (unchecked-int 0) nil)
                                 e (nth vec__12819 (unchecked-int 1) nil)
                                 v (nth vec__12819 (unchecked-int 2) nil)
                                 t (nth vec__12819 (unchecked-int 3) nil)]
                             [e a v t])
                           :avet
                           (let [vec__12822 components
                                 a (nth vec__12822 (unchecked-int 0) nil)
                                 v (nth vec__12822 (unchecked-int 1) nil)
                                 e (nth vec__12822 (unchecked-int 2) nil)
                                 t (nth vec__12822 (unchecked-int 3) nil)]
                             [e a v t])
                           :eavt
                           components
                           :vaet
                           (let [vec__12825 components
                                 v (nth vec__12825 (unchecked-int 0) nil)
                                 a (nth vec__12825 (unchecked-int 1) nil)
                                 e (nth vec__12825 (unchecked-int 2) nil)
                                 t (nth vec__12825 (unchecked-int 3) nil)]
                             [e a v t])))
            e (nth vec__12815 (unchecked-int 0) nil)
            a (nth vec__12815 (unchecked-int 1) nil)
            v (nth vec__12815 (unchecked-int 2) nil)
            t (nth vec__12815 (unchecked-int 3) nil)
            v (datomic.db/maybe-require-ids db a v (= index :vaet))
            t (when t (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t))))
            op (let [G__12828 index]
                 (case
                   G__12828
                   :aevt
                   (fn fn__12829
                     ([p1__12809# p2__12810#]
                       (.seekAEVT ^datomic.db.IDb p1__12809# ^datomic.impl.db.IDatum p2__12810#)))
                   :avet
                   (fn fn__12831
                     ([p1__12811# p2__12812#]
                       (.seekAVET ^datomic.db.IDb p1__12811# ^datomic.impl.db.IDatum p2__12812#)))
                   :eavt
                   (fn fn__12833
                     ([p1__12807# p2__12808#]
                       (.seekEAVT ^datomic.db.IDb p1__12807# ^datomic.impl.db.IDatum p2__12808#)))
                   :vaet
                   (fn fn__12835
                     ([p1__12813# p2__12814#]
                       (.seekRAET
                         ^datomic.db.IDb p1__12813#
                         ^datomic.impl.db.IDatum p2__12814#)))))
            eid (if e (datomic.db/resolve-id db e) 0)
            attrid (if a (datomic.db/resolve-id db a) 0)]
        (when (and (= :avet index) a attrid)
          (let [temp__5457__auto__ (datomic.db/attribute db attrid)]
            (when temp__5457__auto__
              (let [attr temp__5457__auto__]
                (when-not (.hasAVET ^datomic.db.Attribute attr)
                  (error/arg
                    :db.error/attribute-not-indexed
                    (str "attribute: " a " is not indexed")))))))
        (iter/iterable
          (fn fn__12837
            ([]
              (datomic.db/windowed
                db
                nil
                (^clojure.lang.IFn op db (datomic.db/datum db :e eid :a attrid :v v :t t)))))))))
  (defn index-sort->cmp
    ([index_sort]
      (let [G__12843 index_sort]
        (case
          G__12843
          :aevt
          datomic.db/aevt-cmp
          :avet
          datomic.db/avet-cmp
          :eavt
          datomic.db/eavt-cmp
          :vaet
          datomic.db/raet-cmp))))
  (defn reverse-comparator
    ([cmp]
      (reify
        java.util.Comparator
        (^int compare [this x y] (.compare ^java.util.Comparator cmp y x)))))
  (reset-meta!
    #'reverse-comparator
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'cmp {:tag 'Comparator})] {:tag 'java.util.Comparator})),
       :column 1}
      :name
      'reverse-comparator
      :ns
      *ns*))
  (defn rseek-index
    ([p__12849 index_sort d]
      (let [map__12850 p__12849
            map__12850 (if (seq? map__12850)
                         (clojure.lang.PersistentHashMap/create (seq map__12850))
                         map__12850)
            raw (get map__12850 :raw)
            asOfT (get map__12850 :asOfT)
            indexBasisT (get map__12850 :indexBasisT)
            memidx (get map__12850 :memidx)
            indexing (get map__12850 :indexing)
            mid_index (get map__12850 :mid-index)
            index (get map__12850 :index)
            history (get map__12850 :history)
            cmp (datomic.db/index-sort->cmp index_sort)
            rseek (fn rseek
                    ([idx d]
                      (iter/reversed-iter
                        (or (datomic.btset/seek idx d) (datomic.btset/seek-last idx)))))
            idx_key (if (= index_sort :vaet) :raet index_sort)]
        (iter/drop-while
          (fn fn__12854 ([p1__12848#] (neg? (.compare ^java.util.Comparator cmp d p1__12848#))))
          (iter/merge-iters
            (datomic.db/reverse-comparator (datomic.db/index-sort->cmp index_sort))
            (^clojure.lang.IFn rseek (get memidx idx_key) d)
            (^clojure.lang.IFn rseek (and indexing (get indexing idx_key)) d)
            (^clojure.lang.IFn rseek (and mid_index (get mid_index idx_key)) d)
            (^clojure.lang.IFn rseek (and index (get index idx_key)) d)
            (when (or raw (and asOfT (< asOfT indexBasisT)))
              (^clojure.lang.IFn rseek (and history (get history idx_key)) d)))))))
  (defn distinct-last-by
    ([f coll]
      (letfn
        [(step
           [f prior coll]
           (let [temp__5455__auto__ (seq coll)]
             (if temp__5455__auto__
               (let [vec__12864 temp__5455__auto__
                     seq__12865 (seq vec__12864)
                     first__12866 (first seq__12865)
                     seq__12865 (next seq__12865)
                     cur first__12866
                     more seq__12865]
                 (if (= (^clojure.lang.IFn f prior) (^clojure.lang.IFn f cur))
                   (recur f cur more)
                   (lazy-seq (cons prior (^clojure.lang.IFn step f cur more)))))
               (cons prior nil))))]
        (let [temp__5457__auto__ (seq coll)]
          (when temp__5457__auto__
            (let [s temp__5457__auto__] (^clojure.lang.IFn step f (first coll) (next coll))))))))
  (reset-meta!
    #'distinct-last-by
    (assoc
      {:private true, :arglists (clojure.core/list ['f 'coll]), :column 1}
      :name
      'distinct-last-by
      :ns
      *ns*))
  (defn reverse-datum-spec
    ([db index & p__12873]
      (let [map__12874 p__12873
            map__12874 (if (seq? map__12874)
                         (clojure.lang.PersistentHashMap/create (seq map__12874))
                         map__12874)
            e (get map__12874 :e)
            a (get map__12874 :a)
            v (get map__12874 :v)
            t (get map__12874 :t)
            asserting (get map__12874 :asserting false)
            e (if (and (= index :aevt) (nil? v) (not (nil? e)))
                (inc (datomic.db/resolve-id db e))
                (or (datomic.db/resolve-id db e) (long java.lang.Long/MAX_VALUE)))
            a (if (and (or (= index :avet) (= index :eavt)) (nil? v) (not (nil? a)))
                (inc (datomic.db/resolve-id db a))
                (or
                  (datomic.db/resolve-id db a)
                  (java.lang.Integer/valueOf (int java.lang.Integer/MAX_VALUE))))
            v (if (and (= index :vaet) (not v)) (long java.lang.Long/MAX_VALUE) v)
            t (or t 0)]
        [:e e :a a :v v :t t :asserting asserting])))
  (defn rseek-datoms
    ([db index components]
      (let [vec__12886 (let [G__12889 index]
                         (case
                           G__12889
                           :aevt
                           (let [vec__12890 components
                                 a (nth vec__12890 (unchecked-int 0) nil)
                                 e (nth vec__12890 (unchecked-int 1) nil)
                                 v (nth vec__12890 (unchecked-int 2) nil)
                                 t (nth vec__12890 (unchecked-int 3) nil)]
                             [e a v t])
                           :avet
                           (let [vec__12893 components
                                 a (nth vec__12893 (unchecked-int 0) nil)
                                 v (nth vec__12893 (unchecked-int 1) nil)
                                 e (nth vec__12893 (unchecked-int 2) nil)
                                 t (nth vec__12893 (unchecked-int 3) nil)]
                             [e a v t])
                           :eavt
                           components
                           :vaet
                           (let [vec__12896 components
                                 v (nth vec__12896 (unchecked-int 0) nil)
                                 a (nth vec__12896 (unchecked-int 1) nil)
                                 e (nth vec__12896 (unchecked-int 2) nil)
                                 t (nth vec__12896 (unchecked-int 3) nil)]
                             [e a v t])))
            e (nth vec__12886 (unchecked-int 0) nil)
            a (nth vec__12886 (unchecked-int 1) nil)
            v (nth vec__12886 (unchecked-int 2) nil)
            t (nth vec__12886 (unchecked-int 3) nil)
            attrid (if a (datomic.db/resolve-id db a) 0)
            _ (when (and (= :avet index) a attrid)
                (let [temp__5457__auto__ (datomic.db/attribute db attrid)]
                  (when temp__5457__auto__
                    (let [attr temp__5457__auto__]
                      (when-not (.hasAVET ^datomic.db.Attribute attr)
                        (error/arg
                          :db.error/attribute-not-indexed
                          (str "attribute: " a " is not indexed")))))))
            v (datomic.db/maybe-require-ids db a v (= index :vaet))
            t (when t (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t))))
            seek_d (apply
                     datomic.db/datum
                     db
                     (datomic.db/reverse-datum-spec db index :e e :a a :v v :t t))
            datoms (datomic.db/windowed
                     (.history ^datomic.Database db)
                     nil
                     (datomic.db/rseek-index db index seek_d))]
        (if (.isHistory ^datomic.Database db)
          (iter/iter-seq datoms)
          (filter
            (fn fn__12899 ([p1__12885#] (.isAssertion ^datomic.impl.db.IDatum p1__12885#)))
            (datomic.db/distinct-last-by
              (fn fn__12901
                ([d]
                  [(long (.getE ^datomic.impl.db.IDatum d))
                   (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))
                   (.getV ^datomic.impl.db.IDatum d)]))
              (iter/iter-seq datoms)))))))
  (defn datoms
    ([db index components]
      (let [vec__12916 (let [G__12919 index]
                         (case
                           G__12919
                           :aevt
                           (let [vec__12920 components
                                 a (nth vec__12920 (unchecked-int 0) nil)
                                 e (nth vec__12920 (unchecked-int 1) nil)
                                 v (nth vec__12920 (unchecked-int 2) nil)
                                 t (nth vec__12920 (unchecked-int 3) nil)]
                             [e a v t])
                           :avet
                           (let [vec__12923 components
                                 a (nth vec__12923 (unchecked-int 0) nil)
                                 v (nth vec__12923 (unchecked-int 1) nil)
                                 e (nth vec__12923 (unchecked-int 2) nil)
                                 t (nth vec__12923 (unchecked-int 3) nil)]
                             [e a v t])
                           :eavt
                           components
                           :vaet
                           (let [vec__12926 components
                                 v (nth vec__12926 (unchecked-int 0) nil)
                                 a (nth vec__12926 (unchecked-int 1) nil)
                                 e (nth vec__12926 (unchecked-int 2) nil)
                                 t (nth vec__12926 (unchecked-int 3) nil)]
                             [e a v t])))
            e (nth vec__12916 (unchecked-int 0) nil)
            a (nth vec__12916 (unchecked-int 1) nil)
            v (nth vec__12916 (unchecked-int 2) nil)
            t (nth vec__12916 (unchecked-int 3) nil)
            v (datomic.db/maybe-require-ids db a v (= index :vaet))
            t (when t (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t))))
            op (let [G__12929 index]
                 (case
                   G__12929
                   :aevt
                   (fn fn__12930
                     ([p1__12909# p2__12910#]
                       (.seekAEVT ^datomic.db.IDb p1__12909# ^datomic.impl.db.IDatum p2__12910#)))
                   :avet
                   (fn fn__12932
                     ([p1__12911# p2__12912#]
                       (.seekAVET ^datomic.db.IDb p1__12911# ^datomic.impl.db.IDatum p2__12912#)))
                   :eavt
                   (fn fn__12934
                     ([p1__12907# p2__12908#]
                       (.seekEAVT ^datomic.db.IDb p1__12907# ^datomic.impl.db.IDatum p2__12908#)))
                   :vaet
                   (fn fn__12936
                     ([p1__12913# p2__12914#]
                       (.seekRAET
                         ^datomic.db.IDb p1__12913#
                         ^datomic.impl.db.IDatum p2__12914#)))))
            eid (if e (datomic.db/resolve-id db e) 0)
            attrid (if a (datomic.db/resolve-id db a) 0)]
        (when (and (= :avet index) a attrid)
          (let [temp__5457__auto__ (datomic.db/attribute db attrid)]
            (when temp__5457__auto__
              (let [attr temp__5457__auto__]
                (when-not (.hasAVET ^datomic.db.Attribute attr)
                  (error/arg
                    :db.error/attribute-not-indexed
                    (str "attribute: " a " is not indexed")))))))
        (iter/iterable
          (fn fn__12938
            ([]
              (datomic.db/windowed
                db
                (fn fn__12939
                  ([p1__12915#]
                    (and
                      (or (nil? e) (= eid (long (.getE ^datomic.impl.db.IDatum p1__12915#))))
                      (or (nil? a) (= attrid (long (.getA ^datomic.impl.db.IDatum p1__12915#))))
                      (or
                        (nil? v)
                        (zero? (common/compare v (.getV ^datomic.impl.db.IDatum p1__12915#))))
                      (or (nil? t) (= t (long (.getT ^datomic.impl.db.IDatum p1__12915#)))))))
                (^clojure.lang.IFn op db (datomic.db/datum db :e eid :a attrid :v v :t t)))))))))
  (defn attr-index-range
    ([db a start end]
      (let [attrid (datomic.db/require-id db a)
            attr (.elementAt ^datomic.db.IDbImpl db (long attrid))
            _ (when-not (and attr (.hasAVET ^datomic.db.Attribute attr))
                (error/arg
                  :db.error/attribute-not-indexed
                  (str "attribute: " a " is not indexed")))]
        (datomic.db/windowed
          db
          (fn fn__12954
            ([p1__12953#]
              (and
                (= (long attrid) (long (.getA ^datomic.impl.db.IDatum p1__12953#)))
                (or
                  (nil? end)
                  (neg? (common/compare (.getV ^datomic.impl.db.IDatum p1__12953#) end))))))
          (.seekAVET ^datomic.db.IDb db (datomic.db/datum db :a (long attrid) :v start))))))
  (defn find-avet
    ([db a v]
      (let [attrid (datomic.db/require-attrid db a)
            iter (datomic.db/filter-retractions
                   (iter/take-while
                     (fn fn__12964
                       ([it]
                         (and
                           (= attrid (long (.getA ^datomic.impl.db.IDatum it)))
                           (or
                             (nil? v)
                             (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))))
                     (.seekAVET ^datomic.db.IDb db (datomic.db/datum db :a a :v v))))
            it (datomic.db/dget iter)]
        (when (and
                it
                (.isAssertion ^datomic.impl.db.IDatum it)
                (= attrid (long (.getA ^datomic.impl.db.IDatum it)))
                (or (nil? v) (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))
          iter)))
    ([db a] (datomic.db/find-avet db a nil)))
  (reset-meta!
    #'find-avet
    (assoc
      {:tag datomic.iter.Iter,
       :arglists (clojure.core/list ['db 'a] [(.withMeta 'db {:tag 'IDb}) 'a 'v]),
       :column 1}
      :name
      'find-avet
      :ns
      *ns*))
  (defn find-aevt
    ([db a e v]
      (let [attrid (datomic.db/require-attrid db a)
            iter (datomic.db/filter-retractions
                   (iter/take-while
                     (fn fn__12973
                       ([it]
                         (and
                           (= attrid (long (.getA ^datomic.impl.db.IDatum it)))
                           (or (nil? e) (= e (long (.getE ^datomic.impl.db.IDatum it))))
                           (or
                             (nil? v)
                             (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))))
                     (.seekAEVT
                       ^datomic.db.IDb db
                       (if (nil? v)
                         (datomic.db/datum db :a a :e e)
                         (datomic.db/datum db :a a :e e :v v)))))
            it (datomic.db/dget iter)]
        (when (and
                it
                (.isAssertion ^datomic.impl.db.IDatum it)
                (= attrid (long (.getA ^datomic.impl.db.IDatum it)))
                (or (nil? e) (= e (long (.getE ^datomic.impl.db.IDatum it))))
                (or (nil? v) (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))
          iter)))
    ([db a e] (datomic.db/find-aevt db a e nil))
    ([db a] (datomic.db/find-aevt db a nil nil)))
  (reset-meta!
    #'find-aevt
    (assoc
      {:tag datomic.iter.Iter,
       :arglists (clojure.core/list ['db 'a] ['db 'a 'e] [(.withMeta 'db {:tag 'IDb}) 'a 'e 'v]),
       :column 1}
      :name
      'find-aevt
      :ns
      *ns*))
  (defn find-eavt
    ([db e a v]
      (let [attrid (and a (datomic.db/require-attrid db a))
            iter (datomic.db/filter-retractions
                   (iter/take-while
                     (fn fn__12986
                       ([it]
                         (and
                           (or (nil? e) (= e (long (.getE ^datomic.impl.db.IDatum it))))
                           (or (nil? a) (= attrid (long (.getA ^datomic.impl.db.IDatum it))))
                           (or
                             (nil? v)
                             (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))))
                     (.seekEAVT
                       ^datomic.db.IDb db
                       (if (nil? v)
                         (datomic.db/datum db :e e :a a)
                         (datomic.db/datum db :e e :a a :v v)))))
            it (datomic.db/dget iter)]
        (when (and
                it
                (.isAssertion ^datomic.impl.db.IDatum it)
                (or (nil? e) (= e (long (.getE ^datomic.impl.db.IDatum it))))
                (or (nil? a) (= attrid (long (.getA ^datomic.impl.db.IDatum it))))
                (or (nil? v) (zero? (common/compare v (.getV ^datomic.impl.db.IDatum it)))))
          iter)))
    ([db e a] (datomic.db/find-eavt db e a nil))
    ([db e] (datomic.db/find-eavt db e nil nil)))
  (reset-meta!
    #'find-eavt
    (assoc
      {:tag datomic.iter.Iter,
       :arglists (clojure.core/list ['db 'e] ['db 'e 'a] [(.withMeta 'db {:tag 'IDb}) 'e 'a 'v]),
       :column 1}
      :name
      'find-eavt
      :ns
      *ns*))
  (defn find-raet
    ([db r a]
      (let [rid (and r (datomic.db/resolve-id db r))
            attrid (and a (datomic.db/require-attrid db a))
            iter (datomic.db/filter-retractions
                   (iter/take-while
                     (fn fn__13002
                       ([it]
                         (and
                           (= rid (.getV ^datomic.impl.db.IDatum it))
                           (or (nil? a) (= attrid (long (.getA ^datomic.impl.db.IDatum it)))))))
                     (.seekRAET ^datomic.db.IDb db (datomic.db/datum db :v rid :a a))))
            it (datomic.db/dget iter)]
        (when (and
                it
                (.isAssertion ^datomic.impl.db.IDatum it)
                (= rid (.getV ^datomic.impl.db.IDatum it))
                (or (nil? a) (= attrid (long (.getA ^datomic.impl.db.IDatum it)))))
          iter)))
    ([db r] (datomic.db/find-raet db r nil)))
  (reset-meta!
    #'find-raet
    (assoc
      {:tag datomic.iter.Iter,
       :arglists (clojure.core/list ['db 'r] [(.withMeta 'db {:tag 'IDb}) 'r 'a]),
       :column 1}
      :name
      'find-raet
      :ns
      *ns*))
  (defn get-entity
    ([db ent & p__13014]
      (let [map__13015 p__13014
            map__13015 (if (seq? map__13015)
                         (clojure.lang.PersistentHashMap/create (seq map__13015))
                         map__13015)
            raw (get map__13015 :raw)
            eid (datomic.db/resolve-id db ent)]
        (loop [iter (datomic.db/windowed
                      db
                      (fn fn__13016
                        ([p1__13013#] (= eid (long (.getE ^datomic.impl.db.IDatum p1__13013#)))))
                      (.seekEAVT ^datomic.db.IDb db (datomic.db/datum db :e eid)))
               ret nil]
          (if iter
            (let [d (datomic.db/dget iter)
                  attrid (.getA ^datomic.impl.db.IDatum d)
                  attr (.elementAt ^datomic.db.IDbImpl db (java.lang.Integer/valueOf (int attrid)))
                  attrk (if attr
                          (.kw ^datomic.db.Attribute attr)
                          (.keywordOf ^datomic.db.IDb db (java.lang.Integer/valueOf (int attrid))))
                  v (.getV ^datomic.impl.db.IDatum d)
                  vtypeid (and attr (.-vtypeid ^datomic.db.Attribute attr))
                  val (cond
                        raw v
                        (and attr (.-isComponent ^datomic.db.Attribute attr)) (datomic.db/get-entity
                                                                                db
                                                                                v)
                        (and vtypeid (= vtypeid 20)) (or (datomic.db/resolve-kw db v) v)
                        :else (do v))]
              (recur
                (iter/inext iter)
                (assoc
                  (or ret #:db{:id eid})
                  attrk
                  (if (and attr (= 36 (.-cardinality ^datomic.db.Attribute attr)))
                    (conj (get ret attrk #{}) val)
                    val))))
            ret)))))
  (defn entity-error-desc ([db eid] (or (.ident ^datomic.Database db eid) eid)))
  (reset-meta!
    #'entity-error-desc
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'eid]),
       :column 1}
      :name
      'entity-error-desc
      :ns
      *ns*))
  (defn v-error-desc ([v] (if (string? v) (if (< 64 (count v)) (str (subs v 0 61) "...") v) v)))
  (reset-meta!
    #'v-error-desc
    (assoc
      {:private true, :arglists (clojure.core/list ['v]), :column 1}
      :name
      'v-error-desc
      :ns
      *ns*))
  (defn datom-error-desc
    ([db p__13028]
      (let [vec__13029 p__13028
            e (nth vec__13029 (unchecked-int 0) nil)
            a (nth vec__13029 (unchecked-int 1) nil)
            v (nth vec__13029 (unchecked-int 2) nil)
            tx (nth vec__13029 (unchecked-int 3) nil)
            added (nth vec__13029 (unchecked-int 4) nil)
            attr (let [G__13032 (.entid ^datomic.Database db a)]
                   (when-not (nil? G__13032) (datomic.db/attribute db G__13032)))]
        (into
          [(datomic.db/entity-error-desc db e)
           (datomic.db/entity-error-desc db a)
           (if (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
             (datomic.db/entity-error-desc db v)
             (datomic.db/v-error-desc v))]
          (when tx [tx added])))))
  (reset-meta!
    #'datom-error-desc
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) ['e 'a 'v 'tx 'added]]),
       :column 1}
      :name
      'datom-error-desc
      :ns
      *ns*))
  (defn validate-hook-target
    ([db d]
      (when-not (= (.getE ^datomic.impl.db.IDatum d) 0)
        (let [a (datomic.db/entity-error-desc
                  db
                  (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))))
              e (datomic.db/entity-error-desc db (long (.getE ^datomic.impl.db.IDatum d)))]
          (error/arg
            :db.error/invalid-datom
            (str a " must be set on entity :db.part/db, found " e)
            {:a e, :e e})))
      (when-not (= (datomic.db/eid->part (unchecked-long (.getV ^datomic.impl.db.IDatum d))) 0)
        (let [a (datomic.db/entity-error-desc
                  db
                  (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))))
              v (datomic.db/entity-error-desc db (.getV ^datomic.impl.db.IDatum d))]
          (error/arg
            :db.error/not-in-system-partition
            (str "Value of " a " must be in :db.part/db partition, found " v)
            {:a a, :v v})))))
  (defn prevent-ident-retarget!
    ([db e v]
      (let [past_e (.entid ^datomic.Database db v)]
        (when (and past_e (not= past_e e) (get (:constituents db) past_e))
          (error/arg
            :db.error/cannot-retarget-ident
            (str
              "Ident "
              v
              " cannot be used for entity "
              (datomic.db/entity-error-desc db e)
              ", already used for "
              (datomic.db/entity-error-desc db past_e)))))))
  (reset-meta!
    #'prevent-ident-retarget!
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'e 'v]),
       :column 1}
      :name
      'prevent-ident-retarget!
      :ns
      *ns*))
  (defn key-hook
    ([_ db d _]
      (let [db (if (= (.getP ^datomic.impl.db.IDatum d) 0)
                 (update-in
                   (.growElements ^datomic.db.IDbImpl db (long (.getE ^datomic.impl.db.IDatum d)))
                   [:elements (long (.getE ^datomic.impl.db.IDatum d))]
                   (fn fn__13040
                     ([p1__13039#]
                       (when p1__13039#
                         (assoc p1__13039# :kw (.getV ^datomic.impl.db.IDatum d))))))
                 db)]
        (datomic.db/prevent-ident-retarget!
          db
          (long (.getE ^datomic.impl.db.IDatum d))
          (.getV ^datomic.impl.db.IDatum d))
        (.addKeyword
          ^datomic.db.IDbImpl db
          (.getV ^datomic.impl.db.IDatum d)
          (long (.getE ^datomic.impl.db.IDatum d))))))
  (reset-meta!
    #'key-hook
    (assoc
      {:private true,
       :arglists
       (clojure.core/list ['_ (.withMeta 'db {:tag 'IDbImpl}) (.withMeta 'd {:tag 'IDatum}) '_]),
       :column 1}
      :name
      'key-hook
      :ns
      *ns*))
  (defn install-partition-hook
    ([_ db d check?]
      (when check? (datomic.db/validate-hook-target db d))
      (let [partid (.getV ^datomic.impl.db.IDatum d)]
        (.addElement
          ^datomic.db.IDbImpl db
          (datomic.db.Partition. partid (.keywordOf ^datomic.db.IDb db partid))))))
  (reset-meta!
    #'install-partition-hook
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['_ (.withMeta 'db {:tag 'IDbImpl}) (.withMeta 'd {:tag 'IDatum}) 'check?]),
       :column 1}
      :name
      'install-partition-hook
      :ns
      *ns*))
  (defn install-vtype-hook
    ([_ db d check?]
      (when check? (datomic.db/validate-hook-target db d))
      (let [id (.getV ^datomic.impl.db.IDatum d)
            map__13044 (datomic.db/get-entity db id :raw true)
            map__13044 (if (seq? map__13044)
                         (clojure.lang.PersistentHashMap/create (seq map__13044))
                         map__13044)
            ent map__13044
            key (get map__13044 :db/ident)
            fressian_tag (get map__13044 :fressian/tag)]
        (when-not (every? identity [key fressian_tag])
          (error/arg
            :db.error/invalid-value-type
            (str
              "The entity "
              (or key id)
              " must specify :db/ident and :fressian/tag to be installed as a valueType")
            {:entity ent}))
        (.addElement ^datomic.db.IDbImpl db (datomic.db.ValueType. id key fressian_tag)))))
  (reset-meta!
    #'install-vtype-hook
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['_ (.withMeta 'db {:tag 'IDbImpl}) (.withMeta 'd {:tag 'IDatum}) 'check?]),
       :column 1}
      :name
      'install-vtype-hook
      :ns
      *ns*))
  (defn needs-avet? ([ent] (boolean (or (:db/index ent) (:db/unique ent)))))
  (reset-meta!
    #'needs-avet?
    (assoc
      {:private true, :arglists (clojure.core/list ['ent]), :column 1}
      :name
      'needs-avet?
      :ns
      *ns*))
  (defn attr-hook-attr-ids
    ([db]
      (into
        #{}
        (comp
          (map (fn fn__13050 ([p1__13049#] (.entid ^datomic.Database db p1__13049#))))
          (filter identity))
        [42 41 40 43 51 45 44 :db.attr/preds :db/tupleType :db/tupleTypes :db/tupleAttrs])))
  (reset-meta!
    #'attr-hook-attr-ids
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database})]), :column 1}
      :name
      'attr-hook-attr-ids
      :ns
      *ns*))
  (defn functional-attr-ids ([db] (conj (datomic.db/attr-hook-attr-ids db) 10)))
  (reset-meta!
    #'functional-attr-ids
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'functional-attr-ids
      :ns
      *ns*))
  (def required-schema-attrs #{40 41})
  (reset-meta!
    #'required-schema-attrs
    (assoc {:private true, :column 1} :name 'required-schema-attrs :ns *ns*))
  (def schema-hook-attrs #{13 19})
  (reset-meta!
    #'schema-hook-attrs
    (assoc {:private true, :column 1} :name 'schema-hook-attrs :ns *ns*))
  (def install-attrs #{13 12 11 14})
  (reset-meta! #'install-attrs (assoc {:private true, :column 1} :name 'install-attrs :ns *ns*))
  (def tuple-value-types
   #:db.type{:long java.lang.Long,
             :double java.lang.Double,
             :instant java.util.Date,
             :ref java.lang.Long,
             :bigint java.math.BigInteger,
             :symbol clojure.lang.Symbol,
             :string java.lang.String,
             :keyword clojure.lang.Keyword,
             :bigdec java.math.BigDecimal,
             :uri java.net.URI,
             :uuid java.util.UUID,
             :boolean java.lang.Boolean})
  (reset-meta!
    #'tuple-value-types
    (assoc {:private true, :column 1} :name 'tuple-value-types :ns *ns*))
  (defn installed-attribute? ([db eid] (datomic.db/attribute db eid)))
  (reset-meta!
    #'installed-attribute?
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'eid]),
       :column 1}
      :name
      'installed-attribute?
      :ns
      *ns*))
  (def TUP_MIN_ELEMS 2)
  (reset-meta! #'TUP_MIN_ELEMS (assoc {:const true, :column 1} :name 'TUP_MIN_ELEMS :ns *ns*))
  (def TUP_MAX_ELEMS 8)
  (reset-meta! #'TUP_MAX_ELEMS (assoc {:const true, :column 1} :name 'TUP_MAX_ELEMS :ns *ns*))
  (defn tuple-attr-value-type
    ([db attr_id]
      (let [attr (let [G__13055 (datomic.db/resolve-id db attr_id)]
                   (when-not (nil? G__13055) (datomic.db/attribute db G__13055)))
            type (let [G__13056 attr G__13056 (some-> G__13056 (.-vtypeid))]
                   (when-not (nil? G__13056) (.ident ^datomic.Database db G__13056)))]
        (when (and
                (contains? datomic.db/tuple-value-types type)
                (= 35 (.-cardinality ^datomic.db.Attribute attr)))
          type))))
  (reset-meta!
    #'tuple-attr-value-type
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'attr-id]),
       :column 1}
      :name
      'tuple-attr-value-type
      :ns
      *ns*))
  (defn attr-tuple-attrs ([db attr] (:tupleAttrs (datomic.db/require-attr db attr))))
  (reset-meta!
    #'attr-tuple-attrs
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'attr]), :column 1}
      :name
      'attr-tuple-attrs
      :ns
      *ns*))
  (defn datom-tuple-attrs
    ([db d]
      (datomic.db/attr-tuple-attrs
        db
        (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))))))
  (reset-meta!
    #'datom-tuple-attrs
    (assoc
      {:private true, :arglists (clojure.core/list ['db (.withMeta 'd {:tag 'IDatum})]), :column 1}
      :name
      'datom-tuple-attrs
      :ns
      *ns*))
  (defn tuple-install-errors
    ([db e]
      (let [type (get e :db/tupleType)
            types (get e :db/tupleTypes)
            attrs (get e :db/tupleAttrs)
            temp__5457__auto__ (datomic.db/system-eid db :db.type/tuple)]
        (when temp__5457__auto__
          (let [TYPE_TUPLE temp__5457__auto__]
            (cond
              (= TYPE_TUPLE (get e :db/valueType)) (seq
                                                     (map
                                                       (fn fn__13063
                                                         ([kw_or_m]
                                                           (if
                                                             (keyword? kw_or_m)
                                                             {:db/error kw_or_m, :entity e}
                                                             kw_or_m)))
                                                       (remove
                                                         nil?
                                                         [(when-not (=
                                                                      1
                                                                      (count
                                                                        (remove
                                                                          nil?
                                                                          [type types attrs])))
                                                            :db.error/invalid-tuple-kind)
                                                          (when (and
                                                                  type
                                                                  (not
                                                                    (contains?
                                                                      datomic.db/tuple-value-types
                                                                      type)))
                                                            :db.error/invalid-tuple-type)
                                                          (when (and
                                                                  types
                                                                  (not-every?
                                                                    datomic.db/tuple-value-types
                                                                    types))
                                                            :db.error/invalid-tuple-types)
                                                          (when (and
                                                                  attrs
                                                                  (not=
                                                                    35
                                                                    (get e :db/cardinality)))
                                                            {:db/error
                                                             :db.error/tuple-of-attrs-must-be-card-one,
                                                             :entity e})
                                                          (when (and
                                                                  attrs
                                                                  (not-every?
                                                                    (fn 
                                                                      fn__13065
                                                                      ([p1__13061#]
                                                                        (datomic.db/tuple-attr-value-type
                                                                          db
                                                                          p1__13061#)))
                                                                    attrs))
                                                            #:db{:error
                                                                 :db.error/invalid-tuple-attrs,
                                                                 :tupleAttrs
                                                                 (mapv
                                                                   (fn 
                                                                     fn__13067
                                                                     ([p1__13062#]
                                                                       (datomic.db/entity-error-desc
                                                                         db
                                                                         p1__13062#)))
                                                                   attrs)})
                                                          (when (and
                                                                  types
                                                                  (not
                                                                    (<=
                                                                      2
                                                                      (java.lang.Integer/valueOf
                                                                        (int (count types)))
                                                                      8)))
                                                            :db.error/invalid-tuple-length)])))
              (or type types attrs) (do {:db/error :db.error/type-on-non-tuple, :entity e})))))))
  (reset-meta!
    #'tuple-install-errors
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'e]), :column 1}
      :name
      'tuple-install-errors
      :ns
      *ns*))
  (defn install-attribute-errors
    ([before after eid]
      (let [eafter (datomic.db/get-entity after eid :raw true)
            errors (reduce
                     (fn fn__13080
                       ([errs k]
                         (if (get eafter k)
                           errs
                           (cons
                             {:db/error :db.error/schema-attribute-missing,
                              :attribute k,
                              :entity eafter}
                             errs))))
                     nil
                     (map
                       (fn fn__13082 ([p1__13078#] (.ident ^datomic.Database after p1__13078#)))
                       datomic.db/required-schema-attrs))
            errors (seq (concat (datomic.db/tuple-install-errors after eafter) errors))
            errors (if (and (get eafter :db/unique) (= 27 (get eafter :db/valueType)))
                     (cons {:db/error :db.error/unique-not-allowed, :entity eafter} errors)
                     errors)
            errors (if (and (get eafter :db/isComponent) (not= 20 (get eafter :db/valueType)))
                     (cons {:db/error :db.error/components-must-be-refs, :entity eafter} errors)
                     errors)]
        (if (and before (datomic.db/installed-attribute? before eid))
          (let [ebefore (datomic.db/get-entity before eid :raw true)]
            (reduce
              (fn fn__13084
                ([errs k]
                  (if (and
                        (or (^clojure.lang.IFn ebefore k) (^clojure.lang.IFn eafter k))
                        (not= (^clojure.lang.IFn ebefore k) (^clojure.lang.IFn eafter k)))
                    (cons
                      {:db/error :db.error/incompatible-schema-install,
                       :entity (.ident ^datomic.Database before eid),
                       :attribute k,
                       :was (.ident ^datomic.Database before (^clojure.lang.IFn ebefore k)),
                       :requested (.ident ^datomic.Database after (^clojure.lang.IFn eafter k))}
                      errs)
                    errs)))
              errors
              (map
                (fn fn__13088 ([p1__13079#] (.ident ^datomic.Database before p1__13079#)))
                (datomic.db/functional-attr-ids before))))
          errors))))
  (defn reserved-keyword?
    ([kw]
      (let [temp__5457__auto__ (namespace kw)]
        (when temp__5457__auto__
          (let [ns temp__5457__auto__] (or (= ns "db") (str/starts-with? ns "db.")))))))
  (defn create-attr-pred
    ([kw fn_names]
      (let [fn_map (into
                     {}
                     (map (fn fn__13097 ([name] [name (common/requiring-resolve! name)])))
                     fn_names)]
        (fn fn__13099
          ([e v idmap]
            (loop [seq_13100 (seq fn_map) chunk_13101 nil count_13102 0 i_13103 0]
              (if (< i_13103 count_13102)
                (let [vec__13104 (.nth ^clojure.lang.Indexed chunk_13101 (unchecked-int i_13103))
                      name (nth vec__13104 (unchecked-int 0) nil)
                      pred (nth vec__13104 (unchecked-int 1) nil)]
                  (let [result (^clojure.lang.IFn pred v)]
                    (when-not (true? result)
                      (throw
                        (error/arg
                          :db.error/attr-pred
                          (str
                            "Entity "
                            (get (set/map-invert idmap) e e)
                            " attribute "
                            kw
                            " value "
                            v
                            " failed pred "
                            name)
                          #:db.error{:pred-return result}))))
                  (recur seq_13100 chunk_13101 count_13102 (inc i_13103)))
                (let [temp__5457__auto__ (seq seq_13100)]
                  (when temp__5457__auto__
                    (let [seq_13100 temp__5457__auto__]
                      (if (chunked-seq? seq_13100)
                        (let [c__5719__auto__ (chunk-first seq_13100)]
                          (recur (chunk-rest seq_13100) c__5719__auto__ (count c__5719__auto__) 0))
                        (let [vec__13107 (first seq_13100)
                              name (nth vec__13107 (unchecked-int 0) nil)
                              pred (nth vec__13107 (unchecked-int 1) nil)]
                          (let [result (^clojure.lang.IFn pred v)]
                            (when-not (true? result)
                              (throw
                                (error/arg
                                  :db.error/attr-pred
                                  (str
                                    "Entity "
                                    (get (set/map-invert idmap) e e)
                                    " attribute "
                                    kw
                                    " value "
                                    v
                                    " failed pred "
                                    name)
                                  #:db.error{:pred-return result}))))
                          (recur (next seq_13100) nil 0 0)))))))))))))
  (defn create-attribute
    ([db p__13114]
      (let [map__13115 p__13114
            map__13115 (if (seq? map__13115)
                         (clojure.lang.PersistentHashMap/create (seq map__13115))
                         map__13115)
            cardinality (get map__13115 :cardinality)
            attrPreds (get map__13115 :attrPreds)
            unique (get map__13115 :unique)
            vtypeid (get map__13115 :vtypeid)
            index (get map__13115 :index)
            storageHasAVET (get map__13115 :storageHasAVET)
            tupleType (get map__13115 :tupleType)
            tupleTypes (get map__13115 :tupleTypes)
            fulltext (get map__13115 :fulltext)
            noHistory (get map__13115 :noHistory)
            isComponent (get map__13115 :isComponent)
            kw (get map__13115 :kw)
            needsAVET (get map__13115 :needsAVET)
            id (get map__13115 :id)
            tupleAttrs (get map__13115 :tupleAttrs)
            norm (fn norm ([x] (if (nil? x) false x)))
            tupleRefOffsets (cond
                              (= :db.type/ref tupleType) (range 8)
                              tupleTypes (keep-indexed
                                           (fn fn__13118
                                             ([idx item] (when (= :db.type/ref item) idx)))
                                           tupleTypes)
                              tupleAttrs (do
                                           (keep-indexed
                                             (fn fn__13120
                                               ([idx item]
                                                 (when (=
                                                         20
                                                         (.-vtypeid
                                                           (datomic.db/require-attr db item)))
                                                   idx)))
                                             tupleAttrs)))]
        (cond->
          (datomic.db/map->Attribute
            {:unique unique,
             :vtypeid vtypeid,
             :storageHasAVET (^clojure.lang.IFn norm storageHasAVET),
             :index (^clojure.lang.IFn norm index),
             :fulltext (^clojure.lang.IFn norm fulltext),
             :noHistory (^clojure.lang.IFn norm noHistory),
             :isComponent (^clojure.lang.IFn norm isComponent),
             :kw kw,
             :needsAVET (^clojure.lang.IFn norm needsAVET),
             :id id,
             :cardinality cardinality})
          attrPreds
          (assoc :attrPred (delay (datomic.db/create-attr-pred kw attrPreds)))
          tupleType
          (assoc :tupleType tupleType)
          tupleTypes
          (assoc :tupleTypes tupleTypes)
          tupleRefOffsets
          (assoc :tupleRefOffsets (set tupleRefOffsets))
          tupleAttrs
          (assoc :tupleAttrs tupleAttrs)))))
  (defn add-constituents
    ([db comp_id constituents]
      (let [m (into
                {}
                (map
                  (fn fn__13127
                    ([constituent] [(datomic.db/require-attrid db constituent) #{comp_id}])))
                constituents)]
        (update db :constituents (fn fn__13129 ([p1__13126#] (merge-with into p1__13126# m)))))))
  (reset-meta!
    #'add-constituents
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'comp-id 'constituents]), :column 1}
      :name
      'add-constituents
      :ns
      *ns*))
  (defn install-attribute-hook
    ([before after d check?]
      (when check? (datomic.db/validate-hook-target after d))
      (let [id (.getV ^datomic.impl.db.IDatum d)
            map__13132 (datomic.db/get-entity after id :raw true)
            map__13132 (if (seq? map__13132)
                         (clojure.lang.PersistentHashMap/create (seq map__13132))
                         map__13132)
            ent map__13132
            cardinality (get map__13132 :db/cardinality)
            attrPreds (get map__13132 :db.attr/preds)
            unique (get map__13132 :db/unique)
            vtypeid (get map__13132 :db/valueType)
            index (get map__13132 :db/index)
            tupleType (get map__13132 :db/tupleType)
            tupleTypes (get map__13132 :db/tupleTypes)
            fulltext (get map__13132 :db/fulltext)
            noHistory (get map__13132 :db/noHistory)
            isComponent (get map__13132 :db/isComponent)
            kw (get map__13132 :db/ident)
            tupleAttrs (get map__13132 :db/tupleAttrs)
            kw (or kw (datomic.db/resolve-kw after id))
            fulltext (and fulltext (= vtypeid (get datomic.db/BOOT-IDS :db.type/string)))
            avet (datomic.db/needs-avet? ent)]
        (when check?
          (when-not kw
            (error/arg
              :db.error/attribute-ident-missing
              (str "Missing :db/ident for " ent)
              {:entity ent}))
          (let [temp__5457__auto__ (datomic.db/install-attribute-errors before after id)]
            (when temp__5457__auto__
              (let [errors temp__5457__auto__]
                (error/arg
                  :db.error/invalid-install-attribute
                  (str "First error: " (:db/error (first errors)))
                  #:db{:errors errors}))))
          (when-not (datomic.db/value-type before vtypeid)
            (error/arg
              :db.error/not-a-value-type
              (str "Not a value type: " (datomic.db/entity-error-desc before vtypeid))
              {:entity ent}))
          (when-not (or (= 36 cardinality) (= 35 cardinality))
            (error/arg
              :db.error/not-a-cardinality
              (str "Not a cardinality: " (datomic.db/entity-error-desc before cardinality))
              {:entity ent})))
        (let [always_indexed (:cloud-compat before)
              attr (datomic.db/create-attribute
                     after
                     {:unique unique,
                      :vtypeid vtypeid,
                      :storageHasAVET (or avet always_indexed),
                      :index (or index always_indexed),
                      :tupleType tupleType,
                      :tupleTypes tupleTypes,
                      :fulltext fulltext,
                      :noHistory noHistory,
                      :isComponent isComponent,
                      :kw kw,
                      :needsAVET (or avet always_indexed),
                      :id id,
                      :tupleAttrs tupleAttrs,
                      :cardinality cardinality,
                      :attrPreds attrPreds})
              db (.addElement ^datomic.db.IDbImpl after ^datomic.db.IElementImpl attr)]
          (if tupleAttrs (datomic.db/add-constituents db id tupleAttrs) db)))))
  (reset-meta!
    #'install-attribute-hook
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'before {:tag 'IDbImpl})
          (.withMeta 'after {:tag 'IDbImpl})
          (.withMeta 'd {:tag 'IDatum})
          'check?]),
       :column 1}
      :name
      'install-attribute-hook
      :ns
      *ns*))
  (defn card-one-violator
    ([db aid]
      (let [result (reduce
                     (fn fn__13141
                       ([d1 d2]
                         (if (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
                           (reduced [d1 d2])
                           d2)))
                     (datomic.db/asserting-datum -1 -1 -1 -1)
                     (.datoms ^datomic.Database db :aevt (to-array [aid])))]
        (when (vector? result) result))))
  (reset-meta!
    #'card-one-violator
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'aid]),
       :column 1}
      :name
      'card-one-violator
      :ns
      *ns*))
  (defn unique-violator
    ([db aid]
      (let [result (reduce
                     (fn fn__13144
                       ([d1 d2]
                         (if (= (common/compare (.v ^datomic.Datom d1) (.v ^datomic.Datom d2)) 0)
                           (reduced [d1 d2])
                           d2)))
                     (datomic.db/asserting-datum -1 -1 -1 -1)
                     (.datoms ^datomic.Database db :avet (to-array [aid])))]
        (when (vector? result) result))))
  (reset-meta!
    #'unique-violator
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'aid]),
       :column 1}
      :name
      'unique-violator
      :ns
      *ns*))
  (defn set-element-fields
    ([db aid & kvs]
      (update-in db [:elements aid] (fn fn__13148 ([p1__13147#] (apply assoc p1__13147# kvs))))))
  (reset-meta!
    #'set-element-fields
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'aid '& 'kvs]), :column 1}
      :name
      'set-element-fields
      :ns
      *ns*))
  (defn card-many->card-one
    ([db aid _ _]
      (let [temp__5455__auto__ (datomic.db/card-one-violator db aid)]
        (if temp__5455__auto__
          (let [problem temp__5455__auto__]
            [db [{:db/error :db.error/cardinality-violation, :datoms problem}]])
          [(datomic.db/set-element-fields db aid :cardinality 35)]))))
  (reset-meta!
    #'card-many->card-one
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'aid '_ '_]),
       :column 1}
      :name
      'card-many->card-one
      :ns
      *ns*))
  (defn constituent-of
    ([db aid]
      (when (datomic.db/supports-tuples? db)
        (Circular/q
          [:find
           ['?ident '...]
           :in
           '$
           '?aid
           :where
           ['?aid :db/ident '?attr]
           ['?comp :db/tupleAttrs '?attrs]
           [(clojure.core/list 'set '?attrs) '?attr-set]
           [(clojure.core/list 'contains? '?attr-set '?attr)]
           ['?comp :db/ident '?ident]]
          [db aid]))))
  (reset-meta!
    #'constituent-of
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'aid]),
       :column 1}
      :name
      'constituent-of
      :ns
      *ns*))
  (defn card-one->card-many
    ([db aid _ vafter]
      (if (:tupleAttrs (datomic.db/attribute db aid))
        [db
         [{:db/error :db.error/tuple-of-attrs-must-be-card-one,
           :attribute (.ident ^datomic.Database db aid)}]]
        (let [temp__5455__auto__ (seq (datomic.db/constituent-of db aid))]
          (if temp__5455__auto__
            (let [composites temp__5455__auto__]
              [db
               [{:db/error :db.error/constituent-of-a-composite-must-be-card-one,
                 :attribute (.ident ^datomic.Database db aid)}]])
            [(assoc-in db [:elements aid :cardinality] vafter)])))))
  (reset-meta!
    #'card-one->card-many
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'aid '_ 'vafter]),
       :column 1}
      :name
      'card-one->card-many
      :ns
      *ns*))
  (defn drop-avet
    ([db aid _ _]
      (let [unique? (.-unique (datomic.db/attribute db aid))]
        [(if unique?
           (datomic.db/set-element-fields db aid :index false)
           (datomic.db/set-element-fields db aid :index false :needsAVET false))])))
  (reset-meta!
    #'drop-avet
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'aid '_ '_]), :column 1}
      :name
      'drop-avet
      :ns
      *ns*))
  (defn drop-unique
    ([db aid _ _]
      (let [indexed? (.-index (datomic.db/attribute db aid))]
        [(if indexed?
           (datomic.db/set-element-fields db aid :unique nil)
           (datomic.db/set-element-fields db aid :unique nil :index false :needsAVET false))])))
  (reset-meta!
    #'drop-unique
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'aid '_ '_]), :column 1}
      :name
      'drop-unique
      :ns
      *ns*))
  (defn has-values?
    ([db attrid]
      (boolean (seq (datomic.db/datoms (.history ^datomic.Database db) :aevt [attrid])))))
  (defn has-background-indexing? ([db] (boolean (:index db))))
  (defn can-immediately-toggle-storage-has-avet?
    ([db aid]
      (not (and (datomic.db/has-background-indexing? db) (datomic.db/has-values? db aid)))))
  (defn add-unique
    ([db aid _ unique]
      (let [attr (datomic.db/attribute db aid)]
        (if (= 27 (.-vtypeid ^datomic.db.Attribute attr))
          [db
           [{:db/error :db.error/unique-not-allowed,
             :attribute (datomic.db/entity-error-desc db aid)}]]
          (if (.hasAVET ^datomic.db.Attribute attr)
            (let [temp__5455__auto__ (datomic.db/unique-violator db aid)]
              (if temp__5455__auto__
                (let [problem temp__5455__auto__]
                  [db [{:db/error :db.error/unique-violation, :datoms problem}]])
                [(datomic.db/set-element-fields db aid :unique unique)]))
            (if (datomic.db/can-immediately-toggle-storage-has-avet? db aid)
              [(datomic.db/set-element-fields
                 db
                 aid
                 :storageHasAVET
                 true
                 :needsAVET
                 true
                 :unique
                 unique)]
              [db
               [{:db/error :db.error/unique-without-index,
                 :attribute (datomic.db/entity-error-desc db aid)}]]))))))
  (defn memory-db ([db] (assoc db :index nil :mid-index nil :indexing nil :history nil)))
  (reset-meta!
    #'memory-db
    (assoc
      {:tag datomic.Database, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'memory-db
      :ns
      *ns*))
  (defn t-needing-avet
    ([db d]
      (let [a (.a ^datomic.Datom d)]
        (and
          (or (= a 44) (= a 42))
          (.v ^datomic.Datom d)
          (.added ^datomic.Datom d)
          (let [prior (.asOf ^datomic.Database db (dec (.tx ^datomic.Datom d)))
                ent (.entity ^datomic.Database prior (.e ^datomic.Datom d))]
            (and
              (not (:db/unique ent))
              (not (:db/index ent))
              (datomic.db/has-values? prior (.e ^datomic.Datom d))
              (long (datomic.db/eid->eidx (unchecked-long (.tx ^datomic.Datom d))))))))))
  (defn t-needing-excise
    ([db d]
      (and
        (= (.a ^datomic.Datom d) 15)
        (long (datomic.db/eid->eidx (unchecked-long (.tx ^datomic.Datom d)))))))
  (defn t-needing-index-job
    ([db d] (or (datomic.db/t-needing-avet db d) (datomic.db/t-needing-excise db d))))
  (defn data-needs-index?
    ([db datoms]
      (reduce
        (fn fn__13177 ([x d] (if (datomic.db/t-needing-index-job db d) (reduced true) x)))
        false
        datoms)))
  (defn system-schema-namespace? ([s] (or (= "db" s) (= "fressian" s) (str/starts-with? s "db."))))
  (defn system-schema-datom?
    ([db d]
      (let [temp__5457__auto__ (some->
                                 (.ident
                                   ^datomic.Database db
                                   (long (.getE ^datomic.impl.db.IDatum d)))
                                 (namespace))]
        (when temp__5457__auto__
          (let [n temp__5457__auto__] (datomic.db/system-schema-namespace? n))))))
  (defn system-datom?
    ([db d]
      (or
        (datomic.db/system-schema-datom? db d)
        (contains?
          datomic.db/SYSTEM_ATTRS
          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))))))
  (defn memdb-needs-index?
    ([db]
      (let [mdb (datomic.db/memory-db db)]
        (or
          (datomic.db/data-needs-index?
            db
            (.datoms ^datomic.Database mdb :aevt (object-array [15])))
          (datomic.db/data-needs-index?
            db
            (remove
              (fn fn__13189 ([p1__13188#] (datomic.db/system-datom? db p1__13188#)))
              (.datoms ^datomic.Database mdb :aevt (object-array [44]))))))))
  (defn ts-needing-index
    ([db type limit_t]
      (let [mdb (.asOf (datomic.db/memory-db db) limit_t)
            pred (let [G__13193 type]
                   (case
                     G__13193
                     :excise
                     datomic.db/t-needing-excise
                     :schema
                     datomic.db/t-needing-avet))]
        (filter
          identity
          (map
            (partial pred db)
            (.datoms
              ^datomic.Database mdb
              :aevt
              (object-array [(let [G__13194 type] (case G__13194 :excise 15 :schema 44))])))))))
  (defn is-indexing? ([db] (boolean (:indexing db))))
  (defn attribute-seq ([db] (filter (partial instance? datomic.db.Attribute) (:elements db))))
  (defn add-avet
    ([db aid _ _]
      (if (.-unique (datomic.db/attribute db aid))
        [(datomic.db/set-element-fields db aid :index true)]
        (let [avet (into
                     (:avet (:memidx db))
                     (iter/iter-seq
                       (iter/take-while
                         (fn fn__13199
                           ([p1__13198#]
                             (= aid (long (.getA ^datomic.impl.db.IDatum p1__13198#)))))
                         (datomic.btset/seek (:aevt (:memidx db)) (datomic.db/datum db :a aid)))))]
          [(datomic.db/set-element-fields
             (assoc-in db [:memidx :avet] avet)
             aid
             :index
             true
             :needsAVET
             true
             :storageHasAVET
             (datomic.db/can-immediately-toggle-storage-has-avet? db aid))]))))
  (reset-meta!
    #'add-avet
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'aid '_ '_]), :column 1}
      :name
      'add-avet
      :ns
      *ns*))
  (defn create-simple-alter
    ([elem_key]
      (fn fn__13202
        ([db aid _ vafter]
          [(assoc-in db [:elements aid elem_key] (if (= :disabled vafter) false vafter))]))))
  (reset-meta!
    #'create-simple-alter
    (assoc
      {:private true, :arglists (clojure.core/list ['elem-key]), :column 1}
      :name
      'create-simple-alter
      :ns
      *ns*))
  (def alter-fns
   (let [simple_unique (datomic.db/create-simple-alter :unique)
         simple_is_component (datomic.db/create-simple-alter :isComponent)
         simple_no_history (datomic.db/create-simple-alter :noHistory)]
     {[42 38 37] simple_unique,
      [42 37 :disabled] datomic.db/drop-unique,
      [44 :disabled true] datomic.db/add-avet,
      [45 :disabled true] simple_no_history,
      [45 true :disabled] simple_no_history,
      [41 35 36] datomic.db/card-one->card-many,
      [41 36 35] datomic.db/card-many->card-one,
      [42 :disabled 37] datomic.db/add-unique,
      [42 38 :disabled] datomic.db/drop-unique,
      [43 true :disabled] simple_is_component,
      [42 37 38] simple_unique,
      [43 :disabled true] simple_is_component,
      [42 :disabled 38] datomic.db/add-unique,
      [44 true :disabled] datomic.db/drop-avet}))
  (reset-meta! #'alter-fns (assoc {:private true, :column 1} :name 'alter-fns :ns *ns*))
  (defn find-alter-fn
    ([db eid fid from to]
      (cond
        (= from to) (fn fn__13207 ([db _ _ _] [db nil]))
        (= fid (datomic.db/system-eid db :db.attr/preds)) (fn fn__13209
                                                            ([db aid _ preds]
                                                              (if
                                                                (= :disabled preds)
                                                                [(update-in
                                                                   db
                                                                   [:elements aid]
                                                                   dissoc
                                                                   :attrPred)]
                                                                [(assoc-in
                                                                   db
                                                                   [:elements aid :attrPred]
                                                                   (delay
                                                                     (datomic.db/create-attr-pred
                                                                       (.ident
                                                                         ^datomic.Database db
                                                                         aid)
                                                                       preds)))])))
        :default (do
                   (get
                     datomic.db/alter-fns
                     [fid from to]
                     (fn fn__13213
                       ([db _ _ _]
                         [db
                          [{:db/error :db.error/unsupported-alter-schema,
                            :entity (datomic.db/entity-error-desc db eid),
                            :attribute (datomic.db/entity-error-desc db fid),
                            :from (datomic.db/entity-error-desc db from),
                            :to (datomic.db/entity-error-desc db to)}]])))))))
  (defn alter-attribute
    ([before after d]
      (let [eid (.getV ^datomic.impl.db.IDatum d)
            ebefore (datomic.db/get-entity before eid :raw true)
            eafter (datomic.db/get-entity after eid :raw true)
            attr (fn attr
                   ([db ent id]
                     (let [temp__5455__auto__ (get ent (.ident ^datomic.Database db id))]
                       (if temp__5455__auto__ (let [e temp__5455__auto__] e) :disabled))))]
        (when (and
                (get eafter :db.attr/preds)
                (datomic.db/reserved-keyword? (get eafter :db/ident)))
          (error/arg
            :db.error/attr-pred-on-system-attr
            (str
              ":db.attr/preds cannot be added to system attribute "
              (datomic.db/entity-error-desc after eid))))
        (reduce
          (fn fn__13220
            ([p__13219 fid]
              (let [vec__13221 p__13219
                    db (nth vec__13221 (unchecked-int 0) nil)
                    prev_errors (nth vec__13221 (unchecked-int 1) nil)
                    vbefore (^clojure.lang.IFn attr before ebefore fid)
                    vafter (^clojure.lang.IFn attr after eafter fid)
                    f (datomic.db/find-alter-fn before eid fid vbefore vafter)
                    vec__13224 (^clojure.lang.IFn f db eid vbefore vafter)
                    db (nth vec__13224 (unchecked-int 0) nil)
                    errors (nth vec__13224 (unchecked-int 1) nil)]
                [db (concat errors prev_errors)])))
          [after nil]
          (datomic.db/attr-hook-attr-ids before)))))
  (defn alter-attribute-hook
    ([before after d check?]
      (when check?
        (datomic.db/validate-hook-target after d)
        (let [eid (.getV ^datomic.impl.db.IDatum d)]
          (when-not (datomic.db/installed-attribute? before eid)
            (error/arg
              :db.error/invalid-alter-attribute
              (str
                "Cannot alter attribute that does not exist: "
                (datomic.db/entity-error-desc before eid))))))
      (let [vec__13230 (datomic.db/alter-attribute before after d)
            db (nth vec__13230 (unchecked-int 0) nil)
            errors (nth vec__13230 (unchecked-int 1) nil)]
        (when (and check? (seq errors))
          (error/arg
            :db.error/invalid-alter-attribute
            (str "Error: " (first errors))
            #:db{:errors errors}))
        db)))
  (defn system-tx-hook
    ([_ after d check?]
      (let [G__13235 (.getV ^datomic.impl.db.IDatum d)]
        (case G__13235 :schema (datomic.db/update-schema-level after) after))))
  (defn safe-compile-function
    ([db lang code]
      (let [try_compile (fn try_compile
                          ([p1__13237#]
                            (try
                              (^clojure.lang.IFn p1__13237#)
                              (catch
                                java.lang.Throwable
                                t
                                (fn fn__13239
                                  ([db m]
                                    (error/arg
                                      :db.error/data-function-compile-failed
                                      (str "Data function failed to compile" t))))))))]
        (cond
          (= lang (datomic.db/resolve-id db :db.lang/clojure)) (^clojure.lang.IFn try_compile
                                                                 (fn 
                                                                   fn__13242
                                                                   ([] (eval (read-string code)))))
          (= lang (datomic.db/resolve-id db :db.lang/java)) (^clojure.lang.IFn try_compile
                                                              (fn 
                                                                fn__13244
                                                                ([] (janino/java-data-fn code))))
          :else (do
                  (error/arg
                    :db.error/source-lang-not-supported
                    (str
                      "Source lang: "
                      (datomic.db/resolve-kw db lang)
                      " not supported yet")))))))
  (defn install-function-hook
    ([_ db d check?]
      (when-not (= (.getE ^datomic.impl.db.IDatum d) 0)
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str (clojure.core/list 'zero? (clojure.core/list '.getE 'd)))))))
      (let [id (.getV ^datomic.impl.db.IDatum d)
            map__13247 (datomic.db/get-entity db id :raw true)
            map__13247 (if (seq? map__13247)
                         (clojure.lang.PersistentHashMap/create (seq map__13247))
                         map__13247)
            ent map__13247
            key (get map__13247 :db/ident)
            lang (get map__13247 :db/lang)
            code (get map__13247 :db/code)]
        (when check?
          (when-not (every? identity [key lang code])
            (error/arg
              :db.error/invalid-data-function
              (str
                "The entity "
                (or key id)
                " must specify :db/ident, :db/lang, and :db/code to be installed as a function.")
              {:entity ent})))
        (let [code (.replaceAll ^java.lang.String code "momentic" "datomic")
              f (datomic.db/safe-compile-function db lang code)]
          (.addElement ^datomic.db.IDbImpl db (datomic.db.Function. id key lang code f))))))
  (reset-meta!
    #'install-function-hook
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['_ (.withMeta 'db {:tag 'IDbImpl}) (.withMeta 'd {:tag 'IDatum}) 'check?]),
       :column 1}
      :name
      'install-function-hook
      :ns
      *ns*))
  (defn growvec ([v n] (loop [v v i (count v)] (if (<= i n) (recur (conj v nil) (inc i)) v))))
  (reset-meta!
    #'growvec
    (assoc
      {:private true, :arglists (clojure.core/list ['v 'n]), :column 1}
      :name
      'growvec
      :ns
      *ns*))
  (def hooks [])
  (defn add-hook
    ([id f]
      nil
      (def hooks (assoc (datomic.db/growvec datomic.db/hooks id) id f))
      #'datomic.db/hooks))
  (reset-meta!
    #'add-hook
    (assoc
      {:private true, :arglists (clojure.core/list ['id 'f]), :column 1}
      :name
      'add-hook
      :ns
      *ns*))
  (defn get-hook ([id] (when (< id (count datomic.db/hooks)) (datomic.db/hooks id))))
  (reset-meta!
    #'get-hook
    (assoc
      {:private true, :arglists (clojure.core/list ['id]), :column 1}
      :name
      'get-hook
      :ns
      *ns*))
  (datomic.db/add-hook 10 datomic.db/key-hook)
  (datomic.db/add-hook 11 datomic.db/install-partition-hook)
  (datomic.db/add-hook 12 datomic.db/install-vtype-hook)
  (datomic.db/add-hook 13 datomic.db/install-attribute-hook)
  (datomic.db/add-hook 14 datomic.db/install-function-hook)
  (datomic.db/add-hook 19 datomic.db/alter-attribute-hook)
  (datomic.db/add-hook 7 datomic.db/system-tx-hook)
  (defn fulltext?
    ([db attr]
      (let [temp__5457__auto__ (datomic.db/resolve-id db attr)]
        (when temp__5457__auto__
          (let [attrid temp__5457__auto__
                temp__5457__auto__ (.elementAt ^datomic.db.IDbImpl db attrid)]
            (when temp__5457__auto__
              (let [a temp__5457__auto__] (.-fulltext ^datomic.db.Attribute a))))))))
  (defn fulltext-attrs
    ([db]
      (filter
        (partial datomic.db/fulltext? db)
        (:db.install/attribute (datomic.db/get-entity db 0)))))
  (defn scan-aevt
    ([aevt attrid]
      (let [d (datomic.db/asserting-datum
                java.lang.Long/MIN_VALUE
                (unchecked-long ^java.lang.Number attrid)
                nil
                (quot java.lang.Long/MAX_VALUE 4))]
        (iter/take-while
          (fn fn__13258
            ([p1__13257#] (= attrid (long (.getA ^datomic.impl.db.IDatum p1__13257#)))))
          (datomic.btset/seek aevt d)))))
  (reset-meta!
    #'scan-aevt
    (assoc
      {:tag datomic.iter.Iter, :arglists (clojure.core/list ['aevt 'attrid]), :column 1}
      :name
      'scan-aevt
      :ns
      *ns*))
  (declare datomic.db/with-tx+opts)
  (declare datomic.db/add-fulltext)
  (defn as-of-t
    ([db t_or_date]
      (if (instance? java.util.Date t_or_date)
        (let [d (datomic.db/dget
                  (.seekAVET
                    ^datomic.db.IDb db
                    (datomic.db/datum db :a :db/txInstant :v t_or_date)))]
          (if (and
                d
                (=
                  (datomic.db/resolve-id db :db/txInstant)
                  (long (.getA ^datomic.impl.db.IDatum d))))
            (if (= (.getV ^datomic.impl.db.IDatum d) t_or_date)
              (long (.getT ^datomic.impl.db.IDatum d))
              (long (dec (.getT ^datomic.impl.db.IDatum d))))
            (:nextT db)))
        (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t_or_date))))))
  (defn t-at-or-since
    ([db t_or_date]
      (if (instance? java.util.Date t_or_date)
        (let [d (datomic.db/dget
                  (.seekAVET
                    ^datomic.db.IDb db
                    (datomic.db/datum db :a :db/txInstant :v t_or_date)))]
          (if (and
                d
                (=
                  (datomic.db/resolve-id db :db/txInstant)
                  (long (.getA ^datomic.impl.db.IDatum d))))
            (long (.getT ^datomic.impl.db.IDatum d))
            (:nextT db)))
        (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t_or_date))))))
  (defn entid-at
    ([db partition t_or_date]
      (let [partition (datomic.db/partbits db partition)
            t (if (instance? java.util.Date t_or_date)
                (let [d (datomic.db/dget
                          (.seekAVET ^datomic.db.IDb db (datomic.db/datum db :a 50 :v t_or_date)))]
                  (if (and d (= 50 (long (.getA ^datomic.impl.db.IDatum d))))
                    (long (.getT ^datomic.impl.db.IDatum d))
                    (:nextT db)))
                (let [tpart (datomic.db/eid->part (unchecked-long ^java.lang.Number t_or_date))]
                  (when-not (or (zero? tpart) (= 3 (long tpart)))
                    (throw
                      (java.lang.AssertionError.
                        (str
                          "Assert failed: "
                          "t must be raw t or txid"
                          "\n"
                          (pr-str
                            (clojure.core/list
                              'or
                              (clojure.core/list 'zero? 'tpart)
                              (clojure.core/list '= 'PART_TX 'tpart)))))))
                  (long (datomic.db/eid->eidx (unchecked-long ^java.lang.Number t_or_date)))))]
        (long
          (datomic.db/make-eid
            (unchecked-long ^java.lang.Number partition)
            (unchecked-long ^java.lang.Number t))))))
  (defn invoke ([db eid_or_ident & args] (apply (.getFn ^datomic.db.IDb db eid_or_ident) args)))
  (defn datoms-conflict
    ([db d1 d2]
      (error/argd
        :db.error/datoms-conflict
        "Two datoms in the same transaction conflict"
        {:d1 (datomic.db/datom-error-desc db d1), :d2 (datomic.db/datom-error-desc db d2)})))
  (reset-meta!
    #'datoms-conflict
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'd1 'd2]), :column 1}
      :name
      'datoms-conflict
      :ns
      *ns*))
  (defn create-deduper
    ([] (let [dset (java.util.HashSet.)] (fn fn__13270 ([d] (.add ^java.util.HashSet dset d))))))
  (reset-meta!
    #'create-deduper
    (assoc
      {:private true, :arglists (clojure.core/list []), :column 1}
      :name
      'create-deduper
      :ns
      *ns*))
  (defn create-schema-validator
    ([db]
      (fn fn__13273
        ([d]
          (if (= (datomic.db/get-part d) 0)
            (if (= (.getT ^datomic.impl.db.IDatum d) 0)
              true
              (let [a (.getA ^datomic.impl.db.IDatum d)]
                (if (or (= (long a) 13) (= (long a) 19))
                  false
                  (let [temp__5455__auto__ (datomic.db/dget
                                             (datomic.db/find-eavt
                                               db
                                               (long (.getE ^datomic.impl.db.IDatum d))
                                               (java.lang.Integer/valueOf
                                                 (int (.getA ^datomic.impl.db.IDatum d)))))]
                    (if temp__5455__auto__
                      (let [existing temp__5455__auto__]
                        (if (and
                              (zero?
                                (common/compare
                                  (.getV ^datomic.impl.db.IDatum d)
                                  (.getV ^datomic.impl.db.IDatum existing)))
                              (=
                                (boolean (.isAssertion ^datomic.impl.db.IDatum d))
                                (boolean (.isAssertion ^datomic.impl.db.IDatum existing))))
                          false
                          (if (and
                                (datomic.db/system-schema-datom? db existing)
                                (or
                                  (=
                                    (.-cardinality
                                      (datomic.db/attribute
                                        db
                                        (java.lang.Integer/valueOf (int a))))
                                    35)
                                  (not (.isAssertion ^datomic.impl.db.IDatum d))))
                            (let [m (datomic.db/datom-error-desc db d)]
                              (error/arg
                                :db.error/datom-cannot-be-altered
                                (str "Boot datoms cannot be altered: " m)
                                {:datom m}))
                            true)))
                      true)))))
            true)))))
  (reset-meta!
    #'create-schema-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'create-schema-validator
      :ns
      *ns*))
  (deftype
    AVof
    [d]
    java.lang.Object
    (^int hashCode [this] (bit-xor (hash (.a ^datomic.Datom d)) (hash (.v ^datomic.Datom d))))
    (^boolean equals
      [this other]
      (let [o (.-d ^AVof other)]
        (and
          (= (.a ^datomic.Datom d) (.a ^datomic.Datom o))
          (zero? (common/compare (.v ^datomic.Datom d) (.v ^datomic.Datom o)))))))
  (clojure.core/import 'datomic.db.AVof)
  (defn ->AVof ([d] (datomic.db.AVof. d)))
  (deftype
    EAVof
    [d]
    java.lang.Object
    (^int hashCode
      [this]
      (bit-xor
        (bit-xor (hash (.e ^datomic.Datom d)) (hash (.a ^datomic.Datom d)))
        (hash (.v ^datomic.Datom d))))
    (^boolean equals
      [this other]
      (let [o (.-d ^EAVof other)]
        (and
          (= (.e ^datomic.Datom d) (.e ^datomic.Datom o))
          (= (.a ^datomic.Datom d) (.a ^datomic.Datom o))
          (zero? (common/compare (.v ^datomic.Datom d) (.v ^datomic.Datom o)))))))
  (clojure.core/import 'datomic.db.EAVof)
  (defn ->EAVof ([d] (datomic.db.EAVof. d)))
  (deftype
    EAOpof
    [d]
    java.lang.Object
    (^int hashCode
      [this]
      (bit-xor
        (bit-xor (hash (.e ^datomic.Datom d)) (hash (.a ^datomic.Datom d)))
        (hash (.added ^datomic.Datom d))))
    (^boolean equals
      [this other]
      (let [o (.-d ^EAOpof other)]
        (and
          (= (.e ^datomic.Datom d) (.e ^datomic.Datom o))
          (= (.a ^datomic.Datom d) (.a ^datomic.Datom o))
          (= (boolean (.added ^datomic.Datom d)) (boolean (.added ^datomic.Datom o)))))))
  (clojure.core/import 'datomic.db.EAOpof)
  (defn ->EAOpof ([d] (datomic.db.EAOpof. d)))
  (defn create-op-validator
    ([db]
      (let [eavmap (java.util.HashMap.)]
        (fn fn__13301
          ([d]
            (let [v (.put ^java.util.HashMap eavmap (datomic.db.EAVof. d) d)]
              (or (nil? v) (datomic.db/datoms-conflict db v d))))))))
  (reset-meta!
    #'create-op-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'create-op-validator
      :ns
      *ns*))
  (defn create-card-one-validator
    ([db]
      (let [eaomap (java.util.HashMap.)]
        (fn fn__13305
          ([d]
            (if (= 35 (.-cardinality (datomic.db/attribute db (.a ^datomic.Datom d))))
              (let [v (.put ^java.util.HashMap eaomap (datomic.db.EAOpof. d) d)]
                (or (nil? v) (datomic.db/datoms-conflict db v d)))
              true))))))
  (reset-meta!
    #'create-card-one-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'create-card-one-validator
      :ns
      *ns*))
  (defn create-unique-value-validator
    ([db]
      (let [avmap (java.util.HashMap.)]
        (fn fn__13309
          ([d]
            (if (and
                  (.added ^datomic.Datom d)
                  (.-unique (datomic.db/attribute db (.a ^datomic.Datom d))))
              (let [v (.put ^java.util.HashMap avmap (datomic.db.AVof. d) d)]
                (or (nil? v) (datomic.db/datoms-conflict db v d)))
              true))))))
  (reset-meta!
    #'create-unique-value-validator
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'create-unique-value-validator
      :ns
      *ns*))
  (defn attrs-missing-hooks
    ([db datoms]
      (let [attr_hook_attr? (datomic.db/attr-hook-attr-ids db)
            eids (transient #{})
            G__13318 datoms
            vec__13319 G__13318
            seq__13320 (seq vec__13319)
            first__13321 (first seq__13320)
            seq__13320 (next seq__13320)
            map__13322 first__13321
            map__13322 (if (seq? map__13322)
                         (clojure.lang.PersistentHashMap/create (seq map__13322))
                         map__13322)
            d map__13322
            a (get map__13322 :a)
            more seq__13320]
        (loop [eids eids G__13318 G__13318]
          (let [eids eids
                vec__13323 G__13318
                seq__13324 (seq vec__13323)
                first__13325 (first seq__13324)
                seq__13324 (next seq__13324)
                map__13326 first__13325
                map__13326 (if (seq? map__13326)
                             (clojure.lang.PersistentHashMap/create (seq map__13326))
                             map__13326)
                d map__13326
                a (get map__13326 :a)
                more seq__13324]
            (if d
              (cond
                (^clojure.lang.IFn attr_hook_attr? a) (recur (conj! eids (:e d)) more)
                (and (datomic.db/schema-hook-attrs a) (:added d)) (recur (disj! eids (:v d)) more)
                :default (do (recur eids more)))
              (let [eids (persistent! eids)] (when-not (empty? eids) eids))))))))
  (reset-meta!
    #'attrs-missing-hooks
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'datoms]), :column 1}
      :name
      'attrs-missing-hooks
      :ns
      *ns*))
  (defn new-ents-not-installed
    ([db datoms]
      (let [p (count (:elements db))
            needs_install? (fn needs_install_QMARK_
                             ([^long e]
                               (and
                                 (zero? (datomic.db/eid->part e))
                                 (<= p (datomic.db/eid->eidx e)))))
            eids (transient #{})
            G__13335 datoms
            vec__13336 G__13335
            seq__13337 (seq vec__13336)
            first__13338 (first seq__13337)
            seq__13337 (next seq__13337)
            d first__13338
            more seq__13337]
        (loop [eids eids G__13335 G__13335]
          (let [eids eids
                vec__13339 G__13335
                seq__13340 (seq vec__13339)
                first__13341 (first seq__13340)
                seq__13340 (next seq__13340)
                d first__13341
                more seq__13340]
            (if d
              (let [e (.getE ^datomic.impl.db.IDatum d)]
                (cond
                  (^clojure.lang.IFn needs_install? (long e)) (recur (conj! eids (long e)) more)
                  (datomic.db/install-attrs
                    (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))) (recur
                                                                                           (disj!
                                                                                             eids
                                                                                             (.getV
                                                                                               ^datomic.impl.db.IDatum d))
                                                                                           more)
                  :default (do (recur eids more))))
              (let [eids (persistent! eids)] (when-not (empty? eids) eids))))))))
  (reset-meta!
    #'new-ents-not-installed
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'datoms]), :column 1}
      :name
      'new-ents-not-installed
      :ns
      *ns*))
  (def on-prem-only-aids #{46 15 48 44 51 17 47 11 9 14 16 18 52 8 49})
  (reset-meta!
    #'on-prem-only-aids
    (assoc {:private true, :column 1} :name 'on-prem-only-aids :ns *ns*))
  (def on-prem-only-vts #{27})
  (reset-meta!
    #'on-prem-only-vts
    (assoc {:private true, :column 1} :name 'on-prem-only-vts :ns *ns*))
  (defn cloud-compat-validator
    ([db d]
      (let [a (.a ^datomic.Datom d)
            temp__5455__auto__ (or
                                 (datomic.db/on-prem-only-aids a)
                                 (and
                                   (= 40 a)
                                   (datomic.db/on-prem-only-vts (.v ^datomic.Datom d))))]
        (if temp__5455__auto__
          (let [problem temp__5455__auto__]
            (error/arg
              :db.error/not-supported-in-cloud
              (str (.ident ^datomic.Database db problem) " is not supported in Datomic Cloud.")))
          true))))
  (reset-meta!
    #'cloud-compat-validator
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [(.withMeta 'db {:tag 'Database}) (.withMeta 'd {:tag 'Datom})]),
       :column 1}
      :name
      'cloud-compat-validator
      :ns
      *ns*))
  (defn create-cloud-compat-validator
    ([db]
      (if (:cloud-compat db)
        (partial datomic.db/cloud-compat-validator db)
        (fn fn__13347 ([_] true)))))
  (defn filter-assess-tx-datoms
    ([db check_installs? datoms]
      (let [p0 (datomic.db/create-schema-validator db)
            p1 (datomic.db/create-deduper)
            p2 (datomic.db/create-op-validator db)
            p3 (datomic.db/create-card-one-validator db)
            p4 (datomic.db/create-unique-value-validator db)
            p5 (datomic.db/create-cloud-compat-validator db)
            result (filterv
                     (fn fn__13351
                       ([p1__13350#]
                         (and
                           (^clojure.lang.IFn p0 p1__13350#)
                           (^clojure.lang.IFn p1 p1__13350#)
                           (^clojure.lang.IFn p2 p1__13350#)
                           (^clojure.lang.IFn p3 p1__13350#)
                           (^clojure.lang.IFn p4 p1__13350#)
                           (^clojure.lang.IFn p5 p1__13350#))))
                     datoms)
            result (reduce
                     (fn fn__13358
                       ([result aid]
                         (let [hook_attr (if (datomic.db/attribute db aid) 19 13)]
                           (conj
                             result
                             (datomic.db/asserting-datum
                               0
                               hook_attr
                               aid
                               (.getT (first result)))))))
                     result
                     (sort (datomic.db/attrs-missing-hooks db result)))]
        (when check_installs?
          (let [temp__5457__auto__ (datomic.db/new-ents-not-installed db result)]
            (when temp__5457__auto__
              (let [es temp__5457__auto__]
                (error/argd
                  :db.error/schema-without-install
                  "Only schema components can be installed in partition :db.part/db"
                  {:datoms
                   (mapv
                     (partial datomic.db/datom-error-desc db)
                     (filterv
                       (fn fn__13361
                         ([p__13360]
                           (let [map__13362 p__13360
                                 map__13362 (if (seq? map__13362)
                                              (clojure.lang.PersistentHashMap/create
                                                (seq map__13362))
                                              map__13362)
                                 e (get map__13362 :e)]
                             (contains? es e))))
                       result))})))))
        result)))
  (declare datomic.db/->MemLog)
  (declare datomic.db/map->MemLog)
  (defrecord MemLog [txes])
  (clojure.core/import 'datomic.db.MemLog)
  (defn ->MemLog ([txes] (datomic.db.MemLog. txes)))
  (defn map->MemLog
    ([m__7585__auto__]
      (MemLog/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (defn memlog ([] (datomic.db/->MemLog [])))
  (defn add-log
    ([memlog t data] (if (<= 1000 t) (update-in memlog [:txes] conj {:t t, :data data}) memlog)))
  (defn trim-log
    ([memlog t]
      (update-in
        memlog
        [:txes]
        (fn fn__13390
          ([p1__13389#]
            (let [idx (java.util.Collections/binarySearch
                        ^java.util.List p1__13389#
                        {:t t}
                        (common/key-comparator :t))]
              (into
                []
                (subvec p1__13389# (if (< idx 0) (long (- (inc idx))) (long (inc idx)))))))))))
  (defn retracts?
    ([d1 d2]
      (and
        (not (.added ^datomic.Datom d1))
        (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
        (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
        (zero? (common/compare (.v ^datomic.Datom d1) (.v ^datomic.Datom d2)))
        (.added ^datomic.Datom d2)
        (< (.tx ^datomic.Datom d2) (.tx ^datomic.Datom d1)))))
  (reset-meta!
    #'retracts?
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'd1 {:tag 'Datom}) (.withMeta 'd2 {:tag 'Datom})]),
       :column 1}
      :name
      'retracts?
      :ns
      *ns*))
  (defn updates-v?
    ([d1 d2]
      (and
        (.added ^datomic.Datom d1)
        (= (.e ^datomic.Datom d1) (.e ^datomic.Datom d2))
        (= (.a ^datomic.Datom d1) (.a ^datomic.Datom d2))
        (not (zero? (common/compare (.v ^datomic.Datom d1) (.v ^datomic.Datom d2))))
        (.added ^datomic.Datom d2)
        (< (.tx ^datomic.Datom d2) (.tx ^datomic.Datom d1)))))
  (reset-meta!
    #'updates-v?
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'd1 {:tag 'Datom}) (.withMeta 'd2 {:tag 'Datom})]),
       :column 1}
      :name
      'updates-v?
      :ns
      *ns*))
  (defn user-proc? ([procid] (var? procid)))
  (def tx-stat-keys
   [:res-ct
    :res-pf-ms
    :res-tx-ms
    :comp-ct
    :comp-pf-ms
    :comp-tx-ms
    :dedup-ct
    :dedup-pf-ms
    :dedup-tx-ms
    :ucheck-ct
    :ucheck-pf-ms
    :ucheck-tx-ms
    :dup-datoms
    :considered-datoms
    :tx-fn-ms])
  (defn empty-tx-stat-registers
    ([]
      (persistent!
        (reduce
          (fn fn__13406 ([m n] (assoc! m n (java.util.concurrent.atomic.LongAdder.))))
          (transient {})
          datomic.db/tx-stat-keys))))
  (def timing-key?
   #{:dedup-tx-ms :tx-fn-ms :dedup-pf-ms :comp-tx-ms :res-pf-ms :ucheck-tx-ms :ucheck-pf-ms
     :comp-pf-ms :res-tx-ms})
  (defn summarize-tx-stats
    ([tx_stat_registers]
      (persistent!
        (reduce-kv
          (fn fn__13409
            ([m k adder]
              (assoc!
                m
                k
                (let [G__13410 (.sum ^java.util.concurrent.atomic.LongAdder adder)]
                  (if (datomic.db/timing-key? k)
                    (java.lang.Double/valueOf (double (monitor/ns->ms G__13410)))
                    (long G__13410))))))
          (transient {})
          tx_stat_registers))))
  (defn long-add! ([a ^long v] (.add ^java.util.concurrent.atomic.LongAdder a (long v)) nil))
  (defn timed-tx-stat
    ([&form &env adder expr]
      (seq
        (concat
          (clojure.core/list 'clojure.core/let)
          (clojure.core/list
            (apply
              vector
              (seq
                (concat
                  (clojure.core/list 'start__13414__auto__)
                  (clojure.core/list (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                  (clojure.core/list 'ret__13415__auto__)
                  (clojure.core/list expr)))))
          (clojure.core/list
            (seq
              (concat
                (clojure.core/list 'datomic.db/long-add!)
                (clojure.core/list adder)
                (clojure.core/list
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/unchecked-subtract)
                      (clojure.core/list
                        (seq (concat (clojure.core/list 'java.lang.System/nanoTime))))
                      (clojure.core/list 'start__13414__auto__)))))))
          (clojure.core/list 'ret__13415__auto__)))))
  (.setMacro #'datomic.db/timed-tx-stat)
  (declare datomic.db/reverse-key?)
  (declare datomic.db/->Db)
  (declare datomic.db/map->Db)
  (defrecord
    Db
    [id
     ^IndexSet memidx
     ^IndexSet indexing
     ^IndexSet mid-index
     ^IndexSet index
     ^IndexSet history
     memlog
     ^long basisT
     ^long nextT
     ^long indexBasisT
     indexingNextT
     elements
     keys
     ids
     index-root-id
     index-rev
     asOfT
     sinceT
     raw
     filt]
    datomic.db.IDb
    datomic.db.IDbImpl
    datomic.Database
    (acceptDataCheck
      [this indata check]
      (let [basis nextT d (first indata)]
        (when (and check d (not= (long (.getT ^datomic.impl.db.IDatum d)) (long nextT)))
          (throw
            (java.lang.IllegalStateException.
              (str
                "Gap in data - expected: "
                (long nextT)
                " got: "
                (long (.getT ^datomic.impl.db.IDatum d))))))
        (loop [db this
               next_t nextT
               eavt (.-eavt ^datomic.db.IndexSet memidx)
               avet (.-avet ^datomic.db.IndexSet memidx)
               aevt (.-aevt ^datomic.db.IndexSet memidx)
               raet (.-raet ^datomic.db.IndexSet memidx)
               data (seq indata)]
          (if data
            (let [d (first data)
                  eidx (.eidx ^datomic.db.IDatumImpl d)
                  next_t (if (<= next_t eidx) (inc eidx) next_t)
                  attrid (.getA ^datomic.impl.db.IDatum d)
                  attr (.elementAt ^datomic.db.IDbImpl db (java.lang.Integer/valueOf (int attrid)))
                  eavt (conj eavt d)
                  avet (if (and attr (.-needsAVET ^datomic.db.Attribute attr)) (conj avet d) avet)
                  aevt (conj aevt d)
                  raet (if (and attr (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
                         (conj raet d)
                         raet)
                  temp__5455__auto__ (and
                                       (.isAssertion ^datomic.impl.db.IDatum d)
                                       (datomic.db/get-hook
                                         (java.lang.Integer/valueOf (int attrid))))]
              (if temp__5455__auto__
                (let [hook temp__5455__auto__
                      dbval (assoc
                              db
                              :memidx
                              (datomic.db.IndexSet. eavt avet aevt raet nil)
                              :basisT
                              (long basis)
                              :nextT
                              (long next_t))
                      dbval (^clojure.lang.IFn hook this dbval d check)
                      avet (:avet (:memidx dbval))]
                  (recur dbval next_t eavt avet aevt raet (next data)))
                (recur db next_t eavt avet aevt raet (next data))))
            (assoc
              db
              :memidx
              (datomic.db.IndexSet. eavt avet aevt raet (.-fulltext ^datomic.db.IndexSet memidx))
              :memlog
              (datomic.db/add-log memlog (long basis) (vec indata))
              :basisT
              (if check (long basis) (long (.getT ^datomic.impl.db.IDatum d)))
              :nextT
              (long next_t))))))
    (acceptData [this indata] (.acceptDataCheck this indata true))
    (addData
      [this indata ^java.util.ArrayList added tx_stat_registers]
      (let [basis nextT
            map__13474 tx_stat_registers
            map__13474 (if (seq? map__13474)
                         (clojure.lang.PersistentHashMap/create (seq map__13474))
                         map__13474)
            dedup_tx_ms (get map__13474 :dedup-tx-ms)
            dedup_ct (get map__13474 :dedup-ct)
            dup_datoms (get map__13474 :dup-datoms)
            ucheck_ct (get map__13474 :ucheck-ct)
            ucheck_tx_ms (get map__13474 :ucheck-tx-ms)]
        (loop [db this
               next_t (inc basis)
               eavt (.-eavt ^datomic.db.IndexSet memidx)
               avet (.-avet ^datomic.db.IndexSet memidx)
               aevt (.-aevt ^datomic.db.IndexSet memidx)
               raet (.-raet ^datomic.db.IndexSet memidx)
               data (seq indata)]
          (if data
            (let [d (first data)
                  eidx (.eidx ^datomic.db.IDatumImpl d)
                  next_t (if (<= next_t eidx) (inc eidx) next_t)
                  attrid (.getA ^datomic.impl.db.IDatum d)
                  v (.getV ^datomic.impl.db.IDatum d)
                  attr (.elementAt ^datomic.db.IDbImpl db (java.lang.Integer/valueOf (int attrid)))
                  card_one? (= (.-cardinality ^datomic.db.Attribute attr) 35)
                  ed (and
                       (or (zero? basis) (< eidx basis))
                       (do
                         (datomic.db/long-add! dedup_ct 1)
                         (let [start__13414__auto__ (java.lang.System/nanoTime)
                               ret__13415__auto__ (datomic.db/dget
                                                    (datomic.db/find-aevt
                                                      db
                                                      (java.lang.Integer/valueOf
                                                        (int (.getA ^datomic.impl.db.IDatum d)))
                                                      (long (.getE ^datomic.impl.db.IDatum d))
                                                      (when-not card_one?
                                                        (.getV ^datomic.impl.db.IDatum d))))]
                           (datomic.db/long-add!
                             dedup_tx_ms
                             (- (java.lang.System/nanoTime) start__13414__auto__))
                           ret__13415__auto__)))
                  there? (and
                           ed
                           (common/equals-with-strict-scale v (.getV ^datomic.impl.db.IDatum ed)))
                  redundant? (if (.isAssertion ^datomic.impl.db.IDatum d)
                               (and
                                 there?
                                 (not=
                                   (java.lang.Integer/valueOf
                                     (int (.getA ^datomic.impl.db.IDatum d)))
                                   19))
                               (not there?))]
              (when (and
                      (not redundant?)
                      (.-unique ^datomic.db.Attribute attr)
                      (.isAssertion ^datomic.impl.db.IDatum d))
                (datomic.db/long-add! ucheck_ct 1)
                (let [temp__5457__auto__ (let [start__13414__auto__ (java.lang.System/nanoTime)
                                               ret__13415__auto__ (datomic.db/dget
                                                                    (datomic.db/find-avet
                                                                      db
                                                                      (java.lang.Integer/valueOf
                                                                        (int attrid))
                                                                      v))]
                                           (datomic.db/long-add!
                                             ucheck_tx_ms
                                             (- (java.lang.System/nanoTime) start__13414__auto__))
                                           ret__13415__auto__)]
                  (when temp__5457__auto__
                    (let [it temp__5457__auto__]
                      (when-not (some
                                  (fn fn__13475
                                    ([p1__13419#]
                                      (or
                                        (datomic.db/retracts? p1__13419# it)
                                        (and card_one? (datomic.db/updates-v? p1__13419# it)))))
                                  indata)
                        (error/state
                          :db.error/unique-conflict
                          (str
                            "Unique conflict: "
                            (.kw ^datomic.db.Attribute attr)
                            ", value: "
                            v
                            " already held by: "
                            (long (.getE ^datomic.impl.db.IDatum it))
                            " asserted for: "
                            (long (.getE ^datomic.impl.db.IDatum d)))))))))
              (if redundant?
                (let [temp__5455__auto__ (and
                                           (.isAssertion ^datomic.impl.db.IDatum d)
                                           (datomic.db/get-hook
                                             (java.lang.Integer/valueOf (int attrid))))]
                  (if temp__5455__auto__
                    (let [hook temp__5455__auto__
                          dbval (assoc
                                  db
                                  :memidx
                                  (datomic.db.IndexSet. eavt avet aevt raet nil)
                                  :basisT
                                  (long basis)
                                  :nextT
                                  (long next_t))
                          avet (:avet (:memidx dbval))]
                      (recur
                        (^clojure.lang.IFn hook this dbval d true)
                        next_t
                        eavt
                        avet
                        aevt
                        raet
                        (next data)))
                    (do
                      (datomic.db/long-add! dup_datoms 1)
                      (recur db next_t eavt avet aevt raet (next data)))))
                (let [eavt (conj eavt d)
                      avet (if (.-needsAVET ^datomic.db.Attribute attr) (conj avet d) avet)
                      aevt (conj aevt d)
                      raet (if (= 20 (.-vtypeid ^datomic.db.Attribute attr)) (conj raet d) raet)
                      tomb (when (and (.isAssertion ^datomic.impl.db.IDatum d) ed card_one?)
                             (datomic.db/retracting-datum
                               (.getE ^datomic.impl.db.IDatum d)
                               (.getA ^datomic.impl.db.IDatum d)
                               (.getV ^datomic.impl.db.IDatum ed)
                               (.getT ^datomic.impl.db.IDatum d)))
                      eavt (if tomb (conj eavt tomb) eavt)
                      avet (if (and tomb (.-needsAVET ^datomic.db.Attribute attr))
                             (conj avet tomb)
                             avet)
                      aevt (if tomb (conj aevt tomb) aevt)
                      raet (if (and tomb (= 20 (.-vtypeid ^datomic.db.Attribute attr)))
                             (conj raet tomb)
                             raet)]
                  (.add ^java.util.ArrayList added d)
                  (when tomb (.add ^java.util.ArrayList added tomb))
                  (let [temp__5455__auto__ (and
                                             (.isAssertion ^datomic.impl.db.IDatum d)
                                             (datomic.db/get-hook
                                               (java.lang.Integer/valueOf (int attrid))))]
                    (if temp__5455__auto__
                      (let [hook temp__5455__auto__
                            dbval (assoc
                                    db
                                    :memidx
                                    (datomic.db.IndexSet. eavt avet aevt raet nil)
                                    :basisT
                                    (long basis)
                                    :nextT
                                    (long next_t))
                            dbval (^clojure.lang.IFn hook this dbval d true)
                            avet (:avet (:memidx dbval))]
                        (recur dbval next_t eavt avet aevt raet (next data)))
                      (recur db next_t eavt avet aevt raet (next data)))))))
            (assoc
              db
              :memidx
              (datomic.db.IndexSet. eavt avet aevt raet (.-fulltext ^datomic.db.IndexSet memidx))
              :memlog
              (datomic.db/add-log memlog (long basis) (vec added))
              :basisT
              (long basis)
              :nextT
              (long next_t))))))
    (getRawId [this] id)
    (addElement
      [this ^datomic.db.IElementImpl e]
      (let [id (.id ^datomic.db.IElementImpl e)
            _ (let [temp__5457__auto__ (get elements id)]
                (when temp__5457__auto__
                  (let [current temp__5457__auto__]
                    (when (not= (class current) (class e))
                      (error/arg
                        :db.error/invalid-element-change
                        (str
                          (datomic.db/entity-error-desc this id)
                          " cannot be both "
                          (.getSimpleName (class current))
                          " and "
                          (.getSimpleName (class e))))))))
            new_elements (assoc (datomic.db/growvec elements id) id e)]
        (assoc this :elements new_elements)))
    (growElements
      [this i]
      (if (<= i 1048576)
        (assoc this :elements (datomic.db/growvec elements i))
        (error/arg
          :db.error/schema-count-exceeded
          (str "Schema can contain at most " 1048576 " elements"))))
    (addKeyword
      [this kw id]
      (cond->
        (assoc this :keys (assoc keys id kw) :ids (assoc ids (datomic.db/require-kw kw) id))
        (datomic.db/reverse-key? kw)
        (update :_keys (fnil conj #{}) kw)))
    (elementAt [this i] (nth elements (unchecked-int ^java.lang.Number i) nil))
    (eq
      [this other]
      (boolean
        (and
          (instance? Db other)
          (= id (.-id ^Db other))
          (= (long nextT) (long (.nextT ^datomic.Database other)))
          (= asOfT (.asOfT ^datomic.Database other))
          (= sinceT (.sinceT ^datomic.Database other)))))
    (^clojure.lang.IFn getFn
      [this x]
      (if (datomic.db/user-proc? x)
        x
        (let [fnid (datomic.db/require-id this x)
              temp__5455__auto__ (let [elem (.elementAt this (long fnid))]
                                   (when (instance? datomic.db.Function elem) elem))]
          (if temp__5455__auto__
            (let [func temp__5455__auto__] (.-f ^datomic.db.Function func))
            (let [temp__5455__auto__ (get (.entity this (long fnid)) :db/query)]
              (if temp__5455__auto__
                (let [q temp__5455__auto__] (Circular/constructFn q))
                (let [f (get (.entity this (long fnid)) :db/fn)]
                  (if (and f (instance? clojure.lang.IFn f))
                    f
                    (error/arg
                      :db.error/not-a-data-function
                      (str "Not a data function: " x))))))))))
    (^datomic.iter.Iter seekRAET
      [this ^datomic.impl.db.IDatum d]
      (iter/merge-iters
        datomic.db/raet-cmp
        (datomic.btset/seek (.-raet ^datomic.db.IndexSet memidx) d)
        (datomic.btset/seek (and indexing (.-raet ^datomic.db.IndexSet indexing)) d)
        (datomic.btset/seek (and mid-index (.-raet ^datomic.db.IndexSet mid-index)) d)
        (datomic.btset/seek (and index (.-raet ^datomic.db.IndexSet index)) d)
        (when (or raw (and asOfT (< asOfT indexBasisT)))
          (datomic.btset/seek (and history (.-raet ^datomic.db.IndexSet history)) d))))
    (^datomic.iter.Iter seekAEVT
      [this ^datomic.impl.db.IDatum d]
      (iter/merge-iters
        datomic.db/aevt-cmp
        (datomic.btset/seek (.-aevt ^datomic.db.IndexSet memidx) d)
        (datomic.btset/seek (and indexing (.-aevt ^datomic.db.IndexSet indexing)) d)
        (datomic.btset/seek (and mid-index (.-aevt ^datomic.db.IndexSet mid-index)) d)
        (datomic.btset/seek (and index (.-aevt ^datomic.db.IndexSet index)) d)
        (when (or raw (and asOfT (< asOfT indexBasisT)))
          (datomic.btset/seek (and history (.-aevt ^datomic.db.IndexSet history)) d))))
    (^datomic.iter.Iter seekAVET
      [this ^datomic.impl.db.IDatum d]
      (iter/merge-iters
        datomic.db/avet-cmp
        (datomic.btset/seek (.-avet ^datomic.db.IndexSet memidx) d)
        (datomic.btset/seek (and indexing (.-avet ^datomic.db.IndexSet indexing)) d)
        (datomic.btset/seek (and mid-index (.-avet ^datomic.db.IndexSet mid-index)) d)
        (datomic.btset/seek (and index (.-avet ^datomic.db.IndexSet index)) d)
        (when (or raw (and asOfT (< asOfT indexBasisT)))
          (datomic.btset/seek (and history (.-avet ^datomic.db.IndexSet history)) d))))
    (^datomic.iter.Iter seekEAVT
      [this ^datomic.impl.db.IDatum d]
      (iter/merge-iters
        datomic.db/eavt-cmp
        (datomic.btset/seek (.-eavt ^datomic.db.IndexSet memidx) d)
        (datomic.btset/seek (and indexing (.-eavt ^datomic.db.IndexSet indexing)) d)
        (datomic.btset/seek (and mid-index (.-eavt ^datomic.db.IndexSet mid-index)) d)
        (datomic.btset/seek (and index (.-eavt ^datomic.db.IndexSet index)) d)
        (when (or raw (and asOfT (< asOfT indexBasisT)))
          (datomic.btset/seek (and history (.-eavt ^datomic.db.IndexSet history)) d))))
    (getFilter [this] filt)
    (getRaw [this] raw)
    (getSinceT [this] sinceT)
    (getAsOfT [this] asOfT)
    (idOf [this kw] (get ids (datomic.db/to-kw kw)))
    (keywordOf [this id] (^clojure.lang.IFn keys id))
    (getNextT [this] (long nextT))
    (^java.util.Map dbStats [this] (Circular/dbStats this))
    (^datomic.Database filter [this ^datomic.Database$Predicate pred] (.filter this pred))
    (^datomic.Database filter
      [this pred]
      (let [pred (if (instance? datomic.Database$Predicate pred)
                   (fn fn__13468
                     ([db d] (.apply ^datomic.Database$Predicate pred ^datomic.Database db d)))
                   pred)]
        (assoc
          this
          :filt
          (if filt
            (fn fn__13470
              ([p1__13417# p2__13418#]
                (and
                  (^clojure.lang.IFn filt p1__13417# p2__13418#)
                  (^clojure.lang.IFn pred p1__13417# p2__13418#))))
            pred))))
    (^datomic.Database history [this] (assoc this :raw true))
    (^java.lang.Iterable indexRange
      [this attrid start end]
      (iter/iterable (fn fn__13466 ([] (datomic.db/attr-index-range this attrid start end)))))
    (^java.lang.Iterable seekDatoms
      [this index ^"[Ljava.lang.Object;" components]
      (datomic.db/seek-datoms this index components))
    (^java.lang.Iterable datoms
      [this index ^"[Ljava.lang.Object;" components]
      (datomic.db/datoms this index components))
    (^java.util.Map with
      [this ^java.util.List txdata opts]
      (if raw
        (do (throw (java.lang.IllegalStateException. "Can't get history with txdata")) nil)
        (datomic.db/with-tx+opts this txdata opts)))
    (^java.util.Map with [this ^java.util.List txdata] (.with this ^java.util.List txdata nil))
    (invoke [this keyOrId ^"[Ljava.lang.Object;" args] (apply datomic.db/invoke this keyOrId args))
    (entidAt [this part t_or_date] (datomic.db/entid-at this part t_or_date))
    (entid [this keyOrId] (datomic.db/resolve-id this keyOrId))
    (ident [this idOrKey] (datomic.db/resolve-kw this idOrKey))
    (^datomic.Attribute attribute [this aid] (datomic.db/attr-info this aid))
    (pullMany
      [this selector ^java.util.List eids options]
      (Circular/pullMany this selector eids options))
    (^java.util.List pullMany
      [this selector ^java.util.List eids]
      (Circular/pullMany this selector eids))
    (^java.util.stream.Stream indexPull [this options] (Circular/indexPull this options))
    (pull [this selector eid options] (Circular/pull this selector eid options))
    (^java.util.Map pull [this selector eid] (Circular/pull this selector eid nil))
    (^datomic.Entity entity
      [this eid]
      (if raw
        (do (throw (java.lang.IllegalStateException. "Can't create entity from history")) nil)
        (let [temp__5457__auto__ (datomic.db/resolve-id this eid)]
          (when temp__5457__auto__ (let [id temp__5457__auto__] (Circular/emap this id))))))
    (^datomic.Database since [this t] (assoc this :sinceT (datomic.db/as-of-t this t)))
    (^datomic.Database asOf [this t] (assoc this :asOfT (datomic.db/as-of-t this t)))
    (^boolean isFiltered [this] (boolean filt))
    (^boolean isHistory [this] (boolean raw))
    (^java.lang.Long sinceT [this] ^java.lang.Long sinceT)
    (^java.lang.Long asOfT [this] ^java.lang.Long asOfT)
    (^long nextT [this] nextT)
    (^long basisT [this] basisT)
    (^java.lang.String id [this] (str id)))
  (clojure.core/import 'datomic.db.Db)
  (defn ->Db
    ([id
      memidx
      indexing
      mid_index
      index
      history
      memlog
      basisT
      nextT
      indexBasisT
      indexingNextT
      elements
      keys
      ids
      index_root_id
      index_rev
      asOfT
      sinceT
      raw
      filt]
      (datomic.db.Db.
        id
        memidx
        indexing
        mid_index
        index
        history
        memlog
        (unchecked-long ^java.lang.Number basisT)
        (unchecked-long ^java.lang.Number nextT)
        (unchecked-long ^java.lang.Number indexBasisT)
        indexingNextT
        elements
        keys
        ids
        index_root_id
        index_rev
        asOfT
        sinceT
        raw
        filt)))
  (defn map->Db
    ([m__7585__auto__]
      (Db/create
        (if (instance? clojure.lang.MapEquivalence m__7585__auto__)
          m__7585__auto__
          (into {} m__7585__auto__)))))
  (def bootstrap-data-upgrades
   [:db/excise
    [[:db/add 15 :db/ident :db/excise]
     [:db/add 15 :db/valueType :db.type/ref]
     [:db/add 15 :db/cardinality :db.cardinality/one]
     [:db/add :db.part/db :db.install/attribute 15]
     [:db/add 16 :db/ident :db.excise/attrs]
     [:db/add 16 :db/valueType :db.type/ref]
     [:db/add 16 :db/cardinality :db.cardinality/many]
     [:db/add :db.part/db :db.install/attribute 16]
     [:db/add 17 :db/ident :db.excise/beforeT]
     [:db/add 17 :db/valueType :db.type/long]
     [:db/add 17 :db/cardinality :db.cardinality/one]
     [:db/add :db.part/db :db.install/attribute 17]
     [:db/add 18 :db/ident :db.excise/before]
     [:db/add 18 :db/valueType :db.type/instant]
     [:db/add 18 :db/cardinality :db.cardinality/one]
     [:db/add :db.part/db :db.install/attribute 18]]
    :db.part/user
    [[:db/add :db.part/db :db.install/partition :db.part/user]
     [:db/add :db.part/db :db.install/partition :db.part/tx]
     [:db/add (datomic.db/BOOT-IDS :db.bootstrap/part) :db/ident :db.bootstrap/part]]
    :db.alter/attribute
    [[:db/add 19 :db/ident :db.alter/attribute]
     [:db/add 19 :db/valueType :db.type/ref]
     [:db/add 19 :db/cardinality :db.cardinality/many]
     [:db/add :db.part/db :db.install/attribute 19]]
    :db/system-tx
    [[:db/add 7 :db/ident :db/system-tx]
     [:db/add 7 :db/valueType :db.type/keyword]
     [:db/add 7 :db/cardinality :db.cardinality/many]
     [:db/add :db.part/db :db.install/attribute 7]]
    :db.sys/reId
    [[:db/add 9 :db/ident :db.sys/reId]
     [:db/add 9 :db/valueType :db.type/ref]
     [:db/add 9 :db/cardinality :db.cardinality/one]]
    :db.sys/partiallyIndexed
    [[:db/add 8 :db/ident :db.sys/partiallyIndexed]
     [:db/add 8 :db/valueType :db.type/boolean]
     [:db/add 8 :db/cardinality :db.cardinality/one]]
    :db/cas
    [[:db/add :db.fn/cas :db/ident :db/cas]]
    :db/retractEntity
    [[:db/add :db.fn/retractEntity :db/ident :db/retractEntity]]])
  (defmethod
    pp/simple-dispatch
    datomic.db.Db
    fn__13569
    ([db]
      (pr
        (assoc
          (select-keys db [:id :basisT :indexBasisT :index-root-id :asOfT :sinceT :raw])
          :type
          'datomic.db.Db))))
  (defn assertion? ([d] (.isAssertion ^datomic.impl.db.IDatum d)))
  (reset-meta!
    #'assertion?
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'd {:tag 'IDatum})]), :column 1}
      :name
      'assertion?
      :ns
      *ns*))
  (defn ident-setting-datoms
    ([db index]
      (sort-by
        (fn fn__13574 ([p1__13573#] (long (.getTx ^datomic.impl.db.IDatum p1__13573#))))
        (iter/iter-seq
          (iter/take-while
            (fn fn__13576 ([p1__13572#] (= 10 (long (.getA ^datomic.impl.db.IDatum p1__13572#)))))
            (iter/filter
              datomic.db/assertion?
              (iter/merge-iters
                datomic.db/aevt-cmp
                (.seek (.-aevt ^datomic.db.IndexSet index) (datomic.db/datum db :a 10))
                (when (and (:mid-index db) (.-aevt (:mid-index db)))
                  (.seek (.-aevt (:mid-index db)) (datomic.db/datum db :a 10)))
                (when (and (:history db) (.-aevt (:history db)))
                  (.seek (.-aevt (:history db)) (datomic.db/datum db :a 10))))))))))
  (reset-meta!
    #'ident-setting-datoms
    (assoc
      {:private true,
       :arglists (clojure.core/list ['db (.withMeta 'index {:tag 'IndexSet})]),
       :column 1}
      :name
      'ident-setting-datoms
      :ns
      *ns*))
  (defn run-hooks
    ([db index]
      (let [fhooks (assoc (assoc datomic.db/hooks 19 datomic.db/install-attribute-hook) 10 nil)
            ret (reduce
                  (fn fn__13583 ([db datom] (datomic.db/key-hook nil db datom false)))
                  db
                  (datomic.db/ident-setting-datoms db index))
            ret (datomic.db/update-schema-level ret)
            ret (reduce
                  (fn fn__13586
                    ([db p__13585]
                      (let [vec__13587 p__13585
                            attrid (nth vec__13587 (unchecked-int 0) nil)
                            f (nth vec__13587 (unchecked-int 1) nil)]
                        (loop [db db
                               iter (iter/filter
                                      datomic.db/assertion?
                                      (iter/merge-iters
                                        datomic.db/aevt-cmp
                                        (.seek
                                          (.-aevt ^datomic.db.IndexSet index)
                                          (datomic.db/datum db :a attrid))
                                        (when (and (:mid-index db) (.-aevt (:mid-index db)))
                                          (.seek
                                            (.-aevt (:mid-index db))
                                            (datomic.db/datum db :a attrid)))))]
                          (if (and iter (= attrid (long (.getA (datomic.db/dget iter)))))
                            (recur
                              (^clojure.lang.IFn f nil db (datomic.db/dget iter) false)
                              (iter/inext iter))
                            db)))))
                  ret
                  (keep-indexed
                    (fn fn__13593
                      ([p1__13582# p2__13581#] (when p2__13581# [p1__13582# p2__13581#])))
                    fhooks))]
        ret)))
  (defn find-last-tx
    (^long [^long nextT index mid_index]
      (let [d (some
                identity
                (map
                  (fn fn__13597
                    ([t]
                      (let [eid (datomic.db/make-eid 3 (unchecked-long ^java.lang.Number t))
                            d (datomic.db/asserting-datum eid 50 nil eid)
                            G__13598 (iter/merge-iters
                                       datomic.db/eavt-cmp
                                       (datomic.btset/seek (.-eavt ^datomic.db.IndexSet index) d)
                                       (datomic.btset/seek
                                         (and mid_index (.-eavt ^datomic.db.IndexSet mid_index))
                                         d))]
                        (some->
                          (when-not (nil? G__13598)
                            (iter/take-while
                              (fn fn__13599
                                ([p1__13596#]
                                  (and
                                    (=
                                      (long eid)
                                      (long (.getE ^datomic.impl.db.IDatum p1__13596#)))
                                    (= 50 (long (.getA ^datomic.impl.db.IDatum p1__13596#)))
                                    (=
                                      (long eid)
                                      (long (.getTx ^datomic.impl.db.IDatum p1__13596#))))))
                              G__13598))
                          (.get)))))
                  (range (long (dec nextT)) 0 -1)))]
        (.getT ^datomic.impl.db.IDatum d))))
  (reset-meta!
    #'find-last-tx
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         (.withMeta
           [(.withMeta 'nextT {:tag 'long})
            (.withMeta 'index {:tag 'IndexSet})
            (.withMeta 'mid-index {:tag 'IndexSet})]
           {:tag 'long})),
       :column 1}
      :name
      'find-last-tx
      :ns
      *ns*))
  (defmethod print-method datomic.db.Db fn__13606 ([db w] (.write ^java.io.Writer w (str db)) nil))
  (defmethod print-dup datomic.db.Db fn__13608 ([o w] (print-method o w)))
  (def load-builtins (delay (clojure.core/require 'datomic.builtins)))
  (reset-meta! #'load-builtins (assoc {:private true, :column 1} :name 'load-builtins :ns *ns*))
  (defn bootstrap-maybe-resolve
    ([db a v]
      (or
        (when (and (keyword? v) (not (contains? #{:db/ident 10} a))) (datomic.db/resolve-id db v))
        v)))
  (reset-meta!
    #'bootstrap-maybe-resolve
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'a 'v]), :column 1}
      :name
      'bootstrap-maybe-resolve
      :ns
      *ns*))
  (defn add-upgrade-data
    ([db system_data]
      (let [map__13615 db
            map__13615 (if (seq? map__13615)
                         (clojure.lang.PersistentHashMap/create (seq map__13615))
                         map__13615)
            basisT (get map__13615 :basisT)
            nextT (get map__13615 :nextT)
            epoch (java.util.Date. 0)]
        (assoc
          (reduce
            (fn fn__13617
              ([db p__13616]
                (let [vec__13618 p__13616
                      k (nth vec__13618 (unchecked-int 0) nil)
                      data (nth vec__13618 (unchecked-int 1) nil)]
                  (if (let [G__13621 (datomic.db/resolve-id db k)]
                        (when-not (nil? G__13621) (.elementAt ^datomic.db.IDbImpl db G__13621)))
                    db
                    (:db-after
                      (.with
                        (assoc db :basisT -1 :nextT 0)
                        (cons
                          #:db{:id (DbId/create {:idx -1000001, :part :db.part/tx}),
                               :txInstant epoch}
                          data)))))))
            db
            (partition 2 system_data))
          :basisT
          basisT
          :nextT
          nextT))))
  (defn finish-init
    ([db]
      (datomic.db/add-system-eids
        (datomic.db/add-upgrade-data db datomic.db/bootstrap-data-upgrades))))
  (reset-meta!
    #'finish-init
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'finish-init
      :ns
      *ns*))
  (defn db
    ([id p__13625]
      (let [map__13626 p__13625
            map__13626 (if (seq? map__13626)
                         (clojure.lang.PersistentHashMap/create (seq map__13626))
                         map__13626)
            root_id (get map__13626 :root-id)
            mid_index (get map__13626 :mid-index)
            index (get map__13626 :index)
            history (get map__13626 :history)
            basisT (get map__13626 :basisT)
            nextT (get map__13626 :nextT)
            schema_level (get map__13626 :schema-level)
            birth_level (get map__13626 :birth-level)
            rev (get map__13626 :rev)]
        (deref datomic.db/load-builtins)
        (let [basisT (if basisT
                       basisT
                       (long
                         (datomic.db/find-last-tx
                           (unchecked-long ^java.lang.Number nextT)
                           index
                           mid_index)))
              db (datomic.db.Db.
                   id
                   datomic.db/mem-index-set
                   nil
                   mid_index
                   index
                   history
                   (datomic.db/memlog)
                   (unchecked-long ^java.lang.Number basisT)
                   (unchecked-long ^java.lang.Number nextT)
                   (unchecked-long ^java.lang.Number basisT)
                   nil
                   []
                   {}
                   {}
                   root_id
                   rev
                   nil
                   nil
                   nil
                   nil)]
          (datomic.db/finish-init
            (datomic.db/run-hooks
              (assoc db :schema-level schema_level :birth-level birth_level)
              index))))))
  (defn has-memory-index?
    ([db]
      (or
        (not (nil? (.-indexing ^datomic.db.Db db)))
        (not (= datomic.db/mem-index-set (.-memidx ^datomic.db.Db db))))))
  (defn recalc-elements
    ([db basisT]
      (let [basis_db (.asOf ^datomic.Database db basisT)]
        (assoc
          db
          :elements
          (mapv
            (fn fn__13630
              ([e]
                (if (instance? datomic.db.Attribute e)
                  (let [ent (.entity ^datomic.Database basis_db (:id e))
                        has_attr? (seq (:db.install/_attribute ent))]
                    (if has_attr? (assoc e :storageHasAVET (datomic.db/needs-avet? ent)) e))
                  e)))
            (:elements db))))))
  (defn prepare-for-indexing
    ([db]
      (if (.-indexing ^datomic.db.Db db)
        db
        (assoc
          db
          :indexing
          (.-memidx ^datomic.db.Db db)
          :memidx
          datomic.db/mem-index-set
          :indexingNextT
          (:nextT db)))))
  (defn complete-indexing
    ([db p__13634]
      (let [map__13635 p__13634
            map__13635 (if (seq? map__13635)
                         (clojure.lang.PersistentHashMap/create (seq map__13635))
                         map__13635)
            new_index map__13635
            root_id (get map__13635 :root-id)
            index (get map__13635 :index)
            mid_index (get map__13635 :mid-index)
            history (get map__13635 :history)
            basisT (get map__13635 :basisT)
            nextT (get map__13635 :nextT)
            rev (get map__13635 :rev)]
        (if (and (.-indexing ^datomic.db.Db db) (> rev (.-index-rev ^datomic.db.Db db)))
          (if (= (.-indexingNextT ^datomic.db.Db db) nextT)
            (datomic.db/recalc-elements
              (assoc
                db
                :indexing
                nil
                :indexingNextT
                nil
                :memlog
                (datomic.db/trim-log (.-memlog ^datomic.db.Db db) basisT)
                :index
                index
                :mid-index
                mid_index
                :indexBasisT
                basisT
                :history
                history
                :index-root-id
                root_id
                :index-rev
                rev)
              basisT)
            (do
              (throw
                (ex-info
                  "Indexing surpassed by another indexing process"
                  {:local-index-basis (:indexBasisT db), :stored-index-basis basisT}))
              nil))
          db))))
  (defn accept-index
    ([db p__13639]
      (let [map__13640 p__13639
            map__13640 (if (seq? map__13640)
                         (clojure.lang.PersistentHashMap/create (seq map__13640))
                         map__13640)
            root_id (get map__13640 :root-id)
            mid_index (get map__13640 :mid-index)
            index (get map__13640 :index)
            history (get map__13640 :history)
            basisT (get map__13640 :basisT)
            nextT (get map__13640 :nextT)
            rev (get map__13640 :rev)]
        (when-not (nil? (.-indexing ^datomic.db.Db db))
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                (pr-str (clojure.core/list 'nil? (clojure.core/list '.indexing 'db)))))))
        (if (> rev (.-index-rev ^datomic.db.Db db))
          (let [m_13641 {:event :db/accept-index, :basis-t :basisT}
                ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.db")]
                                  (when (.isInfoEnabled ^org.slf4j.Logger logger)
                                    (.info
                                      ^org.slf4j.Logger logger
                                      (logger/process (assoc m_13641 :phase :begin)))
                                    nil)
                                  nil)
                start__8981__auto__ (java.lang.System/nanoTime)
                result__8982__auto__ (try
                                       {:returned
                                        (let [new_memidx datomic.db/mem-index-set
                                              adopt (fn adopt
                                                      ([nidx idx]
                                                        (loop [nidx nidx
                                                               iter (datomic.btset/seek idx)]
                                                          (let [temp__5455__auto__
                                                                (datomic.db/dget iter)]
                                                            (if
                                                              temp__5455__auto__
                                                              (let 
                                                                [d temp__5455__auto__]
                                                                (recur
                                                                  (if
                                                                    (<
                                                                      (.getT
                                                                        ^datomic.impl.db.IDatum d)
                                                                      nextT)
                                                                    nidx
                                                                    (conj nidx d))
                                                                  (iter/inext iter)))
                                                              nidx)))))
                                              iset (.-memidx ^datomic.db.Db db)
                                              eavtr (future-call
                                                      (fn fn__13648
                                                        ([]
                                                          (^clojure.lang.IFn adopt
                                                            (.-eavt
                                                              ^datomic.db.IndexSet new_memidx)
                                                            (.-eavt ^datomic.db.IndexSet iset)))))
                                              avetr (future-call
                                                      (fn fn__13650
                                                        ([]
                                                          (^clojure.lang.IFn adopt
                                                            (.-avet
                                                              ^datomic.db.IndexSet new_memidx)
                                                            (.-avet ^datomic.db.IndexSet iset)))))
                                              aevtr (future-call
                                                      (fn fn__13652
                                                        ([]
                                                          (^clojure.lang.IFn adopt
                                                            (.-aevt
                                                              ^datomic.db.IndexSet new_memidx)
                                                            (.-aevt ^datomic.db.IndexSet iset)))))
                                              raetr (future-call
                                                      (fn fn__13654
                                                        ([]
                                                          (^clojure.lang.IFn adopt
                                                            (.-raet
                                                              ^datomic.db.IndexSet new_memidx)
                                                            (.-raet ^datomic.db.IndexSet iset)))))
                                              eavt (deref eavtr)
                                              avet (deref avetr)
                                              aevt (deref aevtr)
                                              raet (deref raetr)
                                              ft_basis (assoc
                                                         db
                                                         :memidx
                                                         (datomic.db.IndexSet.
                                                           eavt
                                                           avet
                                                           aevt
                                                           raet
                                                           nil)
                                                         :memlog
                                                         (datomic.db/trim-log
                                                           (.-memlog ^datomic.db.Db db)
                                                           basisT)
                                                         :index
                                                         index
                                                         :mid-index
                                                         mid_index
                                                         :indexBasisT
                                                         basisT
                                                         :history
                                                         history
                                                         :index-root-id
                                                         root_id
                                                         :index-rev
                                                         rev)
                                              ft (ftindex/update-fulltext
                                                   nil
                                                   (iter/iter-seq
                                                     (iter/filter
                                                       (fn fn__13656
                                                         ([p1__13638#]
                                                           (datomic.db/fulltext?
                                                             ft_basis
                                                             (java.lang.Integer/valueOf
                                                               (int
                                                                 (.getA
                                                                   ^datomic.impl.db.IDatum p1__13638#))))))
                                                       (datomic.btset/seek aevt))))]
                                          (datomic.db/recalc-elements
                                            (assoc
                                              ft_basis
                                              :memidx
                                              (datomic.db.IndexSet. eavt avet aevt raet ft))
                                            basisT))}
                                       (catch
                                         java.lang.Throwable
                                         t__8983__auto__
                                         {:threw t__8983__auto__}))
                elapsed_13642 (- (java.lang.System/nanoTime) start__8981__auto__)
                msec_13643 (logger/format-as-msec (long elapsed_13642))]
            (monitor/add-stat :AcceptIndexMsec msec_13643)
            (let [endmsg__8984__auto__ (merge
                                         (assoc m_13641 :msec msec_13643 :phase :end)
                                         (when (:threw result__8982__auto__)
                                           {:threw (class (:threw result__8982__auto__))}))
                  logger (org.slf4j.LoggerFactory/getLogger "datomic.db")]
              (when (.isInfoEnabled ^org.slf4j.Logger logger)
                (.info ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
                nil)
              nil)
            (if (contains? result__8982__auto__ :returned)
              (:returned result__8982__auto__)
              (do (throw (:threw result__8982__auto__)) nil)))
          db))))
  (definterface
    IProcess
    (^datomic.db.IProcess inject [^java.lang.Object arg0 ^java.util.Map arg1]))
  (clojure.core/import 'datomic.db.IProcess)
  (definterface IProcessExpander (^java.lang.Object getData [^java.lang.Object arg0]))
  (clojure.core/import 'datomic.db.IProcessExpander)
  (deftype
    ProcessCollector
    [arraylist]
    datomic.db.IProcess
    (^datomic.db.IProcess inject
      [this procargs ^java.util.Map local_tempids]
      (do (.add ^java.util.ArrayList arraylist procargs) this)))
  (clojure.core/import 'datomic.db.ProcessCollector)
  (defn ->ProcessCollector ([arraylist] (datomic.db.ProcessCollector. arraylist)))
  (def write-handler-lookup
   (org.fressian.handlers.WriteHandlerLookup. (fressian/as-lookup fressian/user-write-handlers)))
  (reset-meta!
    #'write-handler-lookup
    (assoc
      {:tag org.fressian.handlers.IWriteHandlerLookup, :column 1}
      :name
      'write-handler-lookup
      :ns
      *ns*))
  (defn reverse-key?
    ([k]
      (boolean
        (let [n (name k)]
          (when-not (str/blank? n)
            (= (char (.charValue \_)) (char (.charAt ^java.lang.String n (unchecked-int 0)))))))))
  (defn reverse-lookup? ([db k] (and (datomic.db/reverse-key? k) (not (contains? (:_keys db) k)))))
  (defn reverse-key
    ([k]
      (let [n (name k)]
        (if (= (char (.charValue \_)) (char (.charAt ^java.lang.String n (unchecked-int 0))))
          (keyword (namespace k) (subs n 1))
          (keyword (namespace k) (str "_" n))))))
  (defn force-map-keywords
    ([db m]
      (if (every? (fn fn__13677 ([k] (or (number? k) (keyword? k)))) (keys m))
        m
        (reduce
          (fn fn__13681
            ([m p__13680]
              (let [vec__13682 p__13680
                    k (nth vec__13682 (unchecked-int 0) nil)
                    v (nth vec__13682 (unchecked-int 1) nil)
                    kw (cond
                         (integer? k) k
                         (instance? java.util.List k) (datomic.db/resolve-id db k)
                         :else (do (datomic.db/to-kw k)))]
                (when (get m kw)
                  (error/arg :db.error/map-key-collision "Key collision" {:input m}))
                (assoc m kw v))))
          {}
          m))))
  (defn nested-entity-map?
    ([db attrid v]
      (and
        (instance? java.util.Map v)
        (not (instance? datomic.db.DbId v))
        (= 20 (.-vtypeid (datomic.db/attribute db attrid))))))
  (defn normalize-map ([m] (if (associative? m) m (into {} m))))
  (reset-meta!
    #'normalize-map
    (assoc
      {:private true, :arglists (clojure.core/list ['m]), :column 1}
      :name
      'normalize-map
      :ns
      *ns*))
  (defn string-tempid ([] (str "datomic.temp-" (datomic.db/next-id))))
  (defn has-unique-id?
    ([db emap]
      (let [unique_id? (fn unique_id_QMARK_
                         ([p1__13692#] (= 38 (.-unique (datomic.db/require-attr db p1__13692#)))))]
        (some
          (fn fn__13696 ([p1__13693#] (^clojure.lang.IFn unique_id? p1__13693#)))
          (keys emap)))))
  (reset-meta!
    #'has-unique-id?
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'emap]), :column 1}
      :name
      'has-unique-id?
      :ns
      *ns*))
  (definterface
    AssignPartitions
    (^java.lang.Object forcePart [^java.lang.Object arg0 ^java.lang.Object arg1])
    (^java.lang.Object matchPart [^java.lang.Object arg0 ^java.lang.Object arg1]))
  (clojure.core/import 'datomic.db.AssignPartitions)
  (definterface GetPartition (^java.lang.Object getPart [^java.lang.Object arg0]))
  (clojure.core/import 'datomic.db.GetPartition)
  (defn reserved-partition? ([^long part] (< part 4)))
  (deftype
    PartitionRequests
    [id_>part id_>match]
    datomic.db.GetPartition
    datomic.db.AssignPartitions
    (getPart
      [this id]
      (or
        (get id_>part id)
        (let [temp__5457__auto__ (get id_>match id)]
          (when temp__5457__auto__
            (let [nxt temp__5457__auto__]
              (loop [visited #{id} nxt nxt]
                (if (contains? visited nxt)
                  (error/arg :db.error/cycle-in-affinity {:e id})
                  (or
                    (get id_>part nxt)
                    (let [temp__5455__auto__ (get id_>match nxt)]
                      (if temp__5455__auto__
                        (let [nnxt temp__5455__auto__] (recur (conj visited nxt) nnxt))
                        (long
                          (datomic.db/eid->part (unchecked-long ^java.lang.Number nxt)))))))))))
        (long (datomic.db/eid->part (unchecked-long ^java.lang.Number id)))))
    (matchPart
      [this id id_to_match]
      (when-not (or
                  (datomic.db/reserved-partition?
                    (datomic.db/eid->part (unchecked-long ^java.lang.Number id)))
                  (datomic.db/reserved-partition?
                    (datomic.db/eid->part (unchecked-long ^java.lang.Number id_to_match))))
        (.put ^java.util.HashMap id_>match id id_to_match)))
    (forcePart
      [this id partbits]
      (when-not (datomic.db/reserved-partition?
                  (datomic.db/eid->part (unchecked-long ^java.lang.Number id)))
        (let [temp__5455__auto__ (get id_>part id)]
          (if temp__5455__auto__
            (let [existing temp__5455__auto__]
              (when-not (zero? existing) (.put ^java.util.HashMap id_>part id partbits)))
            (.put ^java.util.HashMap id_>part id partbits))))))
  (clojure.core/import 'datomic.db.PartitionRequests)
  (defn ->PartitionRequests
    ([id_>part id_>match] (datomic.db.PartitionRequests. id_>part id_>match)))
  (defn part-requests
    ([] (datomic.db.PartitionRequests. (java.util.HashMap.) (java.util.HashMap.))))
  (defn make-child-id
    ([db parentid attrid childmap]
      (let [attr (datomic.db/attribute db attrid)]
        (if (or (.-isComponent ^datomic.db.Attribute attr) (datomic.db/has-unique-id? db childmap))
          (datomic.db/tempid 16)
          (error/arg
            :db.error/invalid-nested-entity
            "Nested entity is not a component and has no :db/id"
            {:entity childmap})))))
  (reset-meta!
    #'make-child-id
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'parentid 'attrid 'childmap]), :column 1}
      :name
      'make-child-id
      :ns
      *ns*))
  (declare datomic.db/expand-map)
  (defn expand-submap
    ([db parentid attrid v part_reqs local_tempids]
      (let [v (datomic.db/force-map-keywords db (datomic.db/normalize-map v))
            childid (datomic.db/local-id
                      (or (:db/id v) (datomic.db/make-child-id db parentid attrid v))
                      db
                      nil
                      local_tempids)]
        (when (.-isComponent (datomic.db/attribute db attrid))
          (.matchPart ^datomic.db.AssignPartitions part_reqs childid parentid))
        (cons
          [:db/add parentid attrid childid]
          (datomic.db/expand-map db (assoc v :db/id childid) part_reqs local_tempids)))))
  (reset-meta!
    #'expand-submap
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         ['db
          'parentid
          'attrid
          'v
          (.withMeta 'part-reqs {:tag 'AssignPartitions})
          'local-tempids]),
       :column 1}
      :name
      'expand-submap
      :ns
      *ns*))
  (defn forward-attr
    ([x]
      (if (number? x)
        x
        (let [n (name x)]
          (if (= (char (.charValue \_)) (char (.charAt ^java.lang.String n (unchecked-int 0))))
            (keyword (namespace x) (subs n 1))
            x)))))
  (defn process-force-partition
    ([forcemap db part_reqs local_tempids]
      (if (map? forcemap)
        (reduce-kv
          (fn fn__13724
            ([_ id part]
              (.forcePart
                ^datomic.db.AssignPartitions part_reqs
                (datomic.db/local-id id db nil local_tempids)
                (datomic.db/partbits db part))))
          nil
          forcemap)
        (error/arg
          :db.error/invalid-force-partition-map
          "Value of :db/force-partition must be a map"
          {:input forcemap}))))
  (defn process-match-partition
    ([matchmap db part_reqs local_tempids]
      (if (map? matchmap)
        (reduce-kv
          (fn fn__13727
            ([_ id primary_id]
              (.matchPart
                ^datomic.db.AssignPartitions part_reqs
                (datomic.db/local-id id db nil local_tempids)
                (datomic.db/local-id primary_id db nil local_tempids))))
          nil
          matchmap)
        (error/arg
          :db.error/invalid-match-partition-map
          "Value of :db/match-partition must be a map"
          {:input matchmap}))))
  (defn expand-map
    ([db m part_reqs local_tempids]
      (let [dbid (datomic.db/local-id
                   (or (get m :db/id) (datomic.db/tempid 16))
                   db
                   nil
                   local_tempids)
            hook_entry? (fn hook_entry_QMARK_
                          ([p1__13730#]
                            (contains?
                              #{"db.alter" "db.install"}
                              (namespace
                                (datomic.db/resolve-kw
                                  db
                                  (.getKey ^java.util.Map$Entry p1__13730#))))))]
        (reduce
          (fn fn__13734
            ([result p__13733]
              (let [vec__13735 p__13733
                    k (nth vec__13735 (unchecked-int 0) nil)
                    v (nth vec__13735 (unchecked-int 1) nil)
                    G__13738 k]
                (case
                  G__13738
                  :db/match-partition
                  (do (datomic.db/process-match-partition v db part_reqs local_tempids) result)
                  :db/force-partition
                  (do (datomic.db/process-force-partition v db part_reqs local_tempids) result)
                  :db/id
                  result
                  (let [attr (datomic.db/forward-attr k)
                        attrib (datomic.db/require-attr db attr)
                        attrid (.id ^datomic.db.Attribute attrib)]
                    (if (= attr k)
                      (reduce
                        (fn fn__13739
                          ([result v]
                            (if (datomic.db/nested-entity-map? db attrid v)
                              (into
                                result
                                (datomic.db/expand-submap
                                  db
                                  dbid
                                  attrid
                                  v
                                  part_reqs
                                  local_tempids))
                              (conj result [:db/add dbid attrid v]))))
                        result
                        (cond
                          (and (instance? java.util.List v) (= 36 (:cardinality attrib))) v
                          (instance? java.util.Set v) (seq v)
                          :default (do [v])))
                      (conj result [:db/add v attrid dbid])))))))
          []
          (concat (remove hook_entry? m) (filter hook_entry? m))))))
  (defn wrong-type-for-attribute
    ([db attrid tag v]
      (error/arg
        :db.error/wrong-type-for-attribute
        (str
          "Value "
          v
          " is not a valid "
          (datomic.db/resolve-kw db tag)
          " for attribute "
          (datomic.db/resolve-kw db attrid)))))
  (defn canonicalize-v
    ([db attrid v tag]
      (cond
        (and (= tag :float) (instance? java.lang.Double v)) (java.lang.Float/valueOf
                                                              (unchecked-float v))
        (and (= tag :bigint) (instance? clojure.lang.BigInt v)) (biginteger v)
        :default (do (datomic.db/wrong-type-for-attribute db attrid tag v)))))
  (defonce TupleElem {})
  (defprotocol TupleElem (tuple-elem? [v]))
  (extend nil datomic.db/TupleElem {:tuple-elem? (fn fn__13765 ([_] true))})
  (extend java.lang.Object datomic.db/TupleElem {:tuple-elem? (fn fn__13767 ([_] false))})
  (extend
    java.math.BigInteger
    datomic.db/TupleElem
    {:tuple-elem? (fn fn__13769 ([v] (<= (.bitLength ^java.math.BigInteger v) 256)))})
  (extend java.lang.Double datomic.db/TupleElem {:tuple-elem? (fn fn__13771 ([_] true))})
  (extend java.util.Date datomic.db/TupleElem {:tuple-elem? (fn fn__13773 ([_] true))})
  (extend
    java.lang.String
    datomic.db/TupleElem
    {:tuple-elem? (fn fn__13775 ([s] (<= (.length ^java.lang.String s) 256)))})
  (extend java.util.UUID datomic.db/TupleElem {:tuple-elem? (fn fn__13777 ([_] true))})
  (extend java.lang.Boolean datomic.db/TupleElem {:tuple-elem? (fn fn__13779 ([_] true))})
  (extend java.net.URI datomic.db/TupleElem {:tuple-elem? (fn fn__13781 ([_] true))})
  (extend
    java.math.BigDecimal
    datomic.db/TupleElem
    {:tuple-elem? (fn fn__13783 ([v] (<= (.precision ^java.math.BigDecimal v) 256)))})
  (extend clojure.lang.Keyword datomic.db/TupleElem {:tuple-elem? (fn fn__13785 ([_] true))})
  (extend java.lang.Long datomic.db/TupleElem {:tuple-elem? (fn fn__13787 ([_] true))})
  (extend clojure.lang.Symbol datomic.db/TupleElem {:tuple-elem? (fn fn__13789 ([_] true))})
  (defonce CoerceV {})
  (defprotocol CoerceV (coerce-v [v]))
  (extend nil datomic.db/CoerceV {:coerce-v (fn fn__13807 ([_] nil))})
  (extend java.lang.Object datomic.db/CoerceV {:coerce-v (fn fn__13809 ([_] nil))})
  (extend
    java.lang.Short
    datomic.db/CoerceV
    {:coerce-v (fn fn__13811 ([v] (long (.longValue ^java.lang.Short v))))})
  (extend
    java.lang.Integer
    datomic.db/CoerceV
    {:coerce-v (fn fn__13813 ([v] (long (.longValue ^java.lang.Integer v))))})
  (extend clojure.lang.BigInt datomic.db/CoerceV {:coerce-v (fn fn__13815 ([v] (biginteger v)))})
  (defn coerce-tuple
    ([tuple]
      (cond
        (some datomic.db/coerce-v tuple) (mapv
                                           (fn fn__13818
                                             ([p1__13817#]
                                               (or (datomic.db/coerce-v p1__13817#) p1__13817#)))
                                           tuple)
        (vector? tuple) tuple
        (instance? java.util.RandomAccess tuple) (into [] tuple)
        :else (do tuple))))
  (reset-meta!
    #'coerce-tuple
    (assoc
      {:private true, :arglists (clojure.core/list ['tuple]), :column 1}
      :name
      'coerce-tuple
      :ns
      *ns*))
  (defn valid-tuple-assert?
    ([attr tup]
      (let [valid? (fn valid_QMARK_
                     ([kw elem]
                       (let [cl (datomic.db/tuple-value-types kw)]
                         (and
                           cl
                           (or
                             (nil? elem)
                             (instance? cl elem)
                             (and (= kw :db.type/ref) (string? elem)))))))]
        (cond
          (:tupleAttrs attr) (and
                               (vector? tup)
                               (= (long (count tup)) (long (count (:tupleAttrs attr)))))
          (:tupleType attr) (let [kw (:tupleType attr)]
                              (and
                                (vector? tup)
                                (every?
                                  (fn fn__13831
                                    ([p1__13822#] (^clojure.lang.IFn valid? kw p1__13822#)))
                                  tup)
                                (<= 2 (java.lang.Integer/valueOf (int (count tup))) 8)))
          (:tupleTypes attr) (do
                               (let [kws (seq (:tupleTypes attr))]
                                 (and
                                   (vector? tup)
                                   (= (long (count kws)) (long (count tup)))
                                   (every?
                                     true?
                                     (map
                                       (fn fn__13833
                                         ([p1__13823# p2__13824#]
                                           (^clojure.lang.IFn valid? p1__13823# p2__13824#)))
                                       kws
                                       tup)))))))))
  (reset-meta!
    #'valid-tuple-assert?
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'attr {:tag 'Attribute}) 'tup]),
       :column 1}
      :name
      'valid-tuple-assert?
      :ns
      *ns*))
  (defn validated-tuple
    ([db op attr v]
      (let [tup (datomic.db/coerce-tuple (datomic.db/require-tuple-ids db attr v))]
        (if (= 1 op)
          (and
            tup
            (every? datomic.db/tuple-elem? tup)
            (datomic.db/valid-tuple-assert? attr tup)
            tup)
          tup))))
  (reset-meta!
    #'validated-tuple
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'db {:tag 'Database}) 'op (.withMeta 'attr {:tag 'Attribute}) 'v]),
       :column 1}
      :name
      'validated-tuple
      :ns
      *ns*))
  (defn inject-retracts!
    ([nextp db eid attrid local_tempids]
      (reduce
        (fn fn__13846
          ([proc p__13845]
            (let [map__13847 p__13845
                  map__13847 (if (seq? map__13847)
                               (clojure.lang.PersistentHashMap/create (seq map__13847))
                               map__13847)
                  e (get map__13847 :e)
                  a (get map__13847 :a)
                  v (get map__13847 :v)]
              (.inject ^datomic.db.IProcess proc [2 e a v] ^java.util.Map local_tempids))))
        nextp
        (datomic.db/datoms db :aevt [attrid eid]))))
  (reset-meta!
    #'inject-retracts!
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [(.withMeta 'nextp {:tag 'IProcess}) 'db 'eid 'attrid 'local-tempids]),
       :column 1}
      :name
      'inject-retracts!
      :ns
      *ns*))
  (defn validated-v-for-attr
    ([db attr v procargs procid]
      (let [vt (.elementAt ^datomic.db.Db db (.-vtypeid ^datomic.db.Attribute attr))]
        (cond
          (nil? v) (error/arg
                     :db.error/nil-value
                     "Nil is not a legal value"
                     {:data procargs,
                      :attribute (datomic.db/resolve-kw db (.id ^datomic.db.Attribute attr))})
          (= (.-vtypeid ^datomic.db.Attribute attr) 20) (if procargs
                                                          (datomic.db/require-id db v procargs)
                                                          (long (datomic.db/require-id db v)))
          (= (.-vtypeid ^datomic.db.Attribute attr) 21) (let [kw (datomic.db/to-kw v)]
                                                          (if (keyword? kw)
                                                            kw
                                                            (datomic.db/wrong-type-for-attribute
                                                              db
                                                              (.id ^datomic.db.Attribute attr)
                                                              (:fressian-tag vt)
                                                              v)))
          (= (.-vtypeid ^datomic.db.Attribute attr) (datomic.db/system-eid db :db.type/tuple)) (let 
                                                                                                 [temp__5455__auto__
                                                                                                  (datomic.db/validated-tuple
                                                                                                    db
                                                                                                    (or
                                                                                                      procid
                                                                                                      1)
                                                                                                    attr
                                                                                                    v)]
                                                                                                 (if
                                                                                                   temp__5455__auto__
                                                                                                   (let 
                                                                                                     [tup
                                                                                                      temp__5455__auto__]
                                                                                                     tup)
                                                                                                   (error/arg
                                                                                                     :db.error/invalid-tuple-value
                                                                                                     "Invalid tuple value"
                                                                                                     {:value
                                                                                                      v,
                                                                                                      :attribute
                                                                                                      (datomic.db/resolve-kw
                                                                                                        db
                                                                                                        (.id
                                                                                                          ^datomic.db.Attribute attr))})))
          (.getWriteHandler datomic.db/write-handler-lookup (subs (str (:fressian-tag vt)) 1) v) v
          :else (do
                  (datomic.db/canonicalize-v
                    db
                    (.id ^datomic.db.Attribute attr)
                    v
                    (:fressian-tag vt))))))
    ([db attr v] (datomic.db/validated-v-for-attr db attr v nil nil)))
  (deftype
    ProcessInpoint
    [db part_reqs nextp]
    datomic.db.IProcess
    (^datomic.db.IProcess inject
      [this procargs ^java.util.Map local_tempids]
      (do
        (cond
          (instance? java.util.Map procargs) (loop [seq_13854 (seq
                                                                (datomic.db/expand-map
                                                                  db
                                                                  (datomic.db/force-map-keywords
                                                                    db
                                                                    procargs)
                                                                  part_reqs
                                                                  local_tempids))
                                                    chunk_13855 nil
                                                    count_13856 0
                                                    i_13857 0]
                                               (if (< i_13857 count_13856)
                                                 (let [x (.nth
                                                           ^clojure.lang.Indexed chunk_13855
                                                           (unchecked-int i_13857))]
                                                   (.inject this x ^java.util.Map local_tempids)
                                                   (recur
                                                     seq_13854
                                                     chunk_13855
                                                     count_13856
                                                     (inc i_13857)))
                                                 (let [temp__5457__auto__ (seq seq_13854)]
                                                   (when temp__5457__auto__
                                                     (let [seq_13854 temp__5457__auto__]
                                                       (if (chunked-seq? seq_13854)
                                                         (let [c__5719__auto__
                                                               (chunk-first seq_13854)]
                                                           (recur
                                                             (chunk-rest seq_13854)
                                                             c__5719__auto__
                                                             (count c__5719__auto__)
                                                             0))
                                                         (let [x (first seq_13854)]
                                                           (.inject
                                                             this
                                                             x
                                                             ^java.util.Map local_tempids)
                                                           (recur (next seq_13854) nil 0 0))))))))
          (instance? java.util.List procargs) (let [procid (datomic.db/resolve-id
                                                             db
                                                             (nth procargs (unchecked-int 0)))]
                                                (if (or (= 1 procid) (= 2 procid))
                                                  (let [eid (datomic.db/require-id
                                                              db
                                                              (nth procargs (unchecked-int 1))
                                                              procargs)
                                                        a (nth procargs (unchecked-int 2))
                                                        attr (datomic.db/require-attr db a)
                                                        attrid (.id ^datomic.db.Attribute attr)]
                                                    (if (and (= 2 procid) (< (count procargs) 4))
                                                      (datomic.db/inject-retracts!
                                                        nextp
                                                        db
                                                        eid
                                                        attrid
                                                        local_tempids)
                                                      (let [v
                                                            (datomic.db/validated-v-for-attr
                                                              db
                                                              attr
                                                              (nth procargs (unchecked-int 3))
                                                              procargs
                                                              procid)]
                                                        (.inject
                                                          ^datomic.db.IProcess nextp
                                                          [procid eid attrid v]
                                                          ^java.util.Map local_tempids))))
                                                  (.inject
                                                    ^datomic.db.IProcess nextp
                                                    procargs
                                                    ^java.util.Map local_tempids)))
          :default (do
                     (error/arg
                       :db.error/not-transaction-data
                       (str "Transaction data element must be a List or Map, got " procargs))))
        this)))
  (clojure.core/import 'datomic.db.ProcessInpoint)
  (defn ->ProcessInpoint ([db part_reqs nextp] (datomic.db.ProcessInpoint. db part_reqs nextp)))
  (defn default-partition
    ([db]
      (or
        (let [temp__5457__auto__ (.entid
                                   ^datomic.db.Db db
                                   (config/property "datomic.defaultPartition"))]
          (when temp__5457__auto__
            (let [p temp__5457__auto__] (when (datomic.db/explicit-partition db p) p))))
        4)))
  (reset-meta!
    #'default-partition
    (assoc
      {:private true, :arglists (clojure.core/list ['db]), :column 1}
      :name
      'default-partition
      :ns
      *ns*))
  (defn get-ids
    ([db data part_reqs]
      (let [default_part_ref (delay (datomic.db/default-partition db))
            genid (fn genid
                    ([e p__13874]
                      (let [vec__13876 p__13874
                            m (nth vec__13876 (unchecked-int 0) nil)
                            p (nth vec__13876 (unchecked-int 1) nil)
                            t (nth vec__13876 (unchecked-int 2) nil)
                            u (nth vec__13876 (unchecked-int 3) nil)
                            z (nth vec__13876 (unchecked-int 4) nil)
                            mpt vec__13876]
                        (if (contains? m e)
                          mpt
                          (let [part (.getPart ^datomic.db.GetPartition part_reqs e)
                                part (if (= part 16) (deref default_part_ref) part)]
                            (cond
                              (zero? part) [(assoc
                                              m
                                              e
                                              (long
                                                (datomic.db/make-eid
                                                  (unchecked-long ^java.lang.Number part)
                                                  (unchecked-long ^java.lang.Number p))))
                                            (inc p)
                                            t
                                            u
                                            true]
                              (= 3 part) [(assoc
                                            m
                                            e
                                            (long
                                              (datomic.db/make-eid
                                                (unchecked-long ^java.lang.Number part)
                                                (.nextT ^datomic.db.Db db))))
                                          p
                                          t
                                          u
                                          z]
                              (datomic.db/valid-partbits?
                                db
                                (unchecked-long ^java.lang.Number part)) [(assoc
                                                                            m
                                                                            e
                                                                            (long
                                                                              (datomic.db/make-eid
                                                                                (unchecked-long
                                                                                  ^java.lang.Number part)
                                                                                (unchecked-long
                                                                                  ^java.lang.Number t))))
                                                                          p
                                                                          (inc t)
                                                                          u
                                                                          z]
                              :else (do
                                      (error/arg
                                        :db.error/not-a-partition
                                        (str "Entity id " e " is not in a valid partition")))))))))
            vec__13869 (reduce
                         (fn fn__13881
                           ([p__13880 d]
                             (let [vec__13882 p__13880
                                   m (nth vec__13882 (unchecked-int 0) nil)
                                   p (nth vec__13882 (unchecked-int 1) nil)
                                   t (nth vec__13882 (unchecked-int 2) nil)
                                   u (nth vec__13882 (unchecked-int 3) nil)
                                   z (nth vec__13882 (unchecked-int 4) nil)
                                   mpt vec__13882
                                   e (.getE ^datomic.impl.db.IDatum d)
                                   a (.getA ^datomic.impl.db.IDatum d)]
                               (cond
                                 (datomic.db/tempid? e) (let [attr
                                                              (datomic.db/require-attr
                                                                db
                                                                (java.lang.Integer/valueOf
                                                                  (int a)))
                                                              v (.getV ^datomic.impl.db.IDatum d)]
                                                          (if (=
                                                                (.-unique
                                                                  ^datomic.db.Attribute attr)
                                                                38)
                                                            (let 
                                                              [temp__5455__auto__
                                                               (datomic.db/dget
                                                                 (datomic.db/find-avet
                                                                   db
                                                                   (java.lang.Integer/valueOf
                                                                     (int a))
                                                                   v))]
                                                              (if
                                                                temp__5455__auto__
                                                                (let 
                                                                  [it temp__5455__auto__]
                                                                  [(assoc
                                                                     m
                                                                     (long e)
                                                                     (long
                                                                       (.getE
                                                                         ^datomic.impl.db.IDatum it)))
                                                                   p
                                                                   t
                                                                   u
                                                                   z])
                                                                (let 
                                                                  [temp__5455__auto__
                                                                   (get-in
                                                                     u
                                                                     [(java.lang.Integer/valueOf
                                                                        (int a))
                                                                      v])]
                                                                  (if
                                                                    temp__5455__auto__
                                                                    (let 
                                                                      [ue temp__5455__auto__]
                                                                      [(assoc m (long e) ue)
                                                                       p
                                                                       t
                                                                       u
                                                                       z])
                                                                    (let 
                                                                      [vec__13885
                                                                       (^clojure.lang.IFn genid
                                                                         (long e)
                                                                         mpt)
                                                                       m
                                                                       (nth
                                                                         vec__13885
                                                                         (unchecked-int 0)
                                                                         nil)
                                                                       p
                                                                       (nth
                                                                         vec__13885
                                                                         (unchecked-int 1)
                                                                         nil)
                                                                       t
                                                                       (nth
                                                                         vec__13885
                                                                         (unchecked-int 2)
                                                                         nil)
                                                                       u
                                                                       (nth
                                                                         vec__13885
                                                                         (unchecked-int 3)
                                                                         nil)]
                                                                      [m
                                                                       p
                                                                       t
                                                                       (assoc-in
                                                                         u
                                                                         [(java.lang.Integer/valueOf
                                                                            (int a))
                                                                          v]
                                                                         (get m (long e)))
                                                                       z])))))
                                                            (^clojure.lang.IFn genid
                                                              (long e)
                                                              mpt)))
                                 (and
                                   (>= (datomic.db/eid->eidx e) (.nextT ^datomic.db.Db db))
                                   (<= 1000 (.nextT ^datomic.db.Db db))) (error/arg
                                                                           :db.error/invalid-entity-id
                                                                           (str
                                                                             "Invalid entity id: "
                                                                             (long e)))
                                 :else (do mpt)))))
                         [{}
                          (java.lang.Integer/valueOf (int (count (.-elements ^datomic.db.Db db))))
                          (long (inc (.nextT ^datomic.db.Db db)))
                          {}
                          false]
                         data)
            m (nth vec__13869 (unchecked-int 0) nil)
            p (nth vec__13869 (unchecked-int 1) nil)
            t (nth vec__13869 (unchecked-int 2) nil)
            u (nth vec__13869 (unchecked-int 3) nil)
            z (nth vec__13869 (unchecked-int 4) nil)]
        [m z])))
  (defn has-tx-inst?
    ([db now datoms]
      (reduce
        (fn fn__13893
          ([result d]
            (if (and
                  (= 50 (long (.getA ^datomic.impl.db.IDatum d)))
                  (= 3 (long (datomic.db/get-part d))))
              (let [basis (.nextT ^datomic.Database db) v (.getV ^datomic.impl.db.IDatum d)]
                (when result
                  (error/arg
                    :db.error/multiple-tx-instants
                    (str "Time conflict: :db/txInstant specified more than once")))
                (when-not (= basis (datomic.db/get-eidx d))
                  (error/arg
                    :db.error/reset-tx-instant
                    (str "You can set :db/txInstant only on the current transaction.")))
                (when (< (common/compare now v) 0)
                  (error/arg
                    :db.error/future-tx-instant
                    (str "Time conflict: " v " is in the future")))
                (let [temp__5457__auto__ (datomic.db/dget
                                           (datomic.db/find-eavt
                                             db
                                             (long
                                               (datomic.db/make-eid
                                                 3
                                                 (.basisT ^datomic.Database db)))
                                             50))]
                  (when temp__5457__auto__
                    (let [basis_inst temp__5457__auto__]
                      (when (< (common/compare v (.getV ^datomic.impl.db.IDatum basis_inst)) 0)
                        (error/arg
                          :db.error/past-tx-instant
                          (str "Time conflict: " v " is older than database basis"))))))
                true)
              result)))
        false
        datoms)))
  (defn next-valid-inst
    ([db now]
      (let [basis_inst (.getV
                         (datomic.db/dget
                           (datomic.db/find-eavt
                             db
                             (long (datomic.db/make-eid 3 (.basisT ^datomic.db.Db db)))
                             50)))]
        (if (> (common/compare now basis_inst) 0) now basis_inst))))
  (reset-meta!
    #'next-valid-inst
    (assoc
      {:private true, :arglists (clojure.core/list [(.withMeta 'db {:tag 'Db}) 'now]), :column 1}
      :name
      'next-valid-inst
      :ns
      *ns*))
  (defn resolve-procid
    ([db arg]
      (if (common/qualified-symbol? arg)
        (common/requiring-resolve! arg)
        (or
          (datomic.db/resolve-id db arg)
          (error/arg
            :db.error/not-a-data-function
            (str "Unable to resolve data function: " arg))))))
  (reset-meta!
    #'resolve-procid
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'arg]), :column 1}
      :name
      'resolve-procid
      :ns
      *ns*))
  (defonce LocalDb {})
  (defprotocol LocalDb (local-db [db]))
  (defn local-tuple
    ([attr v db procargs local_tempids]
      (let [ref_offset? (:tupleRefOffsets attr)
            result (into
                     []
                     (map-indexed
                       (fn fn__13917
                         ([idx elem]
                           (when-not (nil? elem)
                             (if (^clojure.lang.IFn ref_offset? idx)
                               (datomic.db/local-id elem db procargs local_tempids)
                               elem)))))
                     v)]
        result)))
  (reset-meta!
    #'local-tuple
    (assoc
      {:private true,
       :arglists (clojure.core/list ['attr 'v 'db 'procargs 'local-tempids]),
       :column 1}
      :name
      'local-tuple
      :ns
      *ns*))
  (defn ea->v ([db e a] (:v (first (datomic.db/datoms db :eavt [e a])))))
  (reset-meta!
    #'ea->v
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'e 'a]), :column 1}
      :name
      'ea->v
      :ns
      *ns*))
  (defn create-composite
    ([db eaop_map p__13921]
      (let [vec__13922 p__13921
            e (nth vec__13922 (unchecked-int 0) nil)
            a (nth vec__13922 (unchecked-int 1) nil)
            attrs (:tupleAttrs (datomic.db/attribute db a))
            t (.nextT ^datomic.Database db)
            v (mapv
                (fn fn__13925
                  ([attr]
                    (let [aid (.entid ^datomic.Database db attr)
                          db_v (datomic.db/ea->v db e aid)
                          tx_retract (.get
                                       ^java.util.Map eaop_map
                                       (datomic.db/->EAOpof
                                         (datomic.db/retracting-datum
                                           (unchecked-long ^java.lang.Number e)
                                           (unchecked-long ^java.lang.Number aid)
                                           nil
                                           t)))
                          tx_assert (.get
                                      ^java.util.Map eaop_map
                                      (datomic.db/->EAOpof
                                        (datomic.db/asserting-datum
                                          (unchecked-long ^java.lang.Number e)
                                          (unchecked-long ^java.lang.Number aid)
                                          nil
                                          t)))]
                      (if tx_assert
                        (.getV ^datomic.impl.db.IDatum tx_assert)
                        (when-not (and
                                    tx_retract
                                    (zero?
                                      (common/compare
                                        db_v
                                        (.getV ^datomic.impl.db.IDatum tx_retract))))
                          db_v)))))
                attrs)]
        (if (every? nil? v)
          (let [temp__5457__auto__ (datomic.db/ea->v db e a)]
            (when temp__5457__auto__
              (let [composite_v temp__5457__auto__]
                (datomic.db/retracting-datum
                  (unchecked-long ^java.lang.Number e)
                  (unchecked-long ^java.lang.Number a)
                  composite_v
                  t))))
          (datomic.db/asserting-datum
            (unchecked-long ^java.lang.Number e)
            (unchecked-long ^java.lang.Number a)
            v
            t)))))
  (reset-meta!
    #'create-composite
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'db {:tag 'Database}) (.withMeta 'eaop-map {:tag 'Map}) ['e 'a]]),
       :column 1}
      :name
      'create-composite
      :ns
      *ns*))
  (defn ensure-datom?
    ([db d]
      (and
        (= (datomic.db/system-eid db :db/ensure) (long (.getA ^datomic.impl.db.IDatum d)))
        (.isAssertion ^datomic.impl.db.IDatum d))))
  (definterface
    PrefetchDispatcher
    (^java.lang.Object prefetch1 [^java.lang.Object arg0])
    (^java.lang.Object close []))
  (clojure.core/import 'datomic.db.PrefetchDispatcher)
  (defn prefetch! ([dispatcher f] (.prefetch1 ^datomic.db.PrefetchDispatcher dispatcher f)))
  (defn pf-close! ([dispatcher] (.close ^datomic.db.PrefetchDispatcher dispatcher)))
  (defn prefetch-constituents
    ([dispatcher adder db e a]
      (run!
        (fn fn__13936
          ([attr]
            (datomic.db/prefetch!
              dispatcher
              (fn fn__13937
                ([]
                  (let [start__13414__auto__ (java.lang.System/nanoTime)
                        ret__13415__auto__ (datomic.db/ea->v db e attr)]
                    (datomic.db/long-add!
                      adder
                      (- (java.lang.System/nanoTime) start__13414__auto__))
                    ret__13415__auto__))))))
        (:tupleAttrs (datomic.db/attribute db a)))))
  (defn for-side-effects ([proc] (map (fn fn__13943 ([x] (^clojure.lang.IFn proc x) x)))))
  (defn composites-prefetcher
    ([dispatcher tx_stat_registers db]
      (let [needed_eas (java.util.HashSet.)
            constituents (:constituents db)
            adder (:comp-pf-ms tx_stat_registers)]
        (fn fn__13947
          ([d]
            (let [e (.getE ^datomic.impl.db.IDatum d)
                  a (.getA ^datomic.impl.db.IDatum d)
                  temp__5457__auto__ (get constituents (java.lang.Integer/valueOf (int a)))]
              (when temp__5457__auto__
                (let [composites temp__5457__auto__]
                  (run!
                    (fn fn__13948
                      ([p1__13946#]
                        (when (.add ^java.util.HashSet needed_eas [(long e) p1__13946#])
                          (datomic.db/prefetch-constituents
                            dispatcher
                            adder
                            db
                            (long e)
                            p1__13946#))))
                    composites)))))))))
  (defn generate-composites
    ([db datoms tx_stat_registers]
      (let [eaop_map (java.util.HashMap.)
            needed_eas (java.util.HashSet.)
            constituents (:constituents db)]
        (dotimes [i (count datoms)]
          (let [d (nth datoms (unchecked-int i))]
            (.put ^java.util.HashMap eaop_map (datomic.db.EAOpof. d) d)
            (let [temp__5457__auto__ (get
                                       constituents
                                       (java.lang.Integer/valueOf
                                         (int (.getA ^datomic.impl.db.IDatum d))))]
              (when temp__5457__auto__
                (let [composites temp__5457__auto__]
                  (loop [seq_13954 (seq composites) chunk_13955 nil count_13956 0 i_13957 0]
                    (if (< i_13957 count_13956)
                      (let [c (.nth ^clojure.lang.Indexed chunk_13955 (unchecked-int i_13957))]
                        (.add
                          ^java.util.HashSet needed_eas
                          [(long (.getE ^datomic.impl.db.IDatum d)) c])
                        (recur seq_13954 chunk_13955 count_13956 (inc i_13957)))
                      (let [temp__5457__auto__ (seq seq_13954)]
                        (when temp__5457__auto__
                          (let [seq_13954 temp__5457__auto__]
                            (if (chunked-seq? seq_13954)
                              (let [c__5719__auto__ (chunk-first seq_13954)]
                                (recur
                                  (chunk-rest seq_13954)
                                  c__5719__auto__
                                  (count c__5719__auto__)
                                  0))
                              (let [c (first seq_13954)]
                                (.add
                                  ^java.util.HashSet needed_eas
                                  [(long (.getE ^datomic.impl.db.IDatum d)) c])
                                (recur (next seq_13954) nil 0 0)))))))))))))
        (datomic.db/long-add! (:comp-ct tx_stat_registers) (count needed_eas))
        (let [start__13414__auto__ (java.lang.System/nanoTime)
              ret__13415__auto__ (into
                                   []
                                   (keep
                                     (fn fn__13958
                                       ([p1__13953#]
                                         (datomic.db/create-composite db eaop_map p1__13953#))))
                                   needed_eas)]
          (datomic.db/long-add!
            (:comp-tx-ms tx_stat_registers)
            (- (java.lang.System/nanoTime) start__13414__auto__))
          ret__13415__auto__))))
  (reset-meta!
    #'generate-composites
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'datoms 'tx-stat-registers]), :column 1}
      :name
      'generate-composites
      :ns
      *ns*))
  (defn calc-tempids
    ([gid_>lid lid_>eid]
      (let [lid_>gid (set/map-invert gid_>lid)]
        (persistent!
          (reduce
            (fn fn__13968
              ([m p__13967]
                (let [vec__13969 p__13967
                      lid (nth vec__13969 (unchecked-int 0) nil)
                      eid (nth vec__13969 (unchecked-int 1) nil)]
                  (assoc! m (get lid_>gid lid lid) eid))))
            (transient {})
            lid_>eid)))))
  (reset-meta!
    #'calc-tempids
    (assoc
      {:private true, :arglists (clojure.core/list ['gid->lid 'lid->eid]), :column 1}
      :name
      'calc-tempids
      :ns
      *ns*))
  (defn add-tempids-to-errors
    ([f get_tempids]
      (try
        (^clojure.lang.IFn f)
        (catch
          java.lang.Throwable
          t
          (let [temp__5455__auto__ (some->
                                     (ex-data t)
                                     (assoc :tempids (^clojure.lang.IFn get_tempids)))]
            (if temp__5455__auto__
              (let [data temp__5455__auto__ cls (class t) msg (.getMessage ^java.lang.Throwable t)]
                (cond
                  (= cls datomic.impl.Exceptions$IllegalArgumentExceptionInfo) (do
                                                                                 (throw
                                                                                   (datomic.impl.Exceptions$IllegalArgumentExceptionInfo.
                                                                                     ^java.lang.String msg
                                                                                     ^clojure.lang.IPersistentMap data
                                                                                     ^java.lang.Throwable t))
                                                                                 nil)
                  (= cls datomic.impl.Exceptions$IllegalStateExceptionInfo) (do
                                                                              (throw
                                                                                (datomic.impl.Exceptions$IllegalStateExceptionInfo.
                                                                                  ^java.lang.String msg
                                                                                  ^clojure.lang.IPersistentMap data
                                                                                  ^java.lang.Throwable t))
                                                                              nil)
                  :default (do (throw (ex-info msg data t)) nil)))
              (do (throw ^java.lang.Throwable t) nil)))))))
  (reset-meta!
    #'add-tempids-to-errors
    (assoc
      {:private true, :arglists (clojure.core/list ['f 'get-tempids]), :column 1}
      :name
      'add-tempids-to-errors
      :ns
      *ns*))
  (defn prefetch-redundancy+uniqueness
    ([dispatcher tx_stat_registers db datoms]
      (let [basis (:nextT db)
            map__13977 tx_stat_registers
            map__13977 (if (seq? map__13977)
                         (clojure.lang.PersistentHashMap/create (seq map__13977))
                         map__13977)
            redundancy (get map__13977 :dedup-pf-ms)
            uniqueness (get map__13977 :ucheck-pf-ms)]
        (run!
          (fn fn__13978
            ([d]
              (datomic.db/prefetch!
                dispatcher
                (fn fn__13979
                  ([]
                    (let [eidx (.eidx ^datomic.db.IDatumImpl d)
                          attrid (.getA ^datomic.impl.db.IDatum d)
                          v (.getV ^datomic.impl.db.IDatum d)
                          attr (.elementAt
                                 ^datomic.db.IDbImpl db
                                 (java.lang.Integer/valueOf (int attrid)))
                          card_one? (= (.-cardinality ^datomic.db.Attribute attr) 35)
                          ed (and
                               (or (zero? basis) (< eidx basis))
                               (let [start__13414__auto__ (java.lang.System/nanoTime)
                                     ret__13415__auto__ (datomic.db/dget
                                                          (datomic.db/find-aevt
                                                            db
                                                            (java.lang.Integer/valueOf
                                                              (int
                                                                (.getA ^datomic.impl.db.IDatum d)))
                                                            (long
                                                              (.getE ^datomic.impl.db.IDatum d))
                                                            (when-not
                                                              card_one?
                                                              (.getV ^datomic.impl.db.IDatum d))))]
                                 (datomic.db/long-add!
                                   redundancy
                                   (- (java.lang.System/nanoTime) start__13414__auto__))
                                 ret__13415__auto__))
                          there? (and
                                   ed
                                   (common/equals-with-strict-scale
                                     v
                                     (.getV ^datomic.impl.db.IDatum ed)))
                          redundant? (if (.isAssertion ^datomic.impl.db.IDatum d)
                                       there?
                                       (not there?))]
                      (when (and
                              (not redundant?)
                              (.-unique ^datomic.db.Attribute attr)
                              (.isAssertion ^datomic.impl.db.IDatum d))
                        (let [start__13414__auto__ (java.lang.System/nanoTime)
                              ret__13415__auto__ (datomic.db/dget
                                                   (datomic.db/find-avet
                                                     db
                                                     (java.lang.Integer/valueOf (int attrid))
                                                     v))]
                          (datomic.db/long-add!
                            uniqueness
                            (- (java.lang.System/nanoTime) start__13414__auto__))
                          ret__13415__auto__))))))))
          datoms))))
  (defn prefetch-identity
    ([dispatcher tx_stat_registers db e a v]
      (datomic.db/prefetch!
        dispatcher
        (fn fn__13992
          ([]
            (when (datomic.db/tempid? (unchecked-long ^java.lang.Number e))
              (let [start__13414__auto__ (java.lang.System/nanoTime)
                    ret__13415__auto__ (datomic.db/dget (datomic.db/find-avet db a v))]
                (datomic.db/long-add!
                  (:res-pf-ms tx_stat_registers)
                  (- (java.lang.System/nanoTime) start__13414__auto__))
                ret__13415__auto__)))))))
  (deftype
    ProcessExpander
    [db part_reqs arraylist attr_hook_attrs prefetch_dispatcher tx_stat_registers]
    datomic.db.IProcess
    datomic.db.IProcessExpander
    (getData
      [this local_tempids]
      (let [vec__14003 (let [start__13414__auto__ (java.lang.System/nanoTime)
                             ret__13415__auto__ (datomic.db/get-ids db arraylist part_reqs)]
                         (datomic.db/long-add!
                           (:res-tx-ms tx_stat_registers)
                           (- (java.lang.System/nanoTime) start__13414__auto__))
                         ret__13415__auto__)
            ids (nth vec__14003 (unchecked-int 0) nil)
            check_installs? (nth vec__14003 (unchecked-int 1) nil)
            basis (.nextT ^datomic.db.Db db)
            replace_tempid (fn replace_tempid
                             ([v]
                               (or
                                 (^clojure.lang.IFn ids v)
                                 (and
                                   (=
                                     3
                                     (long
                                       (datomic.db/eid->part
                                         (unchecked-long ^java.lang.Number v))))
                                   (long (datomic.db/make-eid 3 basis)))
                                 (let [v (get (set/map-invert local_tempids) v v)]
                                   (error/arg
                                     :db.error/tempid-not-an-entity
                                     (str "tempid '" v "' used only as value in transaction"))))))
            replace_tempids (fn replace_tempids
                              ([d]
                                (let [a (.getA ^datomic.impl.db.IDatum d)
                                      attr (datomic.db/require-attr
                                             db
                                             (java.lang.Integer/valueOf (int a)))
                                      e (.getE ^datomic.impl.db.IDatum d)
                                      v (.getV ^datomic.impl.db.IDatum d)
                                      tempid_v? (and
                                                  (= (.-vtypeid ^datomic.db.Attribute attr) 20)
                                                  (datomic.db/tempid?
                                                    (unchecked-long ^java.lang.Number v)))
                                      tempid_at_index? (fn tempid_at_index_QMARK_
                                                         ([idx elem]
                                                           (and
                                                             (contains?
                                                               (:tupleRefOffsets attr)
                                                               idx)
                                                             elem
                                                             (datomic.db/tempid?
                                                               (unchecked-long
                                                                 ^java.lang.Number elem)))))
                                      tempid_in_v? (and
                                                     (=
                                                       (.-vtypeid ^datomic.db.Attribute attr)
                                                       (datomic.db/system-eid db :db.type/tuple))
                                                     (loop [n 0]
                                                       (if (= n (count v))
                                                         false
                                                         (if (^clojure.lang.IFn tempid_at_index?
                                                               (long n)
                                                               (get v (long n)))
                                                           true
                                                           (recur (inc n))))))]
                                  (if (or (datomic.db/tempid? e) tempid_v? tempid_in_v?)
                                    ((if (.isAssertion ^datomic.impl.db.IDatum d)
                                       datomic.db/asserting-datum
                                       datomic.db/retracting-datum)
                                      (if (datomic.db/tempid? e)
                                        (^clojure.lang.IFn ids (long e))
                                        (long e))
                                      (java.lang.Integer/valueOf (int a))
                                      (cond
                                        tempid_v? (^clojure.lang.IFn replace_tempid v)
                                        tempid_in_v? (into
                                                       []
                                                       (map-indexed
                                                         (fn fn__14018
                                                           ([idx elem]
                                                             (if
                                                               (^clojure.lang.IFn tempid_at_index?
                                                                 idx
                                                                 elem)
                                                               (^clojure.lang.IFn replace_tempid
                                                                 elem)
                                                               elem))))
                                                       v)
                                        :default (do v))
                                      (long (.getT ^datomic.impl.db.IDatum d)))
                                    d))))
            now (java.util.Date.)
            datoms (into
                     []
                     (comp
                       (map replace_tempids)
                       (remove
                         (fn fn__14025
                           ([p1__13999#] (datomic.db/datom-tuple-attrs db p1__13999#))))
                       (datomic.db/for-side-effects
                         (datomic.db/composites-prefetcher
                           prefetch_dispatcher
                           tx_stat_registers
                           db)))
                     arraylist)
            _ (datomic.db/prefetch-redundancy+uniqueness
                prefetch_dispatcher
                tx_stat_registers
                db
                datoms)
            cs (datomic.db/generate-composites db datoms tx_stat_registers)
            _ (datomic.db/prefetch-redundancy+uniqueness
                prefetch_dispatcher
                tx_stat_registers
                db
                cs)
            datoms (into datoms cs)
            datoms (datomic.db/add-tempids-to-errors
                     (fn fn__14027
                       ([] (datomic.db/filter-assess-tx-datoms db check_installs? datoms)))
                     (fn fn__14029 ([] (datomic.db/calc-tempids local_tempids ids))))]
        [(if (datomic.db/has-tx-inst? db now datoms)
           datoms
           (cons
             (datomic.db/asserting-datum
               (datomic.db/make-eid 3 basis)
               50
               (datomic.db/next-valid-inst db now)
               basis)
             datoms))
         ids]))
    (^datomic.db.IProcess inject
      [this procargs ^java.util.Map local_tempids]
      (do
        (let [procid (datomic.db/resolve-procid db (nth procargs (unchecked-int 0)))]
          (if (or (= 1 procid) (= 2 procid))
            (let [e (datomic.db/local-id
                      (nth procargs (unchecked-int 1))
                      db
                      procargs
                      local_tempids)
                  a (nth procargs (unchecked-int 2))
                  attr (datomic.db/require-attr db a)
                  aid (.id ^datomic.db.Attribute attr)
                  v (cond
                      (= (.-vtypeid ^datomic.db.Attribute attr) 20) (datomic.db/local-id
                                                                      (nth
                                                                        procargs
                                                                        (unchecked-int 3))
                                                                      db
                                                                      procargs
                                                                      local_tempids)
                      (:tupleRefOffsets attr) (datomic.db/local-tuple
                                                attr
                                                (nth procargs (unchecked-int 3))
                                                db
                                                procargs
                                                local_tempids)
                      :default (do (nth procargs (unchecked-int 3))))]
              (when (nil? v) (error/arg :db.error/nil-value "nil value"))
              (when (contains? attr_hook_attrs a)
                (.forcePart ^datomic.db.PartitionRequests part_reqs e 0))
              (when (contains? datomic.db/install-attrs a)
                (.forcePart ^datomic.db.PartitionRequests part_reqs v 0))
              (when (= (.-unique ^datomic.db.Attribute attr) 38)
                (datomic.db/long-add! (:res-ct tx_stat_registers) 1)
                (datomic.db/prefetch-identity prefetch_dispatcher tx_stat_registers db e a v))
              (.add
                ^java.util.ArrayList arraylist
                ((if (= procid 1) datomic.db/asserting-datum datomic.db/retracting-datum)
                  e
                  a
                  v
                  (long (.nextT ^datomic.db.Db db)))))
            (let [pfn (.getFn ^datomic.db.Db db procid)
                  inp (datomic.db.ProcessInpoint. db part_reqs this)
                  data (let [start__13414__auto__ (java.lang.System/nanoTime)
                             ret__13415__auto__ (apply pfn db (rest procargs))]
                         (datomic.db/long-add!
                           (:tx-fn-ms tx_stat_registers)
                           (- (java.lang.System/nanoTime) start__13414__auto__))
                         ret__13415__auto__)]
              (reduce
                (fn fn__14001
                  ([p1__13997# p2__13998#]
                    (.inject
                      ^datomic.db.IProcess p1__13997#
                      p2__13998#
                      ^java.util.Map local_tempids)))
                inp
                data))))
        this)))
  (clojure.core/import 'datomic.db.ProcessExpander)
  (defn ->ProcessExpander
    ([db part_reqs arraylist attr_hook_attrs prefetch_dispatcher tx_stat_registers]
      (datomic.db.ProcessExpander.
        db
        part_reqs
        arraylist
        attr_hook_attrs
        prefetch_dispatcher
        tx_stat_registers)))
  (defonce default-prefetch-executor
   (delay
     (when (config/property "datomic.prefetchProbes")
       (let [name "probes"
             nthreads (config/property "datomic.prefetchConcurrency")
             q (java.util.concurrent.LinkedBlockingQueue.)
             factory (thread/daemon-factory name)
             G__14042 (java.util.concurrent.ThreadPoolExecutor.
                        (unchecked-int nthreads)
                        (unchecked-int nthreads)
                        30
                        TimeUnit/SECONDS
                        ^java.util.concurrent.BlockingQueue q
                        ^java.util.concurrent.ThreadFactory factory)]
         (.allowCoreThreadTimeOut
           ^java.util.concurrent.ThreadPoolExecutor G__14042
           (boolean (.booleanValue true)))
         G__14042))))
  (defn get-prefetch-dispatcher
    ([]
      (let [temp__5455__auto__ (deref datomic.db/default-prefetch-executor)]
        (if temp__5455__auto__
          (let [exec temp__5455__auto__
                done_flag (java.util.concurrent.atomic.AtomicBoolean.
                            (boolean (.booleanValue false)))]
            (reify
              datomic.db.PrefetchDispatcher
              (close
                [this]
                (do
                  (.set
                    ^java.util.concurrent.atomic.AtomicBoolean done_flag
                    (boolean (.booleanValue true)))
                  nil))
              (prefetch1
                [this f]
                (do
                  (.execute
                    ^java.util.concurrent.Executor exec
                    (fn fn__14047
                      ([]
                        (when-not (.get ^java.util.concurrent.atomic.AtomicBoolean done_flag)
                          (^clojure.lang.IFn f)))))
                  nil))))
          (reify datomic.db.PrefetchDispatcher (close [this] nil) (prefetch1 [this _] nil))))))
  (defn process-expander
    ([db part_reqs dispatcher tx_stat_registers]
      (datomic.db.ProcessExpander.
        db
        part_reqs
        (java.util.ArrayList.)
        (datomic.db/attr-hook-attr-ids db)
        dispatcher
        tx_stat_registers)))
  (reset-meta!
    #'process-expander
    (assoc
      {:tag datomic.db.IProcessExpander,
       :arglists (clojure.core/list ['db 'part-reqs 'dispatcher 'tx-stat-registers]),
       :column 1}
      :name
      'process-expander
      :ns
      *ns*))
  (defn add-fulltext
    ([db data]
      (let [m_14056 {:event :db/add-fulltext}
            ___8980__auto__ (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.db")]
                              (when (.isDebugEnabled ^org.slf4j.Logger logger)
                                (.debug
                                  ^org.slf4j.Logger logger
                                  (logger/process (assoc m_14056 :phase :begin)))
                                nil)
                              nil)
            start__8981__auto__ (java.lang.System/nanoTime)
            result__8982__auto__ (try
                                   {:returned
                                    (update-in
                                      db
                                      [:memidx :fulltext]
                                      (fn fn__14060
                                        ([previous]
                                          (ftindex/update-fulltext
                                            previous
                                            (filter
                                              (fn fn__14061
                                                ([p1__14055#]
                                                  (datomic.db/fulltext?
                                                    db
                                                    (java.lang.Integer/valueOf
                                                      (int
                                                        (.getA
                                                          ^datomic.impl.db.IDatum p1__14055#))))))
                                              data)))))}
                                   (catch
                                     java.lang.Throwable
                                     t__8983__auto__
                                     {:threw t__8983__auto__}))
            elapsed_14057 (- (java.lang.System/nanoTime) start__8981__auto__)
            msec_14058 (logger/format-as-msec (long elapsed_14057))]
        (monitor/add-stat :DbAddFulltextMsec msec_14058)
        (let [endmsg__8984__auto__ (merge
                                     (assoc m_14056 :msec msec_14058 :phase :end)
                                     (when (:threw result__8982__auto__)
                                       {:threw (class (:threw result__8982__auto__))}))
              logger (org.slf4j.LoggerFactory/getLogger "datomic.db")]
          (when (.isDebugEnabled ^org.slf4j.Logger logger)
            (.debug ^org.slf4j.Logger logger (logger/process endmsg__8984__auto__))
            nil)
          nil)
        (if (contains? result__8982__auto__ :returned)
          (:returned result__8982__auto__)
          (do (throw (:threw result__8982__auto__)) nil)))))
  (defn ensure-pred
    ([s db e spec idmap]
      (let [pred (common/requiring-resolve! s) result (^clojure.lang.IFn pred db e)]
        (when-not (true? result)
          (throw
            (error/arg
              :db.error/entity-pred
              (str
                "Entity "
                (get (set/map-invert idmap) e e)
                " failed pred "
                pred
                " of spec "
                (datomic.db/entity-error-desc db spec))
              #:db.error{:pred-return result}))
          nil))))
  (reset-meta!
    #'ensure-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['s 'db 'e 'spec 'idmap]), :column 1}
      :name
      'ensure-pred
      :ns
      *ns*))
  (defn missing-attrs
    ([entity required] (remove (fn fn__14072 ([p1__14071#] (find entity p1__14071#))) required)))
  (reset-meta!
    #'missing-attrs
    (assoc
      {:private true, :arglists (clojure.core/list ['entity 'required]), :column 1}
      :name
      'missing-attrs
      :ns
      *ns*))
  (defn ensure-entity!
    ([db_before db_after e spec idmap]
      (let [spec_ent (.entity ^datomic.Database db_before spec)
            required (:db.entity/attrs spec_ent)
            preds (:db.entity/preds spec_ent)
            entity (.entity ^datomic.Database db_after e)
            missing (datomic.db/missing-attrs entity required)]
        (when (seq missing)
          (error/arg
            :db.error/entity-attr
            (str
              "Entity "
              (get (set/map-invert idmap) e e)
              " missing attributes "
              missing
              " of spec "
              (datomic.db/entity-error-desc db_before spec))))
        (when (seq preds)
          (loop [seq_14075 (seq preds) chunk_14076 nil count_14077 0 i_14078 0]
            (if (< i_14078 count_14077)
              (let [pred (.nth ^clojure.lang.Indexed chunk_14076 (unchecked-int i_14078))]
                (datomic.db/ensure-pred pred db_after e spec idmap)
                (recur seq_14075 chunk_14076 count_14077 (inc i_14078)))
              (let [temp__5457__auto__ (seq seq_14075)]
                (when temp__5457__auto__
                  (let [seq_14075 temp__5457__auto__]
                    (if (chunked-seq? seq_14075)
                      (let [c__5719__auto__ (chunk-first seq_14075)]
                        (recur (chunk-rest seq_14075) c__5719__auto__ (count c__5719__auto__) 0))
                      (let [pred (first seq_14075)]
                        (datomic.db/ensure-pred pred db_after e spec idmap)
                        (recur (next seq_14075) nil 0 0))))))))
          nil))))
  (defn ensure-tx
    ([db_before db_after data idmap]
      (loop [seq_14082 (seq data) chunk_14083 nil count_14084 0 i_14085 0]
        (if (< i_14085 count_14084)
          (let [d (.nth ^clojure.lang.Indexed chunk_14083 (unchecked-int i_14085))]
            (cond
              (datomic.db/ensure-datom? db_before d) (datomic.db/ensure-entity!
                                                       db_before
                                                       db_after
                                                       (long (.getE ^datomic.impl.db.IDatum d))
                                                       (.getV ^datomic.impl.db.IDatum d)
                                                       idmap)
              (.isAssertion ^datomic.impl.db.IDatum d) (do
                                                         (let [attr
                                                               (.elementAt
                                                                 ^datomic.db.IDbImpl db_before
                                                                 (java.lang.Integer/valueOf
                                                                   (int
                                                                     (.getA
                                                                       ^datomic.impl.db.IDatum d))))
                                                               temp__5457__auto__
                                                               (some-> attr (:attrPred) (deref))]
                                                           (when
                                                             temp__5457__auto__
                                                             (let 
                                                               [pred temp__5457__auto__]
                                                               (^clojure.lang.IFn pred
                                                                 (long
                                                                   (.getE
                                                                     ^datomic.impl.db.IDatum d))
                                                                 (.getV ^datomic.impl.db.IDatum d)
                                                                 idmap))))))
            (recur seq_14082 chunk_14083 count_14084 (inc i_14085)))
          (let [temp__5457__auto__ (seq seq_14082)]
            (when temp__5457__auto__
              (let [seq_14082 temp__5457__auto__]
                (if (chunked-seq? seq_14082)
                  (let [c__5719__auto__ (chunk-first seq_14082)]
                    (recur (chunk-rest seq_14082) c__5719__auto__ (count c__5719__auto__) 0))
                  (let [d (first seq_14082)]
                    (cond
                      (datomic.db/ensure-datom? db_before d) (datomic.db/ensure-entity!
                                                               db_before
                                                               db_after
                                                               (long
                                                                 (.getE ^datomic.impl.db.IDatum d))
                                                               (.getV ^datomic.impl.db.IDatum d)
                                                               idmap)
                      (.isAssertion ^datomic.impl.db.IDatum d) (do
                                                                 (let 
                                                                   [attr
                                                                    (.elementAt
                                                                      ^datomic.db.IDbImpl db_before
                                                                      (java.lang.Integer/valueOf
                                                                        (int
                                                                          (.getA
                                                                            ^datomic.impl.db.IDatum d))))
                                                                    temp__5457__auto__
                                                                    (some->
                                                                      attr
                                                                      (:attrPred)
                                                                      (deref))]
                                                                   (when
                                                                     temp__5457__auto__
                                                                     (let 
                                                                       [pred temp__5457__auto__]
                                                                       (^clojure.lang.IFn pred
                                                                         (long
                                                                           (.getE
                                                                             ^datomic.impl.db.IDatum d))
                                                                         (.getV
                                                                           ^datomic.impl.db.IDatum d)
                                                                         idmap))))))
                    (recur (next seq_14082) nil 0 0))))))))))
  (reset-meta!
    #'ensure-tx
    (assoc
      {:private true,
       :arglists
       (clojure.core/list [(.withMeta 'db-before {:tag 'IDbImpl}) 'db-after 'data 'idmap]),
       :column 1}
      :name
      'ensure-tx
      :ns
      *ns*))
  (defn add-ensured-data
    ([db_before data added idmap tx_stat_registers]
      (let [db_after (.addData
                       ^datomic.db.IDbImpl db_before
                       (remove
                         (fn fn__14094
                           ([p1__14093#] (datomic.db/ensure-datom? db_before p1__14093#)))
                         data)
                       ^java.util.ArrayList added
                       tx_stat_registers)]
        (datomic.db/long-add! (:considered-datoms tx_stat_registers) (count data))
        (datomic.db/ensure-tx db_before db_after data idmap)
        db_after)))
  (reset-meta!
    #'add-ensured-data
    (assoc
      {:private true,
       :arglists
       (clojure.core/list
         [(.withMeta 'db-before {:tag 'IDbImpl}) 'data 'added 'idmap 'tx-stat-registers]),
       :column 1}
      :name
      'add-ensured-data
      :ns
      *ns*))
  (defn with-tx
    ([db dispatcher txdata]
      (let [added (java.util.ArrayList.)
            local_tempids (java.util.HashMap.)
            part_reqs (datomic.db/part-requests)
            inject_all (fn inject_all
                         ([tx procargs]
                           (reduce
                             (fn fn__14100
                               ([p1__14097# p2__14098#]
                                 (.inject
                                   ^datomic.db.IProcess p1__14097#
                                   p2__14098#
                                   ^java.util.Map local_tempids)))
                             tx
                             procargs)))
            tx_stat_registers (datomic.db/empty-tx-stat-registers)
            pe (datomic.db/process-expander db part_reqs dispatcher tx_stat_registers)
            pi (datomic.db.ProcessInpoint. db part_reqs pe)]
        (^clojure.lang.IFn inject_all pi txdata)
        (let [vec__14103 (.getData ^datomic.db.IProcessExpander pe local_tempids)
              data (nth vec__14103 (unchecked-int 0) nil)
              idmap (nth vec__14103 (unchecked-int 1) nil)
              tempids (datomic.db/calc-tempids local_tempids idmap)
              newdb (datomic.db/add-fulltext
                      (datomic.db/add-ensured-data db data added tempids tx_stat_registers)
                      added)]
          (datomic.db/pf-close! dispatcher)
          {:db-before db,
           :db-after newdb,
           :tx-data added,
           :tempids tempids,
           :tx-stats (datomic.db/summarize-tx-stats tx_stat_registers)}))))
  (defn with-tx+opts
    ([db txdata p__14107]
      (let [map__14108 p__14107
            map__14108 (if (seq? map__14108)
                         (clojure.lang.PersistentHashMap/create (seq map__14108))
                         map__14108)
            return_hints (get map__14108 :return-hints)
            io_context (get map__14108 :io-context)
            f (fn f ([] (datomic.db/with-tx db (datomic.db/get-prefetch-dispatcher) txdata)))
            f (if return_hints
                (fn fn__14111
                  ([]
                    (let [vec__14112 (io-trace/tracing-keys f)
                          ret (nth vec__14112 (unchecked-int 0) nil)
                          segments (nth vec__14112 (unchecked-int 1) nil)]
                      (update ret :hints merge {:segments segments}))))
                f)
            ret (if (or return_hints io_context)
                  (let [ctx {:io-context (or io_context :datomic.db/trace), :api :with}
                        map__14116 (io-stats/throw-if-ex! (io-stats/with-io-stats f ctx))
                        map__14116 (if (seq? map__14116)
                                     (clojure.lang.PersistentHashMap/create (seq map__14116))
                                     map__14116)
                        ret (get map__14116 :ret)
                        io_stats (get map__14116 :io-stats)]
                    (assoc ret :io-stats io_stats))
                  (^clojure.lang.IFn f))]
        (dissoc ret :tx-stats))))
  (def bootstrap-data
   [[:db.part/db :db.install/partition :db.part/db]
    [:db/ident :db/valueType :db.type/keyword]
    [:db/ident :db/cardinality :db.cardinality/one]
    [:db/ident :db/unique :db.unique/identity]
    [:db.part/db :db.install/attribute :db/ident]
    [:db.install/partition :db/valueType :db.type/ref]
    [:db.install/partition :db/cardinality :db.cardinality/many]
    [:db.part/db :db.install/attribute :db.install/partition]
    [:db.install/valueType :db/valueType :db.type/ref]
    [:db.install/valueType :db/cardinality :db.cardinality/many]
    [:db.part/db :db.install/attribute :db.install/valueType]
    [:db.install/attribute :db/valueType :db.type/ref]
    [:db.install/attribute :db/cardinality :db.cardinality/many]
    [:db.part/db :db.install/attribute :db.install/attribute]
    [:db.install/function :db/valueType :db.type/ref]
    [:db.install/function :db/cardinality :db.cardinality/many]
    [:db.part/db :db.install/attribute :db.install/function]
    [:db/valueType :db/valueType :db.type/ref]
    [:db/valueType :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/valueType]
    [:db/cardinality :db/valueType :db.type/ref]
    [:db/cardinality :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/cardinality]
    [:db/unique :db/valueType :db.type/ref]
    [:db/unique :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/unique]
    [:db/isComponent :db/valueType :db.type/boolean]
    [:db/isComponent :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/isComponent]
    [:db/index :db/valueType :db.type/boolean]
    [:db/index :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/index]
    [:db/fulltext :db/valueType :db.type/boolean]
    [:db/fulltext :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/fulltext]
    [:db/noHistory :db/valueType :db.type/boolean]
    [:db/noHistory :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/noHistory]
    [:fressian/tag :db/valueType :db.type/keyword]
    [:fressian/tag :db/cardinality :db.cardinality/one]
    [:fressian/tag :db/index true]
    [:db.part/db :db.install/attribute :fressian/tag]
    [:db/lang :db/valueType :db.type/ref]
    [:db/lang :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/lang]
    [:db/code :db/valueType :db.type/string]
    [:db/code :db/cardinality :db.cardinality/one]
    [:db/code :db/fulltext true]
    [:db.part/db :db.install/attribute :db/code]
    [:db.type/long :fressian/tag :int]
    [:db.part/db :db.install/valueType :db.type/long]
    [:db.type/ref :fressian/tag :ref]
    [:db.part/db :db.install/valueType :db.type/ref]
    [:db.type/keyword :fressian/tag :key]
    [:db.part/db :db.install/valueType :db.type/keyword]
    [:db.type/string :fressian/tag :string]
    [:db.part/db :db.install/valueType :db.type/string]
    [:db.type/boolean :fressian/tag :bool]
    [:db.part/db :db.install/valueType :db.type/boolean]
    [:db.type/instant :fressian/tag :inst]
    [:db.part/db :db.install/valueType :db.type/instant]
    [:db/txInstant :db/valueType :db.type/instant]
    [:db/txInstant :db/cardinality :db.cardinality/one]
    [:db/txInstant :db/index true]
    [:db.part/db :db.install/attribute :db/txInstant]
    [:db.type/fn :fressian/tag :datomic/fn]
    [:db.part/db :db.install/valueType :db.type/fn]
    [:db.type/bytes :fressian/tag :bytes]
    [:db.part/db :db.install/valueType :db.type/bytes]
    [:db/fn :db/valueType :db.type/fn]
    [:db/fn :db/cardinality :db.cardinality/one]
    [:db.part/db :db.install/attribute :db/fn]])
  (def system-data-fns
   [{:db/ident :db.fn/retractEntity,
     :db/id (DbId/create {:idx -1000002, :part :db.part/db}),
     :db.install/_function :db.part/db,
     :db/lang :db.lang/clojure,
     :db/code "(clojure.core/fn [db e] (datomic.builtins/build-retract-args db e))"}
    {:db/ident :db.fn/cas,
     :db/id (DbId/create {:idx -1000003, :part :db.part/db}),
     :db.install/_function :db.part/db,
     :db/lang :db.lang/clojure,
     :db/code
     "(clojure.core/fn [db e a ov nv] (datomic.builtins/compare-and-swap db e a ov nv))"}])
  (def db-docs
   [[:db/cardinality
     "Property of an attribute. Two possible values: :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes. Defaults to :db.cardinality/one."]
    [:db/valueType
     "Property of an attribute that specifies the attribute's value type. Built-in value types include, :db.type/keyword, :db.type/string, :db.type/ref, :db.type/instant, :db.type/long, :db.type/bigdec, :db.type/boolean, :db.type/float, :db.type/uuid, :db.type/double, :db.type/bigint,  :db.type/uri."]
    [:db/isComponent
     "Property of attribute whose vtype is :db.type/ref. If true, then the attribute is a component of the entity referencing it. When you query for an entire entity, components are fetched automatically. Defaults to nil."]
    [:db/unique
     "Property of an attribute. If value is :db.unique/value, then attribute value is unique to each entity. Attempts to insert a duplicate value for a temporary entity id will fail. If value is :db.unique/identity, then attribute value is unique, and upsert is enabled. Attempting to insert a duplicate value for a temporary entity id will cause all attributes associated with that temporary id to be merged with the entity already in the database. Defaults to nil."]
    [:db/index
     "Property of an attribute. If true, create an AVET index for the attribute. Defaults to false."]
    [:db/fulltext
     "Property of an attribute. If true, create a fulltext search index for the attribute. Defaults to false."]
    [:db/noHistory
     "Property of an attribute. If true, past values of the attribute are not retained after indexing. Defaults to false."]
    [:db/lang
     "Attribute of a data function. Value is a keyword naming the implementation language of the function. Legal values are :db.lang/java and :db.lang/clojure"]
    [:db.lang/clojure
     "Value of :db/lang attribute, specifying that a data function is implemented in Clojure."]
    [:db.lang/java
     "Value of :db/lang attribute, specifying that a data function is implemented in Java."]
    [:db.part/db
     "Name of the system partition. The system partition includes the core of datomic, as well as user schemas: type definitions, attribute definitions, partition definitions, and data function definitions."]
    [:db.part/user
     "Name of the user partition. The user partition is analogous to the default namespace in a programming language, and should be used as a temporary home for data during interactive development."]
    [:db/add
     "Primitive assertion. All transactions eventually reduce to a collection of primitive assertions and retractions of facts, e.g. [:db/add fred :age 42]."]
    [:db/retract
     "Primitive retraction. All transactions eventually reduce to a collection of assertions and retractions of facts, e.g. [:db/retract fred :age 42]."]
    [:db/doc "Documentation string for an entity."]
    [:db/ident "Attribute used to uniquely name an entity."]
    [:db.type/instant
     "Value type for instants in time. Stored internally as a number of milliseconds since midnight, January 1, 1970 UTC. Representation type will vary depending on the language you are using."]
    [:db/txInstant
     "Attribute whose value is a :db.type/instant. A :db/txInstant is recorded automatically with every transaction."]
    [:db.part/tx
     "Partition used to store data about transactions. Transaction data always includes a :db/txInstant which is the transaction's timestamp, and can be extended to store other information at transaction granularity."]
    [:db.type/long
     "Fixed integer value type. Same semantics as a Java long: 64 bits wide, two's complement binary representation."]
    [:db.type/float
     "Floating point value type. Same semantics as a Java float: single-precision 32-bit IEEE 754 floating point."]
    [:db.type/double
     "Floating point value type. Same semantics as a Java double: double-precision 64-bit IEEE 754 floating point."]
    [:db.unique/value
     "Specifies that an attribute's value is unique. Attempts to create a new entity with a colliding value for a :db.unique/value will fail."]
    [:db.unique/identity
     "Specifies that an attribute's value is unique. Attempts to create a new entity with a colliding value for a :db.unique/value will become upserts."]
    [:db.install/function
     "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a data function."]
    [:db.install/partition
     "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a partition."]
    [:db.install/valueType
     "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as a value type."]
    [:db.install/attribute
     "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will install v as an attribute."]
    [:db.alter/attribute
     "System attribute with type :db.type/ref. Asserting this attribute on :db.part/db with value v will alter the definition of existing attribute v."]
    [:db.cardinality/one
     "One of two legal values for the :db/cardinality attribute. Specify :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes."]
    [:db.cardinality/many
     "One of two legal values for the :db/cardinality attribute. Specify :db.cardinality/one for single-valued attributes, and :db.cardinality/many for many-valued attributes."]
    [:db.type/bigdec
     "Value type for arbitrary precision floating point numbers. Maps to java.math.BigDecimal on the JVM."]
    [:db.type/bigint
     "Value type for arbitrary precision integers. Maps to java.math.BigInteger on the JVM."]
    [:db.type/boolean "Boolean value type."]
    [:db.type/ref
     "Value type for references. All references from one entity to another are through attributes with this value type."]
    [:db.type/uri "Value type for URIs. Maps to java.net.URI on the JVM."]
    [:db.type/uuid "Value type for UUIDs. Maps to java.util.UUID on the JVM."]
    [:db.type/bytes "Value type for small binaries. Maps to byte array on the JVM."]
    [:db.type/fn "Value type for database functions. See Javadoc for Peer.function."]
    [:db/fn "A function-valued attribute for direct use by transactions and queries."]
    [:fressian/tag
     "Keyword-valued attribute of a value type that specifies the underlying fressian type used for serialization."]
    [:db/code
     "String-valued attribute of a data function that contains the function's source code."]
    [:db.type/keyword
     "Value type for keywords. Keywords are used as names, and are interned for efficiency. Keywords map to the native interned-name type in languages that support them."]
    [:db.type/string "Value type for strings."]
    [:db.fn/retractEntity
     "Retract all facts about an entity, including references from other entities and component attributes recursively."]
    [:db.fn/cas "Compare and swap the value of an entity's attribute."]
    [:db.sys/reId
     "System-assigned attribute for an id e in the log that has been changed to id v in the index"]
    [:db.sys/partiallyIndexed
     "System-assigned attribute set to true for transactions not fully incorporated into the index"]])
  (def add-fn-src
   (seq
     (concat
       (clojure.core/list 'clojure.core/fn)
       (clojure.core/list
         (apply
           vector
           (seq
             (concat
               (clojure.core/list 'db__14120__auto__)
               (clojure.core/list 'm__14121__auto__)))))
       (clojure.core/list
         (seq
           (concat
             (clojure.core/list 'clojure.core/let)
             (clojure.core/list
               (apply
                 vector
                 (seq
                   (concat
                     (clojure.core/list
                       (apply
                         hash-map
                         (seq
                           (concat
                             (clojure.core/list 'key__14122__auto__)
                             (clojure.core/list :db/ident)
                             (clojure.core/list 'lang__14123__auto__)
                             (clojure.core/list :db/lang)
                             (clojure.core/list 'code__14124__auto__)
                             (clojure.core/list :db/code)))))
                     (clojure.core/list 'm__14121__auto__)
                     (clojure.core/list 'id__14125__auto__)
                     (clojure.core/list
                       (seq
                         (concat
                           (clojure.core/list 'datomic.db/tempid)
                           (clojure.core/list 0))))))))
             (clojure.core/list
               (apply
                 vector
                 (seq
                   (concat
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list :db/add)
                             (clojure.core/list 'id__14125__auto__)
                             (clojure.core/list :db/ident)
                             (clojure.core/list 'key__14122__auto__)))))
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list :db/add)
                             (clojure.core/list 'id__14125__auto__)
                             (clojure.core/list :db/lang)
                             (clojure.core/list 'lang__14123__auto__)))))
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list :db/add)
                             (clojure.core/list 'id__14125__auto__)
                             (clojure.core/list :db/code)
                             (clojure.core/list 'code__14124__auto__)))))
                     (clojure.core/list
                       (apply
                         vector
                         (seq
                           (concat
                             (clojure.core/list :db/add)
                             (clojure.core/list :db.part/db)
                             (clojure.core/list :db.install/function)
                             (clojure.core/list 'id__14125__auto__)))))))))))))))
  (def bootstrap-txes
   [datomic.db/system-data-fns
    [{:db.install/_valueType :db.part/db,
      :db/ident :db.type/uuid,
      :fressian/tag :uuid,
      :db/id (DbId/create {:idx -1022, :part :db.part/db})}
     {:db.install/_valueType :db.part/db,
      :db/ident :db.type/double,
      :fressian/tag :double,
      :db/id (DbId/create {:idx -1023, :part :db.part/db})}
     {:db.install/_valueType :db.part/db,
      :db/ident :db.type/float,
      :fressian/tag :float,
      :db/id (DbId/create {:idx -1024, :part :db.part/db})}
     {:db.install/_valueType :db.part/db,
      :db/ident :db.type/uri,
      :fressian/tag :uri,
      :db/id (DbId/create {:idx -1025, :part :db.part/db})}
     {:db.install/_valueType :db.part/db,
      :db/ident :db.type/bigint,
      :fressian/tag :bigint,
      :db/id (DbId/create {:idx -1026, :part :db.part/db})}
     {:db.install/_valueType :db.part/db,
      :db/ident :db.type/bigdec,
      :fressian/tag :bigdec,
      :db/id (DbId/create {:idx -1027, :part :db.part/db})}
     {:db/cardinality :db.cardinality/one,
      :db.install/_attribute :db.part/db,
      :db/fulltext true,
      :db/valueType :db.type/string,
      :db/ident :db/doc,
      :db/id (DbId/create {:idx -1028, :part :db.part/db})}]
    (map
      (fn fn__14127
        ([p__14126]
          (let [vec__14128 p__14126
                k (nth vec__14128 (unchecked-int 0) nil)
                v (nth vec__14128 (unchecked-int 1) nil)]
            [:db/add k :db/doc v])))
      datomic.db/db-docs)
    [{:db/ident :db.type/tuple, :fressian/tag :list, :db.install/_valueType :db.part/db}
     {:db/ident :db.type/symbol, :fressian/tag :sym, :db.install/_valueType :db.part/db}
     #:db{:ident :db/tupleType, :valueType :db.type/keyword, :cardinality :db.cardinality/one}]
    [#:db{:ident :db/tupleTypes,
          :valueType :db.type/tuple,
          :tupleType :db.type/keyword,
          :cardinality :db.cardinality/one}
     #:db{:ident :db/tupleAttrs,
          :valueType :db.type/tuple,
          :tupleType :db.type/keyword,
          :cardinality :db.cardinality/one}
     #:db{:ident :db/ensure, :valueType :db.type/ref, :cardinality :db.cardinality/many}
     #:db{:ident :db.entity/attrs, :valueType :db.type/keyword, :cardinality :db.cardinality/many}
     #:db{:ident :db.entity/preds, :valueType :db.type/symbol, :cardinality :db.cardinality/many}
     #:db{:ident :db.attr/preds, :valueType :db.type/symbol, :cardinality :db.cardinality/many}]])
  (def MIN_SCHEMA_LEVEL 3)
  (def MAX_SCHEMA_LEVEL (java.lang.Integer/valueOf (int (count datomic.db/bootstrap-txes))))
  (defn tx-data-for-latest-schema-level
    ([db]
      (map
        (fn fn__14133 ([p1__14132#] (conj p1__14132# #:db{:id "datomic.tx", :system-tx :schema})))
        (drop (:schema-level db) datomic.db/bootstrap-txes))))
  (def schema-level-fns #:datomic.db{TUPLE_SCHEMA_LEVEL datomic.db/add-system-eids})
  (defn on-schema-level
    ([db level]
      (let [temp__5455__auto__ (get datomic.db/schema-level-fns level)]
        (if temp__5455__auto__ (let [f temp__5455__auto__] (^clojure.lang.IFn f db)) db))))
  (defn update-schema-level
    ([db]
      (loop [level (get db :schema-level datomic.db/MIN_SCHEMA_LEVEL) db db]
        (if (= level (long (count datomic.db/bootstrap-txes)))
          (assoc db :schema-level level)
          (let [kw (:db/ident
                     (first
                       (nth datomic.db/bootstrap-txes (unchecked-int ^java.lang.Number level))))]
            (when-not kw (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'kw)))))
            (if (.entid ^datomic.Database db kw)
              (let [nlevel (inc level)] (recur nlevel (datomic.db/on-schema-level db nlevel)))
              (assoc db :schema-level level)))))))
  (defn init-db
    ([id]
      (deref datomic.db/load-builtins)
      (let [assert_kw (fn assert_kw
                        ([p__14139]
                          (let [vec__14141 p__14139
                                k (nth vec__14141 (unchecked-int 0) nil)
                                id (nth vec__14141 (unchecked-int 1) nil)]
                            (datomic.db/asserting-datum
                              (unchecked-long ^java.lang.Number id)
                              10
                              (datomic.db/require-kw k)
                              0))))
            db (datomic.db.Db.
                 id
                 datomic.db/mem-index-set
                 nil
                 nil
                 nil
                 nil
                 (datomic.db/memlog)
                 0
                 0
                 0
                 nil
                 []
                 {}
                 {}
                 nil
                 0
                 nil
                 nil
                 nil
                 nil)
            data (map assert_kw datomic.db/BOOT-IDS)]
        (assoc
          (datomic.db/add-fulltext (.acceptData ^datomic.db.Db db data) data)
          :schema-level
          datomic.db/MAX_SCHEMA_LEVEL
          :birth-level
          datomic.db/MAX_SCHEMA_LEVEL))))
  (reset-meta!
    #'init-db
    (assoc
      {:private true, :arglists (clojure.core/list ['id]), :column 1}
      :name
      'init-db
      :ns
      *ns*))
  (defn bootstrap-db*
    ([id]
      (let [db (datomic.db/init-db id)
            ad (fn ad
                 ([p__14148]
                   (let [vec__14150 p__14148
                         e (nth vec__14150 (unchecked-int 0) nil)
                         a (nth vec__14150 (unchecked-int 1) nil)
                         v (nth vec__14150 (unchecked-int 2) nil)]
                     (datomic.db/asserting-datum
                       (unchecked-long (datomic.db/resolve-id db e))
                       (unchecked-long (datomic.db/resolve-id db a))
                       (datomic.db/bootstrap-maybe-resolve db a v)
                       (.nextT ^datomic.db.Db db)))))
            data (map ad datomic.db/bootstrap-data)
            db (datomic.db/add-fulltext (.acceptDataCheck ^datomic.db.Db db data false) data)
            epoch (java.util.Date. 0)
            db (reduce
                 (fn fn__14154
                   ([p1__14146# p2__14147#]
                     (:db-after
                       (.with
                         ^datomic.Database p1__14146#
                         (conj
                           p2__14147#
                           #:db{:id (DbId/create {:idx -1000004, :part :db.part/tx}),
                                :txInstant epoch})))))
                 db
                 datomic.db/bootstrap-txes)
            db (datomic.db/run-hooks db (:memidx db))
            db (datomic.db/finish-init db)
            midx (:memidx db)
            avet (reduce
                   (fn fn__14156
                     ([ret d]
                       (let [a (.getA ^datomic.impl.db.IDatum d)
                             attr (.elementAt
                                    ^datomic.db.IDbImpl db
                                    (java.lang.Integer/valueOf (int a)))]
                         (if (.-needsAVET ^datomic.db.Attribute attr) (conj ret d) ret))))
                   (:avet midx)
                   (seq (:aevt midx)))
            raet (reduce
                   (fn fn__14158
                     ([ret d]
                       (let [a (.getA ^datomic.impl.db.IDatum d)
                             attr (.elementAt
                                    ^datomic.db.IDbImpl db
                                    (java.lang.Integer/valueOf (int a)))]
                         (if (= (.-vtypeid ^datomic.db.Attribute attr) 20) (conj ret d) ret))))
                   (:raet midx)
                   (seq (:aevt midx)))]
        (assoc db :memidx (assoc midx :avet avet :raet raet) :nextT 1000)))
    ([] (datomic.db/bootstrap-db* "bootstrap")))
  (reset-meta!
    #'bootstrap-db*
    (assoc
      {:private true, :arglists (clojure.core/list [] ['id]), :column 1}
      :name
      'bootstrap-db*
      :ns
      *ns*))
  (def base-bootstrap-db (delay (datomic.db/bootstrap-db*)))
  (reset-meta!
    #'base-bootstrap-db
    (assoc {:private true, :column 1} :name 'base-bootstrap-db :ns *ns*))
  (defn bootstrap-db
    ([id] (assoc (deref datomic.db/base-bootstrap-db) :id id))
    ([] (datomic.db/bootstrap-db (str (common/squuid)))))
  (defn pretty-datum
    ([db d]
      (let [attr (.elementAt
                   ^datomic.db.Db db
                   (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))))]
        {:p
         (.keywordOf
           ^datomic.db.Db db
           (long (datomic.db/eid->part (.getE ^datomic.impl.db.IDatum d)))),
         :e
         (or
           (.keywordOf ^datomic.db.Db db (long (.getE ^datomic.impl.db.IDatum d)))
           (long (datomic.db/eid->eidx (.getE ^datomic.impl.db.IDatum d)))),
         :a
         (.keywordOf
           ^datomic.db.Db db
           (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))),
         :v
         (if (= (.-vtypeid ^datomic.db.Attribute attr) (.idOf ^datomic.db.Db db :db.type/ref))
           (or
             (.keywordOf ^datomic.db.Db db (.getV ^datomic.impl.db.IDatum d))
             (.getV ^datomic.impl.db.IDatum d))
           (.getV ^datomic.impl.db.IDatum d)),
         :t (long (.getT ^datomic.impl.db.IDatum d)),
         :tx (long (.getTx ^datomic.impl.db.IDatum d)),
         :op (.isAssertion ^datomic.impl.db.IDatum d)})))
  (defn t->tx (^long [^long t] (datomic.db/make-eid 3 t)))
  (defn accept-data-no-check ([db tx] (.acceptDataCheck ^datomic.db.IDbImpl db tx false)))
  (defn memlog-txes-since
    ([db t]
      (let [memlog (get-in db [:memlog :txes])
            idx (java.util.Collections/binarySearch
                  ^java.util.List memlog
                  {:t t}
                  (common/key-comparator :t))]
        (subvec memlog (if (< idx 0) (long (- (inc idx))) (long (inc idx)))))))
  (defn accept-txes
    ([db txes]
      (datomic.db/add-fulltext
        (transduce (map :data) (completing datomic.db/accept-data-no-check) db txes)
        (mapcat :data txes)))))