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
  (def flymine {:root  "beta.flymine.org/beta"
                :model {:name "genomic"}})

  (def flymine-beta {:root  "beta.flymine.org/beta"
                     :model {:name "genomic"}})

  (def mousemine {:root  "www.mousemine.org/mousemine"
                  :model {:name "genomic"}})

  (def a-query {:select  ["Gene.symbol" "Gene.secondaryIdentifier" "Gene.homologues.homologue.name"]
                :orderBy [{:symbol "asc"}]
                :where   [{:path  "symbol"
                           :op    "="
                           :value "ab*"}]})


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

  (def subclass-query
    {:from    "OntologyAnnotation",
     :select  ["subject.primaryIdentifier"
               "subject.symbol"
               "evidence.baseAnnotations.subject.symbol"
               "evidence.baseAnnotations.subject.background.name"
               "evidence.baseAnnotations.subject.zygosity"
               "ontologyTerm.identifier"
               "ontologyTerm.name"],
     :orderBy [{:path "subject.symbol", :direction "ASC"}],
     :where   [{:path "ontologyTerm.parents", :type "MPTerm"}
               {:path "ontologyTerm", :type "MPTerm"}
               {:path "subject", :type "SequenceFeature"}
               {:path "evidence.baseAnnotations.subject", :type "Genotype"}
               {:path "ontologyTerm.parents", :op "LOOKUP", :value "*circulating glucose*", :code "A"}]})

  (def subclass-query-2
    {:name            "Lookup_MPhenotype",
     :title           "Lookup --> Mammalian phenotypes (MP terms)",
     :description     "Returns MP terms whose names match the specified search terms.",
     :constraintLogic "A and B",
     :from            "MPTerm",
     :select          ["name" "identifier" "description"],
     :orderBy         [{:path "name", :direction "ASC"}],
     :where           [{:path       "obsolete",
                        :op         "=",
                        :value      "false", :code "B",
                        :editable   false,
                        :switched   "LOCKED",
                        :switchable false}
                       {:path       "name",
                        :op         "CONTAINS",
                        :value      "hemoglobin",
                        :code       "A",
                        :editable   true,
                        :switched   "LOCKED",
                        :switchable false}]})

  (def constraint-query
    {:from    "Gene"
     :select  ["Gene.symbol" "Gene.secondaryIdentifier"]
     :orderBy [{:symbol "asc"}]
     :where   [{:path  "Gene"
                :op    "IN"
                :value "PL FlyAtlas_maleglands_top"}
               {:path  "Gene.symbol"
                :op    "<="
                :value "100"
                :code  "A"}]})

  (def big-query
    {:from    "Gene"
     :select  ["Gene.symbol" "Gene.secondaryIdentifier"]
     :orderBy [{:symbol "asc"}]})

  (go
    (let [fm (assoc flymine-beta :token (<! (fetch/session flymine-beta)))]
      (save/im-list-union fm "UNION" ["pie" "five_genes"])))


  #_(go
    (.log js/console "d" (<! (fetch/table-rows flymine big-query {:start 50 :size 1}))))






  #_(go
      (let [model (<! (fetch/model flymine))]
        (.log js/console (fetch/table-rows flymine constraint-query))))


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