# imcljs

[![CircleCI](https://circleci.com/gh/intermine/imcljs.svg?style=svg)](https://circleci.com/gh/intermine/imcljs)
[![Clojars Project](https://img.shields.io/clojars/v/org.intermine/imcljs.svg)](https://clojars.org/org.intermine/imcljs)


A Clojure/ClojureScript library for interacting with InterMine's web services.

## Getting Started

Add the necessary dependency to your project:

[![Clojars Project](https://img.shields.io/clojars/v/org.intermine/imcljs.svg)](https://clojars.org/org.intermine/imcljs)

imcljs returns channels so you'll also want to include core.async

```[org.clojure/core.async "0.2.395"]```

## Usage

With the exception of the fetch/registry function, all imcljs functions expect a map as their first parameter containing a mandatory `:root` key and two semi-optional keys, `:token` and `:model`.

```clj
(def flymine {:root  "https://www.flymine.org/flymine"
              :token nil ; Optional parameter for authentication
              :model "genomic" ; Required by some functions, such as executing a query
              })
```

We recommend fetching the `model` once and storing it in the above map for re-use across your application.

## Examples

### Fetching a list of InterMines from the Registry

```cljs
(let [;fetch all mines except the dev/beta mines
      prod-mines (fetch/registry false)
      ;fetch all mines INCLUDING the dev/beta mines
      dev-and-prod-mines (fetch/registry true)]

      (go
        (let [prod (<! prod-mines )
              dev (<! dev-and-prod-mines)]
          ;; This should print true, so long as the sum of dev+prod mines is 
          ;; greater than the count of prod mines
          (.log js/console (< (count (:instances (:body prod)))
              (count (:instances (:body dev)))))
          )))
```

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

### REPL

It's useful to have a live interactive environment to invoke imcljs when developing. You can get this by running:

```
$ lein repl
user=> (fig-start)
# Figwheel: Starting server at http://0.0.0.0:5003
# Open this URL in your browser.
user=> (cljs-repl)
```

You can now use `require` to test parts of imcljs and dependencies. Even better, if you install an nREPL plugin to your editor, you can load any namespace and evaluate arbitrary code all from your editor (`src/cljs/imcljs/core.cljs` has useful code for testing). **Keep an eye on your browser devtool's Network and Console tabs**; it's great for debugging requests and seeing the output when running code.

### Running tests

**Required dependency:** phantomjs, to run the tests in a headless javascript engine. You'll need a recent version of node installed, perhaps via [nvm](https://github.com/creationix/nvm). Once node is installed, run `npm install -g phantomjs` to install phantomjs. 

**Local biotestmine:** The tests are run against a local biotestmine instance on port 9999 (can be changed in *test/cljs/imcljs/env.cljs*). If you're not familiar with building InterMine instances, we recommend using [intermine_boot](https://github.com/intermine/intermine_boot).

**To run tests in the browser:**
```bash
lein doo
```

**To run tests in the JVM:**
```bash
lein test
```

### Releasing new versions (Clojars)

1. Update the version number in [project.clj](https://github.com/intermine/imcljs/blob/dev/project.clj#L1)
2. Don't forget to add the new version with notes to **CHANGELOG.md**.
3. Tag the [release with a matching version number in git and push the tag to GitHub](https://git-scm.com/book/en/v2/Git-Basics-Tagging)
4. To [push the release to Clojars](https://github.com/clojars/clojars-web/wiki/Pushing), type `lein deploy clojars`. **Note** that you'll need to have a clojars account that is a member of the [org.intermine](https://clojars.org/search?q=org.intermine) team.

### API Docs

API docs for IMCLJS are available at [intermine.org/imcljs](http://intermine.org/imcljs). 

These docs are automatically generated by CircleCI when anything is merged to dev.

If you would like to generate them locally, run `lein codox` and go to your target/docs folder.  

### Formatting

Builds will fail unless your file is formatted correctly. Write code with whatever formatting you like - but before you commit and push, run `lein format` to auto-format your file to the required standards. The formatting is managed using the [cljfmt](https://github.com/weavejester/cljfmt) package. 
