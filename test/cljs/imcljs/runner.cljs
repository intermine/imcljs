(ns imcljs.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [imcljs.core-test]
            [imcljs.path-test]
            [imcljs.query-test]
            [imcljs.list-test]
            [imcljs.assets-test]
            [imcljs.registry-test]
            [imcljs.auth-test]
            [imcljs.utils-test]))

(doo-tests 'imcljs.core-test 'imcljs.path-test 'imcljs.query-test 'imcljs.list-test 'imcljs.assets-test 'imcljs.registry-test 'imcljs.auth-test 'imcljs.utils-test)
