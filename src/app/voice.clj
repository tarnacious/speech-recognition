(ns app.voice
  (:import (edu.cmu.sphinx.api Configuration StreamSpeechRecognizer))
  (:import java.io.FileInputStream)
)

(defn configure []
  (let [configuration (Configuration.)]
    (.setAcousticModelPath configuration
      "resource:/edu/cmu/sphinx/models/en-us/en-us")
    (.setDictionaryPath configuration
     ; "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")
      "resource:/example.dict")
    (.setLanguageModelPath configuration
      "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp")
    configuration))

(def configuration (configure))
(def recognizer (StreamSpeechRecognizer. configuration))


(defn analyse [filename]
    (let [stream (FileInputStream. filename)]
      (.startRecognition recognizer stream)
      (loop [result (.getResult recognizer)
             results []]
        (if result
          (do
            (println "Got some results")
            (println (.getHypothesis result))
            (.getHypothesis result)
            (recur (.getResult recognizer)
                   (cons (.getHypothesis result) results)))
          (do
            (.stopRecognition recognizer)
            results))
      )))
