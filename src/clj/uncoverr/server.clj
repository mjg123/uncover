(ns uncoverr.server
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.resource :as resource]
            [hiccup.core :refer :all]
            [hiccup.page :refer [html5]]
            [compojure.core :refer :all]
            [uncoverr.persistance :as p]
            [clojure.pprint :refer [pprint]]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(def js-datetime-fmt (f/formatter "yyyy-MM-dd'T'HH:mm"))

(def bootstrap-cdn
  [:link
   {:rel "stylesheet"
    :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
    :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
    :crossorigin "anonymous"}])

(defn body [& content]
  (html5 [:head
          [:title "UNCOVERR"]
          bootstrap-cdn
          [:link {:rel "stylesheet" :href "/uncoverr.css"}]]
         [:body
          [:div {:class "container" :role "main"}
           [:h1 {:class "jumbotron"} [:a {:href "/"} "UncoveRR"]]
           [:div {:class "col-md-12"}
            [:p "A pastebin with optional message embargo and client-side group password protection"]
            [:p "Made by " [:a {:href "http://twitter.com/mjgilliard"} "@mjgilliard"] " for allowing remote participants in a game of Diplomacy."]]
           (html content)]]))

(defn message-classes [embargoed?]
  (if embargoed?
    "form-control alert alert-danger"
    "form-control"))

(defroutes main-routes

  (GET "/" []
       (body [:form {:id "theform" :action "/" :method "POST"}
              [:div {:class "col-md-12"}
               [:input {:class "form-control" :name "title" :placeholder "UNTITLED"}]
               [:textarea {:id "message"
                           :class "form-control" :name "message" :autofocus true} "blahblah"]]
              [:div {:class "col-md-5"}
               [:label {:for "datetime-picker"} "Embargo this message until"]
               [:input {:id "datetime-picker"
                        :class "form-control"
                        :type "datetime-local"
                        :name "datetime"
                        :value (f/unparse js-datetime-fmt (t/now))}]]
              [:div {:class "col-md-5"}
               [:label {:for "passwd1"} "Optionally encrypt with one or more passwords"]
               [:input {:id "passwd1" :class "form-control pw-input" :placeholder "password" :autocomplete "off"}]
               [:input {:type "hidden" :id "dummy-last-password"}]
               [:div {:class "btn-group"}
                [:input {:id "encrypt" :type "button" :class "btn btn-info" :value "Encrypt"}]
                [:input {:id "onemorepw" :type "button" :class "btn btn-info" :value "One more password"}]]
               [:input {:id "passwd_cnt" :name "passwd_cnt" :value "0" :type "hidden"}]
               [:div [:small "All non-blank passwords will be required to decrypt"]]]
              [:div {:class "col-md-12"}
               [:input {:class "btn btn-primary" :type "submit" :value "Save"}]]]
             [:script {:src "/triplesec-3.0.14.min.js"}]
             [:script {:src "/encrypt.js"}]))

  (POST "/" {{:keys [message title datetime passwd_cnt] :as p} :params}
        (pprint p)
        (let [parsed-dt (f/parse js-datetime-fmt datetime)
              pw-cnt (Integer/valueOf passwd_cnt)
              id (p/write-message title message parsed-dt pw-cnt)]
          {:status 303
           :headers {"Location" (str "/" id)}}))

  (GET "/:id" [id]
       (when-let [{:keys [title message embargoed? pw-cnt] :as msg} (p/read-message id)]
         (pprint msg)
         {:stats 200
          :body (body
                 [:div {:class "col-md-12"}
                  [:input    {:class "form-control" :readonly true :value title}]
                  [:textarea {:id "message" :class (message-classes embargoed?) :readonly true} message]
                  (when (and (pos? pw-cnt) (not embargoed?))
                    [:div {:class "col-md-5"}
                     [:label {:for "passwd1"} "All passwords are required (in any order)"]
                     (for [x (range pw-cnt)]
                       [:input {:class "form-control pw-input" :placeholder "password " :autocomplete "off"}])
                     [:input {:id "decrypt" :type "button" :class "btn btn-info" :value "Decrypt"}]
                     [:script {:src "/triplesec-3.0.14.min.js"}]
                     [:script {:src "/decrypt.js"}]])])})))

(def app (-> (handler/site main-routes)
             (resource/wrap-resource "public")))
