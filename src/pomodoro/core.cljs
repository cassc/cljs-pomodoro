(ns pomodoro.core
  (:require
   [clojure.string :as s]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)



(defn my-app []
  (fn []
    [:div.pomo-main
     [:h3 "Pomodoro Timer"]
     [:div.row
      [:div.timeset
       [:div "BREAK LENGTH"]
       [:div.timeset-btn
        [:button.minus {:type :button} "-"]
        [:span "5"]
        [:button {:type :button} "+"]]]
      [:div.timeset
       [:div "SESSION LENGTH"]
       [:div.timeset-btn
        [:button.minus {:type :button} "-"]
        [:span "55"]
        [:button {:type :button} "+"]]]]
     [:div.session-container
      [:div.session
       [:p "Session"]
       [:div "25"]]
      [:div.session.overlay
       [:span.fill]]]
     [:p]
     [:div
      [:button.reset {:type :button} "Reset"]]]))

(defn main []
  (reagent/render [#'my-app] (.getElementById js/document "app")))

(main)

