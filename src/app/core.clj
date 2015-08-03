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
    [app.wav :refer [sample-and-save sample-wav save]]
    [app.voice :refer [analyse]]
    [ring.util.response :refer [response redirect]])
  (:import
    [java.nio ByteOrder]
    [java.io ByteArrayInputStream]
    [java.nio ByteBuffer]
    [java.nio FloatBuffer]
    [com.google.common.io LittleEndianDataInputStream]
    [com.google.common.io ByteStreams]
    [java.io DataInputStream]
    [java.io EOFException]))


(defn read-float [stream]
  (try (double (.readFloat stream))
    (catch EOFException e nil)))


(defn read-floats [stream]
  (let [input (LittleEndianDataInputStream. stream)

        ]
    (loop [value (read-float input)
           values []]
      (if value
        (recur (read-float input) (cons value values))
        values))))


(defn new-uuid []
    (java.util.UUID/randomUUID))

(defroutes api-routes
  (POST "/" [] (fn [x]
    (let [
          id (new-uuid)
          data-path "resources/public/data"
          file-path (str data-path "/" id ".wav")
          _ (save file-path (:body x))
          ]
      (let [text (analyse file-path)]
        (response {"ok" id
                   "text" (first text)}))
      ))))

(defroutes app-routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/"))

(defroutes combined-routes
  (wrap-json-response api-routes)
  app-routes)

(def app combined-routes)




(defn convert-byte-order [in-filename, out-filename]
  (let [input (clojure.java.io/input-stream "test.dat")
        mybytes (ByteStreams/toByteArray input)
        bbuffer (.order (ByteBuffer/wrap mybytes) ByteOrder/LITTLE_ENDIAN)
        floatBuffer (.asFloatBuffer bbuffer)
        items (.remaining floatBuffer)
        arr (float-array items)
        _ (.get floatBuffer arr)
        bbuffer2 (ByteBuffer/allocate (* items 4))
        floatBuffer2 (.asFloatBuffer bbuffer2)
        _ (.put floatBuffer2 arr)
        arr2 (.array bbuffer2)]
       (clojure.java.io/copy arr2 (clojure.java.io/output-stream "test2.dat"))
  ))

(defn -main [& args]
  (let [input (clojure.java.io/input-stream "test.dat")
        ;data (DataInputStream. input)
        ;f (.readFloat data)
        ]
    (save "music.wav" input)
  ))

  ;(sample-wav []))
  ;(run-jetty app {:port 3000}))
