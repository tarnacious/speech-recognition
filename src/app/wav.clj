(ns app.wav
  (:import [javax.sound.sampled AudioInputStream AudioFormat AudioFormat$Encoding AudioSystem AudioFileFormat$Type ]


   )

  (:import
    [java.nio ByteOrder]
    [java.io ByteArrayInputStream]
    [java.nio ByteBuffer]
    [java.nio FloatBuffer]
    [com.google.common.io LittleEndianDataInputStream]
    [com.google.common.io ByteStreams])

  )

(defn ubyte [value]
  "Coerce a value in the range of an unsigned byte to a byte"
  (if (>= value 128) (byte (- value 256)) (byte value)))

(defn get-byte [value offset]
  "Returns a single byte of value offset from least significant"
  (ubyte (bit-and (bit-shift-right value (* offset 8)) 0xFF)))

(defn get-bytes [number]
  "Returns a lazy sequence of the bytes from least significant of number"
  (map (fn [offset] (get-byte number offset)) (range)))

(defn sample-to-int16 [value]
  "Returns an int16 sample of a value between 1 and -1"
  (int (* value 0x7FFF)))

(defn make-wav-header [samples sample-rate]
  "Returns a single channel 16 bit WAV RIFF header byte seq for a sample-rate
  and samples"
  (let [channels 1
        bits-per-sample 16
        block-align (/ (* channels bits-per-sample) 8)
        byte-rate  (/ (* sample-rate bits-per-sample channels) 8)
        data-size (/ (* (count samples) channels bits-per-sample) 8)
        size (+ 36 data-size)]
    (flatten [
      (map byte "RIFF")
      (take 4 (get-bytes size)) ; size following this
      (map byte "WAVE")
      (map byte "fmt ")
      (take 4 (get-bytes 16))  ; format
      (take 2 (get-bytes 1))   ; type pcm
      (take 2 (get-bytes channels))
      (take 4 (get-bytes sample-rate))
      (take 4 (get-bytes byte-rate))
      (take 2 (get-bytes block-align))
      (take 2 (get-bytes bits-per-sample))
      (map byte "data")
      (take 4 (get-bytes data-size))
      ])))

(defn sample-wav [samples]
  (let [sample-rate 44100
        byte-rate (/ (* sample-rate 32) 8)
        audio-format (* 22 4096)
        output-stream (clojure.java.io/output-stream "out.wav")
        ]
      ;(AudioSystem/write input-stream AudioFileFormat$Type/WAVE output-stream)
      ;(println input-stream)

    ))

(defn make-wav [samples sample-rate]
  "Returns a single channel 16 bit WAV RIFF byte seq for a sample-rate and a
  collection of floating point samples between 1 and -1"
  (let [header (make-wav-header samples sample-rate)
        sample-fn (fn [x] (take 2 (get-bytes (sample-to-int16 x))))
        pcm-values (flatten (map sample-fn samples))]
    (concat header pcm-values)))

(defn write-byte-seq [filename byte-seq]
  "Writes a sequence of bytes to disk"
  (with-open [output (clojure.java.io/output-stream filename)]
    (.write output (byte-array byte-seq))))

(defn input-stream-to-floats [input]
  (let [mybytes (ByteStreams/toByteArray input)
        bbuffer (.order (ByteBuffer/wrap mybytes) ByteOrder/LITTLE_ENDIAN)
        floatBuffer (.asFloatBuffer bbuffer)
        items (.remaining floatBuffer)
        arr (float-array items)]
        (.get floatBuffer arr)
        arr
        ))

(defn clip [value]
  (cond
    (> value 1.0) 1.0
    (< value -1.0) -1.0
    :else value))

(defn sample [value]
   (short (* (clip value) java.lang.Short/MAX_VALUE)))

(defn save [filename input]
  (let [audio-format (AudioFormat. 44100 16 1 true false)
        float-samples (input-stream-to-floats input)
        samples (map sample float-samples)
        byte-buffer (ByteBuffer/allocate (* (count samples) 2))
        little-endian (.order byte-buffer ByteOrder/LITTLE_ENDIAN)
        short-buffer (.asShortBuffer little-endian)
        _ (.put short-buffer (short-array samples))
        pcm-byte-array (.array byte-buffer)
        in-stream (clojure.java.io/input-stream pcm-byte-array)
        audio-stream (AudioInputStream. in-stream audio-format (count samples))
        ]
      (AudioSystem/write audio-stream AudioFileFormat$Type/WAVE (clojure.java.io/output-stream filename))))

(defn new-uuid []
    (java.util.UUID/randomUUID))

(defn sample-and-save [data]
    (let [wav (make-wav data 44100)
          id (new-uuid)
          data-path "resources/public/data"
          path (str data-path "/" id "-original.wav")
          path2 (str data-path "/" id ".wav")
          ]
      (if (not (.isDirectory (clojure.java.io/file data-path)))
        (.mkdir (java.io.File. data-path)))
      (write-byte-seq path wav)
      ; TODO: work out how to use the Java Sound API
      (clojure.java.shell/sh "sox" path "-b" "16" path2 "rate" "16k")
      [id path2]))
