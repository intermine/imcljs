(ns imcljs.query-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]
            [imcljs.query :as query]))

(def service {:root "www.flymine.org/query"
              :model {:name "genomic"}})

(def normal-query {:from "Gene"
                   :select ["Gene.symbol" "Gene.organism.name" "Gene.homologues.homologue.symbol"]
                   :where [{:path "Gene.symbol"
                            :op "="
                            :value "mad"
                            :code "A"}]})

(def outer-join-query {:constraintLogic "B and C"
                       :from "Gene"
                       :select ["secondaryIdentifier"
                                "symbol"
                                "homologues.homologue.primaryIdentifier"
                                "homologues.homologue.symbol"
                                "homologues.type"
                                "homologues.homologue.organism.name"
                                "homologues.dataSets.name"
                                "homologues.homologue.goAnnotation.ontologyTerm.name"
                                "homologues.homologue.goAnnotation.evidence.code.code"]
                       :joins ["homologues.homologue.goAnnotation"]
                       :where [{:path "Gene",
                                :op "LOOKUP",
                                :value "CG6235",
                                :extraValue "D. melanogaster",
                                :code "B",
                                :editable true,
                                :switched "LOCKED",
                                :switachable false}
                               {:path "homologues.homologue.organism.name",
                                :op "=",
                                :value "Anopheles gambiae",
                                :code "C",
                                :editable true,
                                :switched "LOCKED",
                                :switchable false}]})

(deftest outer-join
  (testing "Query support for outer joins"
    (async done
           (go
             (let [{results :results} (<! (fetch/table-rows service outer-join-query))]
               (is (some? (not-empty results)))
               (done))))))

(deftest deconstruct-by-class
  (testing "Should be able to deconstruct a query into its classes"
    (async done
           (go
             (let [model (<! (fetch/model service))]
               (let [result (query/deconstruct-by-class model normal-query)]
                 (is (= result {:Gene
                                {"Gene.homologues.homologue"
                                 {:query
                                  {:where [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                                   :from "Gene"
                                   :select ["Gene.homologues.homologue.id"]}}
                                 "Gene"
                                 {:query
                                  {:where [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                                   :from "Gene"
                                   :select ["Gene.id"]}}}
                                :Organism
                                {"Gene.organism"
                                 {:query
                                  {:where [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                                   :from "Gene"
                                   :select ["Gene.organism.id"]}}}}))
                 (done)))))))