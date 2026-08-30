(do
  (clojure.core/in-ns 'datomic.tools.log-tools)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.spec.alpha :as 's]
        ['datomic.common :as 'common]
        ['datomic.config :as 'config]
        ['datomic.db :as 'db]
        ['datomic.iter :as 'iter]
        ['datomic.log :as 'log]
        ['datomic.log.specs :as 'logspecs]
        ['datomic.slf4j :as 'logger])))
  (when-not (.equals 'datomic.tools.log-tools 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.log-tools))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.spec.alpha :as 's]
          ['datomic.common :as 'common]
          ['datomic.config :as 'config]
          ['datomic.db :as 'db]
          ['datomic.iter :as 'iter]
          ['datomic.log :as 'log]
          ['datomic.log.specs :as 'logspecs]
          ['datomic.slf4j :as 'logger]))))
  (s/def-impl :datomic.tools.log-tools/desc :datomic.log/desc :datomic.log/desc)
  (s/def-impl :datomic.tools.log-tools/newdesc :datomic.log/desc :datomic.log/desc)
  (s/def-impl
    :datomic.tools.log-tools/transform-root-result
    (clojure.core/list
      'clojure.spec.alpha/nilable
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req-un
        [:datomic.tools.log-tools/desc :datomic.tools.log-tools/newdesc]))
    (s/nilable-impl
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req-un
        [:datomic.tools.log-tools/desc :datomic.tools.log-tools/newdesc])
      (s/map-spec-impl
        {:req-un [:datomic.tools.log-tools/desc :datomic.tools.log-tools/newdesc],
         :opt-un nil,
         :gfn nil,
         :pred-exprs
         [(fn fn__30057 ([G__30056] (map? G__30056)))
          (fn fn__30059 ([G__30056] (contains? G__30056 :desc)))
          (fn fn__30061 ([G__30056] (contains? G__30056 :newdesc)))],
         :keys-pred
         (fn fn__30063
           ([G__30056]
             (and (map? G__30056) (contains? G__30056 :desc) (contains? G__30056 :newdesc)))),
         :opt-keys [],
         :req-specs [:datomic.tools.log-tools/desc :datomic.tools.log-tools/newdesc],
         :req nil,
         :req-keys [:desc :newdesc],
         :opt-specs [],
         :pred-forms
         [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
          (clojure.core/list
            'clojure.core/fn
            ['%]
            (clojure.core/list 'clojure.core/contains? '% :desc))
          (clojure.core/list
            'clojure.core/fn
            ['%]
            (clojure.core/list 'clojure.core/contains? '% :newdesc))],
         :opt nil})
      nil))
  (s/def-impl :datomic.tools.log-tools/retries 'clojure.core/nat-int? nat-int?)
  (s/def-impl
    'datomic.tools.log-tools/fake-descriptor
    (clojure.core/list 'clojure.spec.alpha/fspec :ret :datomic.tools.log-tools/desc)
    (s/fspec-impl
      nil
      nil
      (s/spec-impl :datomic.tools.log-tools/desc :datomic.tools.log-tools/desc nil nil)
      :datomic.tools.log-tools/desc
      nil
      nil
      nil))
  (def fake-descriptor
   (fn fake_descriptor
     ([root_id]
       {:rev 0,
        :etag "fake",
        :d/l 3,
        :d/r root_id,
        :d/v (config/property "datomic.versionUnique")})))
  (reset-meta!
    #'fake-descriptor
    (assoc
      {:private true, :arglists (clojure.core/list ['root-id]), :column (int 1)}
      :name
      'fake-descriptor
      :ns
      *ns*))
  (defn first-leaf
    ([log]
      (let [root (log/get-root-val log) dir (get (:olookup log) (:uuid (first root)))]
        (get (:olookup log) (:uuid (first dir))))))
  (reset-meta!
    #'first-leaf
    (assoc {:arglists (clojure.core/list ['log]), :column (int 1)} :name 'first-leaf :ns *ns*))
  (defn last-leaf
    ([log]
      (let [root (log/get-root-val log) dir (get (:olookup log) (:uuid (last root)))]
        (get (:olookup log) (:uuid (last dir))))))
  (reset-meta!
    #'last-leaf
    (assoc {:arglists (clojure.core/list ['log]), :column (int 1)} :name 'last-leaf :ns *ns*))
  (s/def-impl
    :datomic.tools.log-tools/t-range
    (clojure.core/list 'clojure.spec.alpha/tuple :datomic.log/t :datomic.log/t)
    (s/tuple-impl [:datomic.log/t :datomic.log/t] [:datomic.log/t :datomic.log/t]))
  (s/def-impl
    'datomic.tools.log-tools/t-range
    (clojure.core/list 'clojure.spec.alpha/fspec :ret :datomic.tools.log-tools/t-range)
    (s/fspec-impl
      nil
      nil
      (s/spec-impl :datomic.tools.log-tools/t-range :datomic.tools.log-tools/t-range nil nil)
      :datomic.tools.log-tools/t-range
      nil
      nil
      nil))
  (defn t-range
    ([log] [(:t (first (first-leaf log))) (inc (log/max-eidx (:data (last (last-leaf log)))))]))
  (reset-meta!
    #'t-range
    (assoc {:arglists (clojure.core/list ['log]), :column (int 1)} :name 't-range :ns *ns*))
  (def create-tree-log
   (fn create_tree_log
     ([olookup root_id]
       (when-not root_id
         (throw (java.lang.AssertionError. (str "Assert failed: " (pr-str 'root-id)))))
       (log/->LogImpl olookup (fake-descriptor root_id) (log/empty-tail)))))
  (reset-meta!
    #'create-tree-log
    (assoc
      {:arglists (clojure.core/list ['olookup 'root-id]), :column (int 1)}
      :name
      'create-tree-log
      :ns
      *ns*))
  (defn write*
    ([cs id val]
      (when-not (= :created (deref (log/zip-and-create cs id val)))
        (throw (java.lang.RuntimeException. "Unable to write to storage"))
        nil)))
  (reset-meta!
    #'write*
    (assoc {:arglists (clojure.core/list ['cs 'id 'val]), :column (int 1)} :name 'write* :ns *ns*))
  (def truncate-log
   (fn truncate_log
     ([cr basis_root_id t]
       (when (< 1000 t)
         (let [keep? (fn keep_QMARK_ ([p1__30073#] (< (:t p1__30073#) t)))
               ol (:olookup cr)
               cs (:cluster cr)
               write (partial write* cs)
               root (get ol basis_root_id)
               new_root (into [] (take-while keep? root))
               dir (get ol (:uuid (peek new_root)))
               new_dir (into [] (take-while keep? dir))
               leaf (get ol (:uuid (peek new_dir)))
               new_leaf (into [] (take-while keep? leaf))
               vec__30074 (repeatedly common/rand-uuid)
               leaf_id (nth vec__30074 (int 0) nil)
               dir_id (nth vec__30074 (int 1) nil)
               root_id (nth vec__30074 (int 2) nil)]
           (^clojure.lang.IFn write leaf_id (log/fressianed-leaf new_leaf))
           (^clojure.lang.IFn write
             dir_id
             (log/fressianed-dir
               (conj (pop new_dir) (log/create-entry (:t (first new_leaf)) leaf_id))))
           (^clojure.lang.IFn write
             root_id
             (log/fressianed-dir
               (conj (pop new_root) (log/create-entry (:t (first new_dir)) dir_id))))
           {:basis
            {:root-id basis_root_id,
             :dir-id (:uuid (peek new_root)),
             :leaf-id (:uuid (peek new_dir)),
             :root-count (java.lang.Integer/valueOf (int (count root))),
             :dir-count (java.lang.Integer/valueOf (int (count dir))),
             :leaf-count (java.lang.Integer/valueOf (int (count leaf)))},
            :truncated
            {:root-id root_id,
             :dir-id dir_id,
             :leaf-id leaf_id,
             :root-count (java.lang.Integer/valueOf (int (count new_root))),
             :dir-count (java.lang.Integer/valueOf (int (count new_dir))),
             :leaf-count (java.lang.Integer/valueOf (int (count new_leaf)))}})))))
  (reset-meta!
    #'truncate-log
    (assoc
      {:arglists (clojure.core/list ['cr 'basis-root-id 't]), :column (int 1)}
      :name
      'truncate-log
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.log-tools/merge-roots
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list
        'clojure.spec.alpha/cat
        :cr
        'clojure.core/any?
        :root-id-1
        'clojure.core/uuid?
        :root-id-2
        'clojure.core/uuid?)
      :ret
      'clojure.core/uuid?)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/cat
          :cr
          'clojure.core/any?
          :root-id-1
          'clojure.core/uuid?
          :root-id-2
          'clojure.core/uuid?)
        (s/cat-impl
          [:cr :root-id-1 :root-id-2]
          [any? uuid? uuid?]
          ['clojure.core/any? 'clojure.core/uuid? 'clojure.core/uuid?])
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/cat
        :cr
        'clojure.core/any?
        :root-id-1
        'clojure.core/uuid?
        :root-id-2
        'clojure.core/uuid?)
      (s/spec-impl 'clojure.core/uuid? uuid? nil nil)
      'clojure.core/uuid?
      nil
      nil
      nil))
  (def merge-roots
   (fn merge_roots
     ([cr root_id_1 root_id_2]
       (let [ol (:olookup cr)
             log_1 (create-tree-log ol root_id_1)
             log_2 (create-tree-log ol root_id_2)
             range_1 (t-range log_1)
             range_2 (t-range log_2)
             log_root_1 (log/get-root-val log_1)
             log_root_2 (log/get-root-val log_2)]
         (if (= (second range_1) (first range_2))
           (let [merged_root_id (common/rand-uuid)]
             (write*
               (:cluster cr)
               merged_root_id
               (log/fressianed-dir (into log_root_1 log_root_2)))
             merged_root_id)
           (do
             (throw (ex-info "Logs are not contiguous" {:range-1 range_1, :range-2 range_2}))
             nil))))))
  (reset-meta!
    #'merge-roots
    (assoc
      {:arglists (clojure.core/list ['cr 'root-id-1 'root-id-2]), :column (int 1)}
      :name
      'merge-roots
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.log-tools/transform-root
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :ret
      :datomic.tools.log-tools/transform-root-result)
    (s/fspec-impl
      nil
      nil
      (s/spec-impl
        :datomic.tools.log-tools/transform-root-result
        :datomic.tools.log-tools/transform-root-result
        nil
        nil)
      :datomic.tools.log-tools/transform-root-result
      nil
      nil
      nil))
  (defn transform-root
    ([cs f]
      (let [vec__30081 (log/read-tail-descriptor cs)
            desc (nth vec__30081 (int 0) nil)
            newdesc (log/inc-rev (update desc :d/r f))]
        (when-not (= (:d/r desc) (:d/r newdesc))
          (when (log/write-tail-descriptor cs newdesc nil)
            (let [result {:desc desc, :newdesc newdesc}]
              (let [logger (org.slf4j.LoggerFactory/getLogger "datomic.tools.log-tools")]
                (when (.isWarnEnabled ^org.slf4j.Logger logger)
                  (.warn
                    ^org.slf4j.Logger logger
                    (logger/process
                      (assoc result :event :datomic.tools.log-tools/transform-root))))
                nil)
              result))))))
  (reset-meta!
    #'transform-root
    (assoc
      {:arglists (clojure.core/list ['cs 'f]), :column (int 1)}
      :name
      'transform-root
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.log-tools/race-to-transform-root
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :ret
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req-un
        [:datomic.tools.log-tools/retries :datomic.tools.log-tools/transform-root-result]))
    (s/fspec-impl
      nil
      nil
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/keys
          :req-un
          [:datomic.tools.log-tools/retries :datomic.tools.log-tools/transform-root-result])
        (s/map-spec-impl
          {:req-un
           [:datomic.tools.log-tools/retries :datomic.tools.log-tools/transform-root-result],
           :opt-un nil,
           :gfn nil,
           :pred-exprs
           [(fn fn__30086 ([G__30085] (map? G__30085)))
            (fn fn__30088 ([G__30085] (contains? G__30085 :retries)))
            (fn fn__30090 ([G__30085] (contains? G__30085 :transform-root-result)))],
           :keys-pred
           (fn fn__30092
             ([G__30085]
               (and
                 (map? G__30085)
                 (contains? G__30085 :retries)
                 (contains? G__30085 :transform-root-result)))),
           :opt-keys [],
           :req-specs
           [:datomic.tools.log-tools/retries :datomic.tools.log-tools/transform-root-result],
           :req nil,
           :req-keys [:retries :transform-root-result],
           :opt-specs [],
           :pred-forms
           [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
            (clojure.core/list
              'clojure.core/fn
              ['%]
              (clojure.core/list 'clojure.core/contains? '% :retries))
            (clojure.core/list
              'clojure.core/fn
              ['%]
              (clojure.core/list 'clojure.core/contains? '% :transform-root-result))],
           :opt nil})
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/keys
        :req-un
        [:datomic.tools.log-tools/retries :datomic.tools.log-tools/transform-root-result])
      nil
      nil
      nil))
  (def race-to-transform-root
   (fn race_to_transform_root
     ([cs f retry_limit]
       (loop [n 0]
         (if (>= n retry_limit)
           {:retries (long n), :transform-root-result nil}
           (let [result (transform-root cs f)]
             (if result {:retries (long n), :transform-root-result result} (recur (inc n)))))))))
  (reset-meta!
    #'race-to-transform-root
    (assoc
      {:arglists (clojure.core/list ['cs 'f 'retry-limit]), :column (int 1)}
      :name
      'race-to-transform-root
      :ns
      *ns*))
  (def rebuild-root
   (fn rebuild_root
     ([olookup root_id]
       (let [root (get olookup root_id)]
         (reduce
           (fn fn__30098
             ([p__30097 entry]
               (let [map__30099 p__30097
                     map__30099 (if (seq? map__30099)
                                  (if (next map__30099)
                                    (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                      (to-array map__30099))
                                    (if (seq map__30099) (first map__30099) {}))
                                  map__30099)
                     rebuilt (get map__30099 :rebuilt)
                     root (get map__30099 :root)
                     root_t (:t entry)
                     dir_t (:t (first (get olookup (:uuid entry))))]
                 (if (= root_t dir_t)
                   {:rebuilt rebuilt, :root (conj root entry)}
                   {:rebuilt (conj rebuilt {:root-t root_t, :dir-t dir_t}),
                    :root (conj root (assoc entry :t dir_t))}))))
           {:rebuilt nil, :root [(first root)]}
           (rest root))))))
  (reset-meta!
    #'rebuild-root
    (assoc
      {:arglists (clojure.core/list ['olookup 'root-id]), :column (int 1)}
      :name
      'rebuild-root
      :ns
      *ns*))
  (defn has-t? ([log t] (= t (:t (iter/iget (log/seek-tx log t))))))
  (reset-meta!
    #'has-t?
    (assoc {:arglists (clojure.core/list ['log 't]), :column (int 1)} :name 'has-t? :ns *ns*)))