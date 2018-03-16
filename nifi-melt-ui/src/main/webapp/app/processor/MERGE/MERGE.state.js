'use strict';

var MeltProcessorState = function ($stateProvider) {

    $stateProvider
        .state('merge', {
            url: "/merge?id&revision&clientId&editable",
            templateUrl: "app/processor/MERGE/MERGE.view.html",
            controller: 'MERGEController',
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