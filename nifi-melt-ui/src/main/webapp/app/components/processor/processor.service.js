'use strict';

var ProcessorService =  function ProcessorService($http) {

    console.log("Logging from ProcessorService");

    return  {
        'setProperties': setProperties,
        'getType': getType,
        'getDetails' : getDetails
    };

    function setProperties(processorId,revisionId,clientId,properties){
        var urlParams = 'processorId='+processorId+'&revisionId='+revisionId+'&clientId='+clientId;
        return $http({url: 'api/standard/processor/properties?'+urlParams,method:'PUT',data:properties});
    }

    function getType(id) {
        return $http({
            url: 'api/standard/processor/details?processorId=' + id,
            method: 'GET',
            transformResponse: [function (data) {
                var obj = JSON.parse(data)
                var type = obj['type'];
                return type;
            }]
        });
    }

    function getDetails(id) {
        return $http({ url: 'api/standard/processor/details?processorId=' + id, method: 'GET'});
    }

};

ProcessorService.$inject = ['$http'];

angular.module('standardUI').factory('ProcessorService', ProcessorService);
