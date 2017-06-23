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

(def service {:root "www.flymine.org/flymine"
              :model {:name "genomic"}})

(def simple-query {:from "Gene" :select ["Gene.organism.name"]})

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

(defn on-js-reload []
  (go
    (let [model (<! (fetch/model service))]
      (.log js/console "done" (query/deconstruct-by-class model complicated-query)))))






