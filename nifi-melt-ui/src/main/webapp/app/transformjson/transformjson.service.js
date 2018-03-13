'use strict';

var TransformJsonService = function TransformJsonService($http) {

    return  {
        'validate': validate,
        'execute' : execute,
        'getDatabaseInformation': getDatabaseInformation
    };


    function validate(spec){
        return $http({ url:'api/standard/transformjson/validate',method:'POST', data:spec });
    }

    function execute(spec){
        return $http({ url:'api/standard/transformjson/execute',method:'POST', data:spec,
            transformResponse: [function (data) {
                return data;
            }]});
    }

    function getDatabaseInformation() {
        console.log("TransformJsonService: getDatabaseInformation");
        return $http({ url:'api/melt/ctas/database',method:'GET'});
    }

};

TransformJsonService.$inject = ['$http'];

angular.module('standardUI').factory('TransformJsonService', TransformJsonService);
