'use strict';

var MainController = function ($scope, $state, ProcessorService) {

    //Got to state wich has a same name with the processor
    ProcessorService.getType($state.params.id).then(function(response){
        var type = response.data; //com.cumpel.nifi.melt.processors.CTAS
        var stateName = type.substring(type.lastIndexOf(".") + 1, type.length).toLowerCase(); //ctas
        var result = $state.go(stateName,$state.params);
    }).catch(function(response) {
        $state.go('error');
    });
};

MainController.$inject = ['$scope','$state','ProcessorService'];

angular.module('standardUI').controller('MainController', MainController);
