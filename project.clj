(defproject app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main app.core
  :plugins [[lein-ring "0.8.8"]]
  :ring { :handler app.core/app
          :auto-reload? true }
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["resources/"]
  :jvm-opts [ "-Xms4G" "-Xmx4G"]

:repositories {
  "sonatype"{
    :url "http://oss.sonatype.org/content/repositories/releases"
    :snapshots false
    :releases {:checksum :fail}}
  "sonatype-snapshots" {
    :url "http://oss.sonatype.org/content/repositories/snapshots"
    :snapshots true
    :releases {:checksum :fail :update :always}}}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.2"]
                 [compojure "1.3.3"]
                 [ring/ring-json "0.3.1"]
                 [org.clojure/data.json "0.2.5"]
                 [edu.cmu.sphinx/sphinx4-core "1.0-SNAPSHOT"]
                 [edu.cmu.sphinx/sphinx4-data "1.0-SNAPSHOT"]])
