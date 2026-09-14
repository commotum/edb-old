(do
  (clojure.core/in-ns 'datomic.cli)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cli)
    {:doc
     "Command line parsing.\n\nFunctions in this namespace deal with the following data structures:\n\nCommand line args\nA sequence of strings, e.g. from *command-line-args*\n\n(Processed) args\nargs converted into map of keys to typed values\n\nSpecs             \nA set of descriptions of arguments. Specs are maps that contain\nsome or all of the following keys:\n\n* short-name   a single chararcter name\n* long-name    a human-friendly Clojure name\n* coerce       a function to coerce a string to desired type\n* required     true if the arg must be present\n* default      a default value\n* doc          a docstring for the argument\n\nPositions\nA collection of keywords specifying the names to bind to positional\narguments. The names must correspond to the :long-name field in the\nspec map.\n\nA program that had a required, positional argument dir and an optional,\nnamed argument port might start like this:\n\n(def args (cli/parse-or-exit!\n           *file*\n           *command-line-args*\n           #{{:long-name :dir :required true}\n             {:long-name :port :short-name :p :coerce #(Integer. %)}}\n           [:dir]))\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.set :as 'set]
        ['clojure.java.io :as 'io]
        ['clojure.java.shell :as 'sh]
        ['clojure.string :as 'str]
        ['datomic.require :as 'req])))
  (when-not (.equals 'datomic.cli 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cli))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.set :as 'set]
          ['clojure.java.io :as 'io]
          ['clojure.java.shell :as 'sh]
          ['clojure.string :as 'str]
          ['datomic.require :as 'req]))))
  (defn unique-index
    ([xrel k v]
      (reduce
        (fn fn__20166
          ([m x]
            (let [temp__5823__auto__ (get x k)]
              (if temp__5823__auto__
                (let [xk temp__5823__auto__ temp__5823__auto__ (if v (get x v) x)]
                  (if temp__5823__auto__ (let [xv temp__5823__auto__] (assoc m xk xv)) m))
                m))))
        {}
        xrel))
    ([xrel k] (unique-index xrel k nil)))
  (reset-meta!
    #'unique-index
    (assoc
      {:private true, :arglists (clojure.core/list ['xrel 'k] ['xrel 'k 'v]), :column (int 1)}
      :name
      'unique-index
      :ns
      *ns*))
  (defn print-help
    ([cmd spec positions]
      (binding [*out* *err*]
        (let [pset (into #{} positions)
              positional? (fn positional_QMARK_
                            ([p1__20171#] (contains? pset (:long-name p1__20171#))))
              named (remove positional? spec)
              positional (filter positional? spec)]
          (apply println "Usage:" (str cmd (when (seq named) " {options}")) (map name positions))
          (when (seq named)
            (println "\nOptions: ")
            (loop [seq_20174 (seq named) chunk_20175 nil count_20176 0 i_20177 0]
              (if (< i_20177 count_20176)
                (let [opt (.nth ^clojure.lang.Indexed chunk_20175 (int i_20177))]
                  (println
                    (str
                      (format
                        "%-20s: "
                        (str
                          (let [temp__5825__auto__ (:short-name opt)]
                            (when temp__5825__auto__
                              (let [sn temp__5825__auto__] (str "-" (name sn) ",\n "))))
                          "--"
                          (name (:long-name opt))))
                      (:doc opt)
                      (let [temp__5825__auto__ (:default opt)]
                        (when temp__5825__auto__
                          (let [df temp__5825__auto__] (str " (default " df ")"))))))
                  (recur seq_20174 chunk_20175 count_20176 (inc i_20177)))
                (let [temp__5825__auto__ (seq seq_20174)]
                  (when temp__5825__auto__
                    (let [seq_20174 temp__5825__auto__]
                      (if (chunked-seq? seq_20174)
                        (let [c__6090__auto__ (chunk-first seq_20174)]
                          (recur
                            (chunk-rest seq_20174)
                            c__6090__auto__
                            (int (count c__6090__auto__))
                            (int 0)))
                        (let [opt (first seq_20174)]
                          (println
                            (str
                              (format
                                "%-20s: "
                                (str
                                  (let [temp__5825__auto__ (:short-name opt)]
                                    (when temp__5825__auto__
                                      (let [sn temp__5825__auto__] (str "-" (name sn) ",\n "))))
                                  "--"
                                  (name (:long-name opt))))
                              (:doc opt)
                              (let [temp__5825__auto__ (:default opt)]
                                (when temp__5825__auto__
                                  (let [df temp__5825__auto__] (str " (default " df ")"))))))
                          (recur (next seq_20174) nil 0 0)))))))))
          (when (seq positional)
            (println "\nPositional arguments:")
            (let [idx (unique-index positional :long-name)]
              (loop [seq_20178 (seq positions) chunk_20179 nil count_20180 0 i_20181 0]
                (if (< i_20181 count_20180)
                  (let [pos (.nth ^clojure.lang.Indexed chunk_20179 (int i_20181))]
                    (let [temp__5825__auto__ (get idx pos)]
                      (when temp__5825__auto__
                        (let [arg temp__5825__auto__]
                          (println
                            (str
                              (format "%-20s: " (name (:long-name arg)))
                              (:doc arg)
                              (let [temp__5825__auto__ (:default arg)]
                                (when temp__5825__auto__
                                  (let [df temp__5825__auto__] (str " (default " df ")")))))))))
                    (recur seq_20178 chunk_20179 count_20180 (inc i_20181)))
                  (let [temp__5825__auto__ (seq seq_20178)]
                    (when temp__5825__auto__
                      (let [seq_20178 temp__5825__auto__]
                        (if (chunked-seq? seq_20178)
                          (let [c__6090__auto__ (chunk-first seq_20178)]
                            (recur
                              (chunk-rest seq_20178)
                              c__6090__auto__
                              (int (count c__6090__auto__))
                              (int 0)))
                          (let [pos (first seq_20178)]
                            (let [temp__5825__auto__ (get idx pos)]
                              (when temp__5825__auto__
                                (let [arg temp__5825__auto__]
                                  (println
                                    (str
                                      (format "%-20s: " (name (:long-name arg)))
                                      (:doc arg)
                                      (let [temp__5825__auto__ (:default arg)]
                                        (when temp__5825__auto__
                                          (let [df temp__5825__auto__]
                                            (str " (default " df ")")))))))))
                            (recur (next seq_20178) nil 0 0)))))))))
            nil)))))
  (reset-meta!
    #'print-help
    (assoc
      {:arglists (clojure.core/list ['cmd 'spec 'positions]), :column (int 1)}
      :name
      'print-help
      :ns
      *ns*))
  (defn cli->map
    ([strings positions vararg]
      (let [G__20198 strings
            vec__20199 G__20198
            seq__20200 (seq vec__20199)
            first__20201 (first seq__20200)
            seq__20200 (next seq__20200)
            s first__20201
            more seq__20200
            positions positions
            m {}]
        (loop [G__20198 G__20198 positions positions m m]
          (let [vec__20202 G__20198
                seq__20203 (seq vec__20202)
                first__20204 (first seq__20203)
                seq__20203 (next seq__20203)
                s first__20204
                more seq__20203
                positions positions
                m m]
            (if s
              (let [temp__5823__auto__ (re-matches #"-+(.*)" s)]
                (if temp__5823__auto__
                  (let [vec__20205 temp__5823__auto__
                        _ (nth vec__20205 (int 0) nil)
                        k (nth vec__20205 (int 1) nil)]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (let [vec__20208 more
                            seq__20209 (seq vec__20208)
                            first__20210 (first seq__20209)
                            seq__20209 (next seq__20209)
                            v first__20210
                            more seq__20209]
                        (recur more positions (assoc m (keyword k) v)))))
                  (let [vec__20211 positions
                        seq__20212 (seq vec__20211)
                        first__20213 (first seq__20212)
                        seq__20212 (next seq__20212)
                        k first__20213
                        pmore seq__20212]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (recur more pmore (assoc m (keyword k) s))))))
              m))))))
  (reset-meta!
    #'cli->map
    (assoc
      {:arglists (clojure.core/list ['strings 'positions 'vararg]), :column (int 1)}
      :name
      'cli->map
      :ns
      *ns*))
  (defn expand-short-names
    ([m spec] (let [idx (unique-index spec :short-name :long-name)] (set/rename-keys m idx))))
  (reset-meta!
    #'expand-short-names
    (assoc
      {:private true, :arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'expand-short-names
      :ns
      *ns*))
  (defn coerce-vals
    ([m spec]
      (let [idx (unique-index spec :long-name :coerce)]
        (reduce
          (fn fn__20218
            ([m p__20217]
              (let [vec__20219 p__20217
                    k (nth vec__20219 (int 0) nil)
                    v (nth vec__20219 (int 1) nil)]
                (assoc m k ((get idx k identity) v)))))
          {}
          m))))
  (reset-meta!
    #'coerce-vals
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'coerce-vals
      :ns
      *ns*))
  (defn apply-defaults
    ([m spec] (let [defaults (unique-index spec :long-name :default)] (merge defaults m))))
  (reset-meta!
    #'apply-defaults
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'apply-defaults
      :ns
      *ns*))
  (defn missing-values
    ([m spec]
      (let [required (into #{} (keys (unique-index spec :long-name :required)))]
        (set/difference required (set (keys m))))))
  (reset-meta!
    #'missing-values
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'missing-values
      :ns
      *ns*))
  (defn parse-or-exit!
    ([cmd args spec positions vararg]
      (if (some #{"--help"} args)
        (do (print-help cmd spec positions) (java.lang.System/exit (int -1)) nil)
        (let [m (apply-defaults
                  (coerce-vals (expand-short-names (cli->map args positions vararg) spec) spec)
                  spec)
              missing (missing-values m spec)]
          (if (seq missing)
            (do
              (print-help cmd spec positions)
              (println "\n**** missing required arguments" missing "****")
              (java.lang.System/exit (int -1))
              nil)
            m))))
    ([cmd arg spec positions] (parse-or-exit! cmd arg spec positions nil)))
  (reset-meta!
    #'parse-or-exit!
    (assoc
      {:arglists
       (clojure.core/list ['cmd 'arg 'spec 'positions] ['cmd 'args 'spec 'positions 'vararg]),
       :column (int 1)}
      :name
      'parse-or-exit!
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cli" "failed") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cli" "failed") (atom false))
  (defn fail ([msg] (reset! failed true) msg))
  (reset-meta!
    #'fail
    (assoc {:arglists (clojure.core/list ['msg]), :column (int 1)} :name 'fail :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cli" "exit-after-command") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cli" "exit-after-command") (atom true))
  (defn shell
    ([& args]
      (let [map__20228 (apply sh/sh args)
            map__20228 (if (seq? map__20228)
                         (if (next map__20228)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20228))
                           (if (seq map__20228) (first map__20228) {}))
                         map__20228)
            m map__20228
            exit (get map__20228 :exit)
            err (get map__20228 :err)]
        (when-not (or (= 0 exit) (seq err))
          (throw (ex-info "Shell command failed" {:args args, :result m})))
        (:out m))))
  (reset-meta!
    #'shell
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name 'shell :ns *ns*))
  (defn development-version
    ([& _]
      (let [map__20231 (read-string (slurp "pom.clj"))
            map__20231 (if (seq? map__20231)
                         (if (next map__20231)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20231))
                           (if (seq map__20231) (first map__20231) {}))
                         map__20231)
            version_prefix (get map__20231 :version-prefix)
            revision (str/trimr (shell "build/revision"))]
        (str version_prefix "." revision))))
  (reset-meta!
    #'development-version
    (assoc
      {:arglists (clojure.core/list ['& '_]), :column (int 1)}
      :name
      'development-version
      :ns
      *ns*)))