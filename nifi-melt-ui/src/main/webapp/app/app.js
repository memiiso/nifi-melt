'use strict';

var AppRun =  function($rootScope,$state,$http){
    console.log("App Run");
    if (nf.Storage.hasItem('jwt')) {
        var token = nf.Storage.getItem('jwt');
        $http.defaults.headers.common.Authorization = 'Bearer ' + token;
    }

    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error){
        event.preventDefault();
        $state.go('error');
    });

};

// TODO replace!
var AppConfig = function ($urlRouterProvider,$mdThemingProvider) {
    console.log("App Config Run");
    $urlRouterProvider.otherwise(function($injector,$location){
        console.log("App Config urlRouterProvider.otherwise");
        var urlComponents = $location.absUrl().split("?");
        // urlComponents contains:
        //    0:"http://localhost:8080/nifi-melt-ui-1.0-SNAPSHOT/configure"
        //    1:"id=237b918c-0162-1000-eb72-97ffe741c142&revision=0&clientId=241f4e5d-0162-1000-b617-78c40897b974&editable=true"
        //
        return '/main?' + urlComponents[1]; //Got to '/main?id=237b918c-0162-1000-eb72-97ffe741c142&revision=0&clientId=241f4e5d-0162-1000-b617-78c40897b974&editable=true'
    });
};

AppRun.$inject = ['$rootScope','$state','$http'];
AppConfig.$inject = ['$urlRouterProvider'];

angular.module('meltStandardUI', ['ui.codemirror','ui.router','ngMaterial'])
    .run(AppRun)
    .config(AppConfig)
    ;