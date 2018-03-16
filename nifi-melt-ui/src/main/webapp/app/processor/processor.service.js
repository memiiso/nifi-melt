'use strict';

var ProcessorService =  function ProcessorService($http,$location) {
    
    var _processor = {};

    function _setDetails(response){
        var urlParams = $location.search();
        _processor = response;
        _processor.processorId 		= urlParams.id;
        _processor.clientId 		= urlParams.clientId;
        _processor.revisionId 		= urlParams.revision;
        _processor.editable 		= urlParams.editable;
        _processor.processorName 		    = 'error';
        // set processor processorName
        response.then(function(response){
                var processorLongName = response.data.type; //com.memiiso.nifi.melt.processors.CTAS
                var processorName = processorLongName.substring(processorLongName.lastIndexOf(".") + 1, processorLongName.length).toLowerCase(); //ctas
                _processor.processorName=processorName
            }).catch(function(response) {
                _processor.processorName='error'
            });
    }
    function getDetails(){
        var urlParams = $location.search();
        // init if its null or previous request failed
        if (Object.keys(_processor).length === 0 ) {
            console.log("initing");
            var response = $http({ url: 'api/standard/processor/details?processorId=' + urlParams.id, method: 'GET'});
        	_setDetails(response);
        }
        return _processor;
    }


    function setProperties(properties){
        var p = getDetails();
        var urlParams = 'processorId='+p.processorId+'&revisionId='+p.revisionId+'&clientId='+p.clientId;
        var response = $http({
        //TODO make ctas dynamic!
            url: 'api/'+p.processorName+'/setproperties?'+urlParams,
            method:'PUT',
            data:properties
            });
        _setDetails(response);
        return _processor;
    }

    function getDatabaseInformation() {
        var p = this.getDetails();
        console.log("ProcessorService getDatabaseInformation");
        return $http({ url:'api/standard/processor/databaseInformation?processorId=' + p.processorId,method:'GET'});
    }

    function validateSelect(selectStatement) {
       var p = getDetails();
       return $http({
                url: 'api/standard/processor/validateSelect?processorId=' + p.processorId,
                method:'POST',
                data: selectStatement
                });
    }

    function runSelect(selectStatement) {
       var p = getDetails();
       return $http({
                url: 'api/standard/processor/validateSelect?processorId=' + p.processorId,
                method:'POST',
                data: selectStatement
                });
    }

    return  {
        'setProperties': setProperties,
        'getDetails' : getDetails,
        'getDatabaseInformation': getDatabaseInformation,
        'validateSelect': validateSelect,
        'runSelect': runSelect,
    };
};


ProcessorService.$inject = ['$http','$location'];
angular.module('meltStandardUI').factory('ProcessorService', ProcessorService);
