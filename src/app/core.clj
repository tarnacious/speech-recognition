(ns app.core
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [compojure.handler :as handler]
    [ring.util.response :refer [response]]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [clojure.java.io :refer [output-stream]]
    [clojure.data.json :as json]
    [app.wav :refer [sample-and-save]]
    [app.voice :refer [analyse]]
    [ring.util.response :refer [response redirect]])
  (:import
    [com.google.common.io LittleEndianDataInputStream]
    [java.io DataInputStream]
    [java.io EOFException]))


(defn read-float [stream]
  (try (double (.readFloat stream))
    (catch EOFException e nil)))


(defn readFloats [stream]
  (let [input (LittleEndianDataInputStream. stream)]
    (loop [value (read-float input)
           values []]
      (if value
        (recur (read-float input) (cons value values))
        values))))

(defroutes api-routes
  (POST "/" [] (fn [x]
    (let [data (reverse (readFloats (:body x)))
          [id path] (sample-and-save data)]
      (let [text (analyse path)]
        (response {"ok" id
                   "text" text}))))))

(defroutes app-routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/"))

(defroutes combined-routes
  (wrap-json-response api-routes)
  app-routes)

(def app combined-routes)

(defn -main [& args]
  (run-jetty app {:port 3000}))
