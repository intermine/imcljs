(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as client]
            [imcljs.path :as path]
            [imcljs.entity :as entity]))

(enable-console-print!)

(defn on-js-reload []
  (def flymine {:root  "www.flymine.org/query"
                :model {:name "genomic"}})

  (def a-query {:select ["Gene.secondaryIdentifier Gene.symbol"]
                :orderBy [{:symbol "asc"}]
                :where  [{:path  "Gene.symbol"
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
                      {:path   "SequenceFeature.organism.shortName"
                       :op     "="
                       :value "D. melanogaster"}]})


  (let [model-req (client/get "http://localhost:9001/model.json" {:with-credentials? false})]
    (go (let [model (:model (:body (<! model-req)))]
          (.log js/console "Walked" (count (entity/direct-descendant-of model :SequenceFeature))))))


  ;(go (.log js/console "templates" (<! (fetch/templates flymine))))
  ;(go (.log js/console "enrichment" (<! (fetch/enrichment flymine {:list "PL FlyTF_putativeTFs"}))))
  ;(go (.log js/console "rows" (<! (fetch/rows flymine a-query))))
  )