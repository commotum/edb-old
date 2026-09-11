(do
  (clojure.core/in-ns 'datomic.data)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.data 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.data))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  ;; ATOMIC-NOTE BEGIN serialization-data-role (baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d)
  ;; Observed: this namespace contains only a fixed permutation of 0..127.
  ;; config-ext optionally loads it with obscure and license-inline; no table
  ;; consumer was found in the recovered trees, and those implementations are
  ;; not present here. Its further purpose is unknown from this evidence.
  ;; Do not mistake the name "data" for Datomic's datom/value model or invent a
  ;; native serialization requirement from this table. Preserve the artifact;
  ;; the actual value handler and storage callers are traced in fressian.clj.
  ;; The peer copy is byte-identical at the baseline.
  ;; ATOMIC-NOTE END serialization-data-role
  (def table
   [35
    47
    22
    72
    51
    21
    9
    94
    50
    79
    4
    127
    102
    36
    26
    80
    77
    85
    5
    112
    62
    38
    28
    60
    25
    1
    8
    40
    113
    86
    69
    14
    23
    104
    7
    32
    30
    57
    19
    111
    84
    82
    6
    95
    107
    126
    34
    24
    105
    75
    29
    59
    12
    87
    92
    2
    117
    71
    100
    76
    123
    53
    88
    119
    90
    78
    109
    58
    73
    18
    39
    67
    64
    20
    93
    91
    74
    106
    66
    45
    13
    96
    114
    43
    54
    3
    46
    97
    61
    89
    27
    108
    44
    110
    70
    101
    122
    125
    55
    48
    31
    16
    11
    17
    33
    42
    115
    98
    81
    37
    65
    63
    15
    116
    49
    103
    0
    83
    52
    120
    121
    118
    68
    41
    10
    124
    99
    56])
  (reset-meta! #'table (assoc {:column (int 1)} :name 'table :ns *ns*)))