(ns pomodoro.core
  (:require
   [clojure.string :as s]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(def default-config {:break-length 10
                     :session-length 50
                     :started? false})
(defonce config-state (atom default-config))

(defn reset-config! []
  (reset! config-state default-config))

(defn current-time []
  (.getTime js/Date.))

(defn ms->mins [ms]
  (let [all-secs (/ ms 1000)
        [mins secs] [(quot all-secs 60) (mod all-secs 60)]]
    (str (if (< mins 10) (str "0" mins) mins)
         ":"
         (if (< secs 10) (str "0" secs) secs))))

(defn start-timer! []
  (go-loop []
    (when (:current @config-state)
      (let [{:keys [pause current remain session-length break-length]} @config-state
            remain (or remain (* (if (= current :session) session-length break-length) 60 1000))]
        (cond
          pause
          (<! (timeout 1000))
          
          (pos? remain)
          (do
            (<! (timeout 1000))
            (when (:current @config-state) ;; double check
              (swap! config-state assoc :remain (- remain 1000))))

          :else
          (swap! config-state assoc :current (if (= current :session) :break :session) :remain nil))
        (recur)))))

(defn update-with-key [key func]
  (when-not (:current @config-state)
    (let [nval (func (key @config-state))]
      (when (pos? nval)
        (swap! config-state assoc key nval)))))

(defn start-or-pause-timer! []
  (cond
    (:pause @config-state)
    (swap! config-state assoc :pause nil)

    (not (:current @config-state))
    (do
      (swap! config-state assoc :current :session)
      (start-timer!))

    (:current @config-state)
    (swap! config-state assoc :pause true)))

(defn fill-span []
  (let [{:keys [current session-length remain break-length]} @config-state
        length (when (and remain current)
                 (* (if (= current :session) session-length break-length) 60 1000))
        percent (if current (/ (- length remain) length 0.01) 0)]
    [:span.fill {:style {:height (str percent "%")}}]))

(defn my-app []
  (fn []
    [:div.pomo-main
     [:h3 "Pomodoro Timer"]
     [:div.row
      [:div.timeset
       [:div "BREAK LENGTH"]
       [:div.timeset-btn
        [:button.minus {:type :button :on-click #(update-with-key :break-length dec)} "-"]
        [:span (:break-length @config-state)]
        [:button {:type :button :on-click #(update-with-key :break-length inc)} "+"]]]
      [:div.timeset
       [:div "SESSION LENGTH"]
       [:div.timeset-btn
        [:button.minus {:type :button :on-click #(update-with-key :session-length dec)} "-"]
        [:span (:session-length @config-state)]
        [:button {:type :button :on-click #(update-with-key :session-length inc)} "+"]]]]
     [:div.session-container {:on-click #(start-or-pause-timer!)}
      [:p.ontop.pause (when (:pause @config-state) "paused")]
      [:p.ontop {:class (if (:pause @config-state) "pause" "title")} (name (:current @config-state :session))]
      [:div.ontop.remain (ms->mins (when (:current @config-state)
                                     (:remain @config-state)))]
      [:div.session.overlay
       [fill-span]]]
     [:p]
     [:div [:button.reset {:type :button :on-click #(reset-config!)} "Reset"]]]))

(defn main []
  (reagent/render [#'my-app] (.getElementById js/document "app")))

(main)

