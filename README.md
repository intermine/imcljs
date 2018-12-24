# imcljs

[![Build Status](https://travis-ci.org/intermine/imcljs.svg?branch=master)](https://travis-ci.org/intermine/imcljs)

A Clojure/ClojureScript library for interacting with InterMine's web services.

## Getting Start

Add the necessary dependency to your project:

[![Clojars Project](https://img.shields.io/clojars/v/org.intermine/imcljs.svg)](https://clojars.org/org.intermine/imcljs)

imcljs returns channels so you'll also want to include core.async

```[org.clojure/core.async "0.2.395"]```

## Usage

All imcljs funtions expect a map as their first parameter containing a mandtory `:root` key and two semi-optional keys, `:token` and `:model`.

```clj
(def flymine {:root  "www.flymine.org/query"
              :token nil ; Optional parameter for authentication
              :model "genomic" ; Required by some functions, such as executing a query
              })
```

We recommend fetching the `model` once and storing it in the above map for re-use across your application.

## Examples

### Fetching assets

```cljs
; Fetch model (you'll need this for later.)
(go (log (<! (fetch/model flymine)))

; Fetch templates
(go (log (<! (fetch/templates flymine)))

; Fetch lists
(go (log (<! (fetch/lists flymine)))

; Fetch summary fields
(go (log (<! (fetch/summary-fields flymine)))
```

### Fetching query results

Most result-fetching functions require that the `:model` key be present in their first parameter.

```cljs

(ns my-app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]))


(def my-query {:from   "Gene"
              :select ["Gene.secondaryIdentifier Gene.symbol"]
              :where  [{:path  "Gene.symbol"
                        :op    "="
                        :value "a*"}]})

; Rows
(go (log (<! (fetch/rows flymine my-query))))

; Records
(go (log (<! (fetch/records flymine my-query {:size 10}))))

; Row Count
(go (log (<! (fetch/row-count flymine my-query))))

```

## Development

### Running tests

To run tests in the browser:
```bash
lein doo default
```

To run tests in the JVM:
```bash
lein test
```

### Releasing new versions (Clojars)

1. Make sure to update the version number in [project.clj](https://github.com/intermine/imcljs/blob/dev/project.clj#L1)
2. Tag the [release with a matching version number in git and push the tag to GitHub](https://git-scm.com/book/en/v2/Git-Basics-Tagging)
3. To [push the release to Clojars](https://github.com/clojars/clojars-web/wiki/Pushing), type `lein deploy clojars`. **Note** that you'll need to have a clojars account that is a member of the [org.intermine](https://clojars.org/search?q=org.intermine) team. Currently (October 2018)  this is @yochannah, @sergio, @danielabutano and @julie-sullivan.
