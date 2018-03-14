'use strict';

var CtasExpressionState = function ($stateProvider) {

    $stateProvider
        .state('ctas', {
            url: "/ctasexpression?id&revision&clientId&editable",
            templateUrl: "app/ctasexpression/ctasexpression.view.html",
            controller: 'CtasExpressionController',
            resolve: { //Get all processor infos as id, name, type, properties etc. and save in details
                details: ['ProcessorService', '$stateParams',
                    function (ProcessorService, $stateParams) {
                        return ProcessorService.getDetails($stateParams.id);
                    }
                ],
                databases: ['ProcessorService',
                    function (ProcessorService) {
                        return ProcessorService.getCtasDatabase();
                    }
                ]
            }
        })
};

CtasExpressionState.$inject = ['$stateProvider'];
angular.module('standardUI').config(CtasExpressionState);