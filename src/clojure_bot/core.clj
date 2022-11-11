(ns clojure-bot.core
  (:require [clojure.core.async :refer [<!! go-loop]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t])
  (:gen-class))

; TODO: fill correct token
(def token (env :telegram-token))

(def image-list [])

(def message-list [])

(p/stop channel)

(h/defhandler handler

  (h/command-fn "image-attack"
                (fn [{{id :id :as chat} :chat}]
                  (dotimes [i 5]
                    (t/send-photo token id (io/file (io/resource "image.jpeg"))))))

  (h/command-fn "spam-random-images"
                (fn [{{id :id :as chat} :chat}]
                  (println "Bot joined new chat: " chat)
                  (go-loop []
                    (do
                      (t/send-photo token id (io/file (io/resource (rand-nth image-list))))
                      (t/send-text token id (rand-nth message-list))
                      (Thread/sleep 360000)
                      (recur)))))

  (h/command-fn "help"
                (fn [{{id :id :as chat} :chat}]
                  (println "Help was requested in " chat)
                  (t/send-text token id "/image-attack or /spam-random-images")))

  (h/message-fn
   (fn [{{id :id} :chat :as message}]
     (println "Intercepted message: " message)
     (t/send-text token id "unknown command"))))


(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the bot")
  (<!! (p/start token handler)))
