 ;    Copyright 2010 Nathan Marz
 ; 
 ;    This program is free software: you can redistribute it and/or modify
 ;    it under the terms of the GNU General Public License as published by
 ;    the Free Software Foundation, either version 3 of the License, or
 ;    (at your option) any later version.
 ; 
 ;    This program is distributed in the hope that it will be useful,
 ;    but WITHOUT ANY WARRANTY; without even the implied warranty of
 ;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ;    GNU General Public License for more details.
 ; 
 ;    You should have received a copy of the GNU General Public License
 ;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns cascalog.ops-impl
  (:use [cascalog vars util graph])
  (:import [cascading.tuple Fields])
  (:require [cascalog [workflow :as w] [predicate :as p]]))

(defn one [] 1)

(defn existence-int [v] (if v 1 0))

(defn limit-init [options limit]
  (fn [sort-tuple & tuple]
    [[(vec sort-tuple) (vec tuple)]]))

(defn- mk-limit-comparator [options]
  (fn [[#^Comparable o1 _] [#^Comparable o2 _]]
    (if (:sort options)
      (* (.compareTo o1 o2) (if (boolean (:reverse options)) -1 1))
      0 )))

(defn limit-combine [options limit]
  (let [compare-fn (mk-limit-comparator options)]
   (fn [list1 list2]
      (let [res (concat list1 list2)]
        (if (> (count res) (* 2 limit))
          (take limit (sort compare-fn res))
          res
          ))
      )))

(defn limit-extract [options limit]
  (let [compare-fn (mk-limit-comparator options)]
  (fn [alist]
    (let [alist (if (<= (count alist) limit) alist (take limit (sort compare-fn alist)))]
      (map (partial apply concat) alist)))))

(defn limit-buffer [options limit]
  (fn [tuples]
    (take limit tuples)))

(defn limit-rank-buffer [options limit]
  (fn [tuples]
    (take limit (map #(conj (vec %1) %2) tuples (iterate inc 1)))))