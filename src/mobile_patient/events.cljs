(ns mobile-patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                          dispatch subscribe reg-sub-raw]]
   [mobile-patient.ui :as ui]
   [clojure.spec.alpha :as s]
   [mobile-patient.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; Views
(def ds (ui/ReactNative.ListView.DataSource. #js{:rowHasChanged (fn[a b] false)}))

(def row-comp (r/reactify-component
               (fn [props]
                 (let [row (props :row)
                       navigation (props :navigation)
                       chat @row]
                   [ui/touchable-highlight
                    {:style {:border-top-width 1 :border-color "#000"}
                     :on-press #(println "Chat click")}
                    [ui/text (:name chat)]]))))

(defn ChatScreen [{:keys [navigation]}]
  (let [chats (subscribe [:chats])]
    (fn [_]
      [ui/view
       [ui/list-view {:dataSource (.cloneWithRows ds (clj->js @chats))
                      :render-row (fn [row]
                                    (r/create-element
                                     row-comp
                                     #js{:row row
                                         :navigation navigation}))}]])))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(reg-sub-raw
 :chats
 (fn [db _] (reaction (:chats @db))))

(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (assoc db :chats (vec (map #(r/atom (:resource %)) (:entry value))))))

(defn parms->query [parms]
  (str "?"
       (clojure.string/join
        "&"
        (map #(str
               (js/encodeURIComponent (name (first %)))
               "="
               (js/encodeURIComponent (second %)))
             parms))))

(reg-fx
 :fetch
 (fn [{:keys [:uri :opts :success]}]
   (let [base-url (subscribe [:get-in [:config :base-url]])]
     (-> (js/fetch (str @base-url uri (parms->query (:parms opts)))
                   (clj->js (merge {:method "GET"
                                    :headers {"content-type" "application/json"}}
                                   opts)))
         (.then #(.json %))
         (.then
          (fn [response]
            (dispatch [success (js->clj response :keywordize-keys true)])))
         (.catch #(println %)))
     {})))

(reg-event-fx
 :get-chats
 (fn [cofx [_]]
   {:fetch {:uri "/Chat" :success :on-chats}}))
