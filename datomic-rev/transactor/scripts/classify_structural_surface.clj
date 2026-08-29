(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(import '(java.io File PushbackReader)
        '(java.nio.file Files LinkOption)
        '(java.security MessageDigest))

(def expected-namespace-count 247)
(def expected-relation-header
  ["namespace" "oracle_status" "candidate_status" "oracle_sha256"
   "candidate_sha256" "relation"])
(def unstable-location-names
  #{"file" "line" "column" "end-line" "end-column"})
(def no-follow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn fail!
  [message data]
  (throw (ex-info message data)))

(defn ensure!
  [condition message data]
  (when-not condition
    (fail! message data)))

(defn regular-file!
  [^File file label]
  (ensure! (Files/isRegularFile (.toPath file) no-follow-links)
           (str label " must be a regular, non-symbolic file")
           {:label label :path (.getPath file)})
  file)

(defn directory!
  [^File directory label]
  (ensure! (Files/isDirectory (.toPath directory) no-follow-links)
           (str label " must be a directory, not a symbolic link")
           {:label label :path (.getPath directory)})
  directory)

(defn sha256-file
  [^File file]
  (let [digest (MessageDigest/getInstance "SHA-256")
        buffer (byte-array 65536)]
    (with-open [input (io/input-stream file)]
      (loop []
        (let [read-count (.read input buffer)]
          (when-not (= -1 read-count)
            (when (pos? read-count)
              (.update digest buffer 0 read-count))
            (recur)))))
    (apply str
           (map #(format "%02x" (bit-and 0xff %))
                (.digest digest)))))

(defn read-single-edn!
  [^File file label]
  (regular-file! file label)
  (with-open [reader (PushbackReader. (io/reader file :encoding "UTF-8"))]
    (let [eof (Object.)
          payload (edn/read {:eof eof} reader)
          trailing (edn/read {:eof eof} reader)]
      (ensure! (not (identical? eof payload))
               (str label " is empty")
               {:label label :path (.getPath file)})
      (ensure! (identical? eof trailing)
               (str label " contains more than one EDN form")
               {:label label :path (.getPath file)})
      payload)))

(defn split-tsv-line
  [line]
  (vec (.split ^String line "\t" -1)))

(defn read-lines!
  [^File file label]
  (regular-file! file label)
  (with-open [reader (io/reader file :encoding "UTF-8")]
    (vec (line-seq reader))))

(defn parse-relation!
  [^File root]
  (let [relation-file
        (io/file root "evidence" "namespace-surface-relation.tsv")
        lines (read-lines! relation-file "surface relation")]
    (ensure! (= expected-relation-header
                (some-> (first lines) split-tsv-line))
             "surface relation has an unexpected header"
             {:expected expected-relation-header
              :actual (some-> (first lines) split-tsv-line)})
    (ensure! (= (inc expected-namespace-count) (count lines))
             "surface relation has an unexpected row count"
             {:expected expected-namespace-count
              :actual (max 0 (dec (count lines)))})
    (let [rows
          (mapv
            (fn [index line]
              (let [fields (split-tsv-line line)]
                (ensure! (= 6 (count fields))
                         "surface relation row does not have six fields"
                         {:line (+ index 2) :field-count (count fields)})
                (let [[namespace-name oracle-status candidate-status
                       oracle-sha candidate-sha relation] fields]
                  (ensure! (boolean
                             (re-matches #"[A-Za-z0-9_.-]+" namespace-name))
                           "surface relation contains an unsafe namespace"
                           {:line (+ index 2) :namespace namespace-name})
                  (ensure! (= "PASS" oracle-status)
                           "oracle surface evidence is incomplete"
                           {:line (+ index 2) :namespace namespace-name
                            :status oracle-status})
                  (ensure! (= "PASS" candidate-status)
                           "candidate surface evidence is incomplete"
                           {:line (+ index 2) :namespace namespace-name
                            :status candidate-status})
                  (ensure! (boolean (re-matches #"[0-9a-f]{64}" oracle-sha))
                           "oracle surface SHA-256 is malformed"
                           {:line (+ index 2) :namespace namespace-name})
                  (ensure! (boolean (re-matches #"[0-9a-f]{64}" candidate-sha))
                           "candidate surface SHA-256 is malformed"
                           {:line (+ index 2) :namespace namespace-name})
                  (ensure! (contains? #{"MATCH" "DIFFER"} relation)
                           "surface relation is neither MATCH nor DIFFER"
                           {:line (+ index 2) :namespace namespace-name
                            :relation relation})
                  {:namespace namespace-name
                   :oracle-sha oracle-sha
                   :candidate-sha candidate-sha
                   :relation relation})))
            (range)
            (rest lines))
          namespaces (mapv :namespace rows)]
      (ensure! (= (count namespaces) (count (set namespaces)))
               "surface relation contains duplicate namespaces"
               {:rows (count namespaces)
                :unique-namespaces (count (set namespaces))})
      rows)))

(defn typed-map-node?
  [node]
  (and (vector? node)
       (= 4 (count node))
       (= :map (first node))
       (vector? (nth node 3))))

(defn typed-keyword
  [node]
  (when (and (vector? node)
             (= 3 (count node))
             (= :keyword (first node))
             (or (nil? (second node)) (string? (second node)))
             (string? (nth node 2)))
    (if-let [keyword-namespace (second node)]
      (keyword keyword-namespace (nth node 2))
      (keyword (nth node 2)))))

(defn validate-metadata-node!
  [node label]
  (ensure! (typed-map-node? node)
           "Var metadata is not a canonical typed-map node"
           {:label label})
  (let [entries (nth node 3)
        keys
        (mapv
          (fn [index entry]
            (ensure! (and (vector? entry) (= 2 (count entry)))
                     "Var metadata contains a malformed entry"
                     {:label label :entry-index index})
            (let [key-value (typed-keyword (first entry))]
              (ensure! (some? key-value)
                       "Var metadata contains a non-keyword key"
                       {:label label :entry-index index})
              key-value))
          (range)
          entries)]
    (ensure! (= (count keys) (count (set keys)))
             "Var metadata contains duplicate keys"
             {:label label})
    node))

(defn validate-surface!
  [surface label]
  (ensure! (map? surface)
           "surface payload is not a map"
           {:label label})
  (ensure! (map? (:vars surface))
           "surface payload has no Var map"
           {:label label})
  (ensure! (map? (:classes surface))
           "surface payload has no class map"
           {:label label})
  (doseq [[variable descriptor] (:vars surface)]
    (ensure! (symbol? variable)
             "surface Var key is not a symbol"
             {:label label :variable (pr-str variable)})
    (ensure! (map? descriptor)
             "surface Var descriptor is not a map"
             {:label label :variable (str variable)})
    (ensure! (contains? descriptor :metadata)
             "surface Var descriptor has no metadata field"
             {:label label :variable (str variable)})
    (validate-metadata-node!
      (:metadata descriptor)
      (str label "/" variable)))
  surface)

(defn location-key-node?
  [node]
  (and (vector? node)
       (= :keyword (first node))
       (contains? unstable-location-names (nth node 2 nil))))

(declare strip-locations)

(defn strip-locations
  [value]
  (cond
    (map? value)
    (into (empty value)
          (map (fn [[key child]]
                 [(strip-locations key) (strip-locations child)]))
          value)

    (typed-map-node? value)
    (let [[tag object-meta ignored entries] value]
      [tag
       (strip-locations object-meta)
       (strip-locations ignored)
       (->> entries
            (remove (fn [[key-node _]] (location-key-node? key-node)))
            (mapv (fn [[key child]]
                    [(strip-locations key) (strip-locations child)])))])

    (vector? value) (mapv strip-locations value)
    (list? value) (apply list (map strip-locations value))
    (set? value) (set (map strip-locations value))
    (seq? value) (doall (map strip-locations value))
    :else value))

(defn strip-var-metadata
  [surface]
  (update surface :vars
          (fn [vars]
            (into (empty vars)
                  (map (fn [[variable descriptor]]
                         [variable (dissoc descriptor :metadata)]))
                  vars))))

(defn normalized-metadata-map
  [node]
  (into {}
        (map (fn [[key-node child]]
               [(typed-keyword key-node) child]))
        (nth node 3)))

(defn metadata-for
  [surface variable]
  (if-let [descriptor (get (:vars surface) variable)]
    (-> descriptor :metadata strip-locations normalized-metadata-map)
    {}))

(defn differing-keys
  [left right]
  (let [all-keys (into #{} (concat (keys left) (keys right)))]
    (set (filter #(not= (get left % ::missing)
                        (get right % ::missing))
                 all-keys))))

(defn surface-file
  [^File root mode namespace-name]
  (io/file root "logs" mode (str namespace-name ".surface.edn")))

(defn read-surface-pair!
  [^File root {:keys [namespace oracle-sha candidate-sha relation]}]
  (let [oracle-file (surface-file root "surface-oracle" namespace)
        candidate-file (surface-file root "surface-candidate" namespace)
        oracle-label (str "oracle surface " namespace)
        candidate-label (str "candidate surface " namespace)
        oracle (-> (read-single-edn! oracle-file oracle-label)
                   (validate-surface! oracle-label))
        candidate (-> (read-single-edn! candidate-file candidate-label)
                      (validate-surface! candidate-label))
        actual-oracle-sha (sha256-file oracle-file)
        actual-candidate-sha (sha256-file candidate-file)
        byte-equal? (java.util.Arrays/equals
                      (Files/readAllBytes (.toPath oracle-file))
                      (Files/readAllBytes (.toPath candidate-file)))]
    (ensure! (= oracle-sha actual-oracle-sha)
             "oracle surface SHA-256 does not match its relation row"
             {:namespace namespace :expected oracle-sha
              :actual actual-oracle-sha})
    (ensure! (= candidate-sha actual-candidate-sha)
             "candidate surface SHA-256 does not match its relation row"
             {:namespace namespace :expected candidate-sha
              :actual actual-candidate-sha})
    (ensure! (= relation (if byte-equal? "MATCH" "DIFFER"))
             "surface relation disagrees with the payload bytes"
             {:namespace namespace :relation relation
              :payload-relation (if byte-equal? "MATCH" "DIFFER")})
    [oracle candidate]))

(defn classify-row
  [state ^File root {:keys [namespace] :as relation-row}]
  (let [[oracle candidate] (read-surface-pair! root relation-row)
        variables (into #{} (concat (keys (:vars oracle))
                                    (keys (:vars candidate))))
        row-differences
        (reduce
          (fn [differences variable]
            (let [keys (differing-keys (metadata-for oracle variable)
                                       (metadata-for candidate variable))]
              (if (seq keys)
                (assoc differences variable keys)
                differences)))
          {}
          variables)]
    (reduce
      (fn [result [variable keys]]
        (reduce
          (fn [inner key]
            (-> inner
                (update-in [:families key :vars] (fnil conj #{})
                           [namespace variable])
                (update-in [:families key :namespaces] (fnil conj #{})
                           namespace)))
          (-> result
              (update :metadata-differing-vars conj [namespace variable])
              (update :metadata-differing-namespaces conj namespace))
          keys))
      (cond-> state
        (= oracle candidate) (update :exact inc)
        (= (strip-locations oracle) (strip-locations candidate))
        (update :without-location inc)
        (= (strip-var-metadata oracle) (strip-var-metadata candidate))
        (update :without-var-metadata inc))
      row-differences)))

(defn metadata-key-name
  [key]
  (if-let [key-namespace (namespace key)]
    (str key-namespace "/" (name key))
    (name key)))

(defn classify!
  [^File root]
  (let [rows (parse-relation! root)]
    (reduce (fn [state row] (classify-row state root row))
            {:namespace-count (count rows)
             :exact 0
             :without-location 0
             :without-var-metadata 0
             :metadata-differing-vars #{}
             :metadata-differing-namespaces #{}
             :families {}}
            rows)))

(defn emit-result!
  [{:keys [namespace-count exact without-location without-var-metadata
           metadata-differing-vars metadata-differing-namespaces families]}]
  (println "record_type\tname\tvalue\tnamespace_count")
  (doseq [[name value]
          [["schema-version" 1]
           ["status" "PASS"]
           ["namespace-count" namespace-count]
           ["exact-namespace-count" exact]
           ["location-stripped-exact-namespace-count" without-location]
           ["var-metadata-excluded-exact-namespace-count"
            without-var-metadata]
           ["metadata-differing-var-count" (count metadata-differing-vars)]
           ["metadata-differing-namespace-count"
            (count metadata-differing-namespaces)]
           ["metadata-family-count" (count families)]]]
    (printf "summary\t%s\t%s\t\n" name value))
  (doseq [[key {:keys [vars namespaces]}]
          (sort-by (comp metadata-key-name key) families)]
    (printf "metadata-family\t%s\t%s\t%s\n"
            (metadata-key-name key) (count vars) (count namespaces))))

(defn main!
  []
  (ensure! (= 1 (count *command-line-args*))
           "usage: classify_structural_surface.clj STRUCTURAL_OUTPUT_ROOT"
           {:argument-count (count *command-line-args*)})
  (let [root (-> (io/file (first *command-line-args*))
                 (directory! "structural output root")
                 .getCanonicalFile)
        result (classify! root)]
    (emit-result! result)))

(try
  (main!)
  (catch Throwable failure
    (binding [*out* *err*]
      (println "structural surface classification failed:" (.getMessage failure))
      (when-let [data (ex-data failure)]
        (println (pr-str data))))
    (System/exit 1)))
