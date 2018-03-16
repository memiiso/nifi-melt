'use strict';

var MainController = function ($scope, $state, ProcessorService,$location) {

    console.log("Main Controller");
    //Got to state which has a same name with the processor
    ProcessorService.getDetails().then(function(response){
        var processorType = response.data.type; //com.memiiso.nifi.melt.processors.CTAS
        var stateName = processorType.substring(processorType.lastIndexOf(".") + 1, processorType.length).toLowerCase(); //ctas
        console.log("state.go : "+stateName);
        var result = $state.go(stateName,$state.params);
    }).catch(function(response) {
        $state.go('error');
    });
};

MainController.$inject = ['$scope','$state','ProcessorService','$location'];

angular.module('meltStandardUI').controller('MainController', MainController);
