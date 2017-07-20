(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as client]
            [imcljs.path :as path]
            [imcljs.query :as query]
            [imcljs.save :as save]
            [imcljs.entity :as entity]
            [imcljs.auth :as auth]))

(enable-console-print!)

(def service {:root "www.flymine.org/query"
              :model {:name "genomic"}})

(def simple-query {:from "Gene" :select ["Gene.organism.name"] :size 10})

(def outer-join-query {:from "Gene"
                       :select ["primaryIdentifier"
                                "secondaryIdentifier"
                                "symbol"
                                "publications.year"]
                       :joins ["publications"]
                       :size 10
                       :sortOrder [{:path "symbol"
                                    :direction "ASC"}]
                       :where [{:path "secondaryIdentifier"
                                :op "="
                                :value "AC3.1*" ;AC3*
                                :code "A"}]})

(def complicated-query {:from "Gene",
                        :select ["Gene.symbol"
                                 "Gene.organism.name"
                                 "Gene.alleles.symbol"
                                 "Gene.alleles.phenotypeAnnotations.annotationType"
                                 "Gene.alleles.phenotypeAnnotations.description"],
                        :constraintLogic "(A or B)",
                        :where [{:path "Gene.symbol", :value "zen", :op "=", :code "A"}
                                {:path "Gene.symbol", :value "mad", :op "=", :code "B"}],
                        :orderBy '()})

(def test-code {:from "Gene",
                :select ["Gene.symbol"
                         "Gene.organism.name"
                         "Gene.alleles.symbol"
                         "Gene.alleles.phenotypeAnnotations.annotationType"
                         "Gene.alleles.phenotypeAnnotations.description"],
                :where [{:path "Gene.symbol", :value "zen", :op "="}]})


(defn on-js-reload []
  (go
    (let [model (<! (fetch/model service))]
      (.log js/console "sterile" (query/sterilize-query simple-query))
      (.log js/console "xml" (query/->xml {:name "genomic"} simple-query))
      (.log js/console "sort" (<! (fetch/rows service simple-query))))))







