'use strict';

var CtasExpressionService = function CtasExpressionService($http) {

    return  {
        'validate': validate,
        'execute' : execute,
        'getDatabaseInformation': getDatabaseInformation
    };


    function validate(spec){
        return $http({ url:'api/standard/ctasexpression/validate',method:'POST', data:spec });
    }

    function execute(spec){
        return $http({ url:'api/standard/ctasexpression/execute',method:'POST', data:spec,
            transformResponse: [function (data) {
                return data;
            }]});
    }

    function getDatabaseInformation() {
        return $http({ url:'api/melt/ctas/database',method:'GET'});
    }

};

CtasExpressionService.$inject = ['$http'];

angular.module('standardUI').factory('CtasExpressionService', CtasExpressionService);
