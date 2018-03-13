'use strict';

var MainController = function ($scope, $state, ProcessorService) {

    console.log("MainController initialization");

    ProcessorService.getType($state.params.id).then(function(response){
        var type = response.data;
        console.log(type);
        var stateName = type.substring(type.lastIndexOf(".") + 1, type.length).toLowerCase();
        var result = $state.go(stateName,$state.params);

    }).catch(function(response) {
        console.log('go error page');
        $state.go('error');
    });

};

MainController.$inject = ['$scope','$state','ProcessorService'];

angular.module('standardUI').controller('MainController', MainController);
