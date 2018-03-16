'use strict';

var MeltProcessorState = function ($stateProvider) {

    $stateProvider
        .state('ctas', {
            url: "/ctas?id&revision&clientId&editable",
            templateUrl: "app/processor/CTAS/CTAS.view.html",
            controller: 'CTASController',
            resolve: { //Get all processor infos as id, name, type, properties etc. and save in details
                details: ['ProcessorService', '$stateParams',
                    function (ProcessorService, $stateParams) {
                        return ProcessorService.getDetails();
                    }
                ],
                databases: ['ProcessorService', '$stateParams',
                    function (ProcessorService, $stateParams) {
                        return ProcessorService.getDatabaseInformation();
                    }
                ]
            }
        })
};

MeltProcessorState.$inject = ['$stateProvider'];
angular.module('meltStandardUI').config(MeltProcessorState);