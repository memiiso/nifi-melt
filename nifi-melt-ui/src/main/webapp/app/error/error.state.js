'use strict';

var ErrorState = function($stateProvider) {

    $stateProvider
        .state('error', {
            url: "/error",
            templateUrl: "app/error/error.view.html"
        })
};

ErrorState.$inject = ['$stateProvider'];
angular.module('meltStandardUI').config(ErrorState);