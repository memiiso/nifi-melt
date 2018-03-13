'use strict';

var MainState = function($stateProvider) {

    $stateProvider
        .state('main', {
            url: "/main?id&revision&clientId&editable",
            controller: 'MainController'
        })
};

MainState.$inject = ['$stateProvider'];

angular.module('standardUI').config(MainState);