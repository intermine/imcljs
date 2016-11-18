(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as client]
            [imcljs.path :as path]
            [imcljs.query :as query]
            [imcljs.save :as save]
            [imcljs.entity :as entity]))





(enable-console-print!)

(defn on-js-reload []
  (def flymine {:root  "www.flymine.org/query"
                :model {:name "genomic"}})

  (def a-query {:select  ["Gene.symbol" "Gene.secondaryIdentifier" "Gene.homologues.homologue.name"]
                :orderBy [{:symbol "asc"}]
                :where   [{:path  "symbol"
                           :op    "="
                           :value "a*"}]})


  (def region {:from    "SequenceFeature"
               :select  ["SequenceFeature.id"]
               :orderBy [["SequenceFeature.id" "asc"]]
               :where   [{:path   "SequenceFeature"
                          :op     "ISA"
                          :values ["Exon" "Intron" "Gene"]}]})



  (def bigr {:from   "SequenceFeature"
             :select ["SequenceFeature.id"
                      "SequenceFeature.name"
                      "SequenceFeature.primaryIdentifier"
                      "SequenceFeature.symbol"
                      "SequenceFeature.chromosomeLocation.start"
                      "SequenceFeature.chromosomeLocation.end"
                      "SequenceFeature.chromosomeLocation.locatedOn.primaryIdentifier"]
             :where  [{:path   "SequenceFeature.chromosomeLocation"
                       :op     "OVERLAPS"
                       :values ["2L:14615455..14619002"
                                "2R:5866646..5868384"
                                "3R:2578486..2580016"]}
                      {:path  "SequenceFeature.organism.shortName"
                       :op    "="
                       :value "D. melanogaster"}]})


  (def ids-constraint-shortcut
    {:from   "Gene"
     :select ["Gene.symbol"]
     :where  [{:path "Gene"
               :ids  [1000100 1000781 1001050 1001183 1001292]}]})


  (go
    (.log js/console "d" (<! (fetch/table-rows flymine a-query))))


  ;(go
  ;  (let [fm (assoc flymine :token (<! (fetch/session flymine)))]
  ;    (save/im-list fm a-query)))
  #_(go
      (let [model (<! (fetch/model flymine))]
        (.log js/console (query/deconstruct-by-class model a-query))))

  ;(go (.log js/console "templates" (<! (fetch/templates flymine))))
  ;(go (.log js/console "enrichment" (<! (fetch/enrichment flymine {:list "PL FlyTF_putativeTFs"}))))
  ;(go (.log js/console "rows" (<! (fetch/rows flymine a-query))))
  )