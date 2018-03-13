
'use strict';

var CtasExpressionState = function($stateProvider) {

    $stateProvider
        .state('ctas', {
            url: "/ctasexpression?id&revision&clientId&editable",
            templateUrl: "app/ctasexpression/ctasexpression.view.html",
            controller: 'CtasExpressionController',
            resolve: {
                details: ['ProcessorService','$stateParams',
                    function (ProcessorService,$stateParams) {
                        return ProcessorService.getDetails($stateParams.id);
                    }
                ]
            }
        })

};

CtasExpressionState.$inject = ['$stateProvider'];

angular.module('standardUI').config(CtasExpressionState);