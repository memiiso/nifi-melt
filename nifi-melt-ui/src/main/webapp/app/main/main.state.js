'use strict';

var MainState = function($stateProvider) {
    console.log("Main State");
    $stateProvider
        .state('main', {
            url: "/main?id&revision&clientId&editable",
            controller: 'MainController'
        })
};

MainState.$inject = ['$stateProvider'];

angular.module('meltStandardUI').config(MainState);