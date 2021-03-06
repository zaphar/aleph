;;   Copyright (c) Zachary Tellman. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns aleph.test.http-response
  (:use [aleph] :reload-all)
  (:use
    [clojure.test]
    [clojure.contrib.duck-streams :only [pwd]])
  (:import
    [java.io
     File
     ByteArrayInputStream]))

(defn string-handler [request]
  (respond! request
    {:status 200
     :header {"Content-Type" "text/html"}
     :body "String!"}))

(defn seq-handler [request]
  (respond! request
    {:status 200
     :header {"Content-Type" "text/html"}
     :body ["sequence: " 1 " two " 3.0]}))

(defn file-handler [request]
  (respond! request
    {:status 200
     :body (File. (str (pwd) "/test/starry_night.jpg"))}))

(defn stream-handler [request]
  (respond! request
    {:status 200
     :header {"Content-Type" "text/html"}
     :body (ByteArrayInputStream. (.getBytes "Stream!"))}))

(def server (atom nil))
(def latch (promise))

(def route-map
  {"/stream" stream-handler
   "/file" file-handler
   "/seq" seq-handler
   "/string" string-handler
   "/stop" (fn [_]
	     (stop @server)
	     (deliver latch true))})

(defn handler [request]
  (when-let [handler (route-map (:uri request))]
    (handler request)))

(deftest http-response
  (let [server (reset! server (run-aleph handler {:port 8080}))]
    (is @latch)))
