"use strict";angular.module("angularApp",[]).config(["$routeProvider",function(a){a.when("/",{templateUrl:"views/main.html",controller:"MainCtrl"}).otherwise({redirectTo:"/"})}]),angular.module("angularApp").controller("MainCtrl",["$scope","$http",function(a,b){a.error=[];var c=function(){b.get("/score").then(function(b){a.score=b.data},function(b){a.error.push([b.status,b.data].join(" : ")),a.error.push("Vous devriez implémenter la méthode doGet() de la servlet."),a.score="N/A"})};c();var d=function(d){a.error=[],b({method:"POST",url:"/score",data:$.param({player:d}),headers:{"Content-Type":"application/x-www-form-urlencoded"}}).then(function(){c()},function(b){400==b.status?a.error.push("Le jeu est terminé"):(a.error.push([b.status,b.data].join(" : ")),a.error.push("Vous devriez implementer la méthode doPost() de la servlet."))})};a.scoreP1=function(){d("1")},a.scoreP2=function(){d("2")},a.reset=function(){a.error=[],b.delete("/score").then(function(){c()},function(b){a.error.push([b.status,b.data].join(" : ")),a.error.push("Vous devriez implementer la méthode doDelete() de la servlet.")})}}]);