(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [imcljs.fetch :as fetch]
            [cljs-http.client :refer [post get delete]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as client]
            [imcljs.path :as path]
            [imcljs.query :as query]
            [imcljs.save :as save]
            [imcljs.entity :as entity]
            [imcljs.send :as send]
            [imcljs.auth :as auth]
            [cljs-http.client :as http]
            [imcljs.internal.utils :refer [<<!]]))

(enable-console-print!)

(def service {:root  "beta.humanmine.org/beta"
              :model {:name "genomic"}})

(def simple-query {:from "Gene" :select ["Gene.organism.name"] :size 10})

(def basic-query {:from "Gene" :select ["Gene.organism.name"]})

(def outer-join-query {:from      "Gene"
                       :select    ["primaryIdentifier"
                                   "secondaryIdentifier"
                                   "symbol"
                                   "publications.year"]
                       :joins     ["publications"]
                       :size      10
                       :sortOrder [{:path      "symbol"
                                    :direction "ASC"}]
                       :where     [{:path  "secondaryIdentifier"
                                    :op    "="
                                    :value "AC3.1*" ;AC3*
                                    :code  "A"}]})

(def complicated-query {:from            "Gene",
                        :select          ["Gene.symbol"
                                          "Gene.organism.name"
                                          "Gene.alleles.symbol"
                                          "Gene.alleles.phenotypeAnnotations.annotationType"
                                          "Gene.alleles.phenotypeAnnotations.description"],
                        :constraintLogic "(A or B)",
                        :where           [{:path "Gene.symbol", :value "zen", :op "=", :code "A"}
                                          {:path "Gene.symbol", :value "mad", :op "=", :code "B"}],
                        :orderBy         '()})

(def test-code {:from   "Gene",
                :select ["Gene.symbol"
                         "Gene.organism.name"
                         "Gene.alleles.symbol"
                         "Gene.alleles.phenotypeAnnotations.annotationType"
                         "Gene.alleles.phenotypeAnnotations.description"],
                :where  [{:path "Gene.symbol", :value "zen", :op "="}]})

(def tquery {:title  "pathway_genes1_1",
             :from   "Gene",
             :select ["Gene.secondaryIdentifier"
                      "Gene.symbol"
                      "Gene.primaryIdentifier"
                      "Gene.organism.name"],
             :where  [{:path "Gene", :op "IN", :value "pathway_genes1_1"}]})

(def weight-query {:from   "Protein"
                   :select ["primaryAccession" "molecularWeight"]
                   :where  [{:path  "Protein.primaryAccession"
                             :op    "LIKE"
                             :value "A0A021*"}]})

(defn on-js-reload []
  (go
    (let [model (<! (fetch/model service))]
      (go (js/console.log "done" (<! (fetch/code service model {:lang  "js"
                                                                :query



                                                                basic-query})))))))






