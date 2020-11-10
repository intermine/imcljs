(ns imcljs.path-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]))

(deftest walk-parent-reference
  (testing "Should be able to walk a path that references its parent and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene.proteins.genes")]
            (is (= (map :name walked) '("Gene" "Protein" "Gene")))
            (done)))))))

(deftest walk-parent-references
  (testing "Should be able to walk a path with multiple parent references and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene.proteins.genes.ontologyAnnotations.ontologyTerm.name")]
            (is (= (map :name walked) '("Gene" "Protein" "Gene" "OntologyAnnotation" "OntologyTerm" "name")))
            (done)))))))

(deftest walk-subclasses-with-type-constraints
  (testing "Should be able to walk a path with multiple subclasses requiring type constraints and return parts of the model"
    (async done
      (go
        (let [model (assoc (<! (fetch/model service))
                           :type-constraints [{:path "Gene.childFeatures" :type "MRNA"}
                                              {:path "Gene.childFeatures.CDSs.transcript" :type "TRNA"}])]
          (let [walked (path/walk model "Gene.childFeatures.CDSs.transcript.name")]
            (is (= (map :name walked) '("Gene" "MRNA" "CDS" "TRNA" "name")))
            (done))))))
  (testing "Should walk subclass even if it's the last part of the path"
    (async done
      (go
        (let [model (assoc (<! (fetch/model service))
                           :type-constraints [{:path "Gene.childFeatures" :type "MRNA"}])]
          (let [walked (path/walk model "Gene.childFeatures")]
            (is (= (map :name walked) '("Gene" "MRNA")))
            (done)))))))

(deftest walk-root
  (testing "Should be able to walk a path that is a single root and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene")]
            (is (= (map :name walked) '("Gene")))
            (done)))))))

(deftest walk-properties
  (testing "Should be able to walk a path and return properties of the classes in the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene.alleles" :walk-properties? true)]
            (is (= (map :displayName walked) '("Gene" "Alleles")))
            (done))))))
  (testing "Should be able to walk a path with multiple subclasses requiring type constraints and return properties of the classes in the model"
    (async done
      (go
        (let [model (assoc (<! (fetch/model service))
                           :type-constraints [{:path "Gene.childFeatures" :type "MRNA"}
                                              {:path "Gene.childFeatures.CDSs.transcript" :type "TRNA"}])]
          (let [walked (path/walk model "Gene.childFeatures.CDSs.transcript.name" :walk-properties? true)]
            (is (= (map :displayName walked) '("Gene" "Child Features" "CDSs" "Transcript" "Name")))
            (done)))))))

(deftest path-root
  (testing "Should be able to parse a root that is just a path"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene"]
          (is (= :Gene (path/class model path)))
          (is (= "Gene" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-normal
  (testing "Should be able to parse a path that is normal"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.organism.name"]
          (is (= :Organism (path/class model path)))
          (is (= "Gene.organism" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-subclass
  (testing "Should be able to parse a path with a subclass"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.proteins.genes.symbol"]
          (is (= :Gene (path/class model path)))
          (is (= "Gene.proteins.genes" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-subclasses
  (testing "Should be able to parse a path with multiple subclass"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.proteins.genes.ontologyAnnotations.ontologyTerm.name"]
          (is (= :OntologyTerm (path/class model path)))
          (is (= "Gene.proteins.genes.ontologyAnnotations.ontologyTerm"
                 (path/trim-to-last-class model path)))
          (done))))))
