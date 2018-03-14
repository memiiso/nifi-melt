'use strict';

var AppRun =  function($rootScope,$state,$http){

    if (nf.Storage.hasItem('jwt')) {
        var token = nf.Storage.getItem('jwt');
        $http.defaults.headers.common.Authorization = 'Bearer ' + token;
    }

    $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error){
        event.preventDefault();
        $state.go('error');
    });

};

var AppConfig = function ($urlRouterProvider,$mdThemingProvider) {

    $urlRouterProvider.otherwise(function($injector,$location){

        var urlComponents = $location.absUrl().split("?");
        /* urlComponents contains:
            0:"http://localhost:8080/nifi-melt-ui-1.0-SNAPSHOT/configure"
            1:"id=237b918c-0162-1000-eb72-97ffe741c142&revision=0&clientId=241f4e5d-0162-1000-b617-78c40897b974&editable=true"
        */
        return '/main?' + urlComponents[1]; //Got to '/main?id=237b918c-0162-1000-eb72-97ffe741c142&revision=0&clientId=241f4e5d-0162-1000-b617-78c40897b974&editable=true'
    });

    //Define app palettes
    $mdThemingProvider.definePalette('basePalette', {
        '50': '728E9B',
        '100': '728E9B',
        '200': '004849', /* link-color */
        '300': '775351', /* value-color */
        '400': '728E9B',
        '500': '728E9B', /* base-color */
        '600': '728E9B',
        '700': '728E9B',
        '800': '728E9B',
        '900': 'rgba(249,250,251,0.97)', /* tint base-color 96% */
        'A100': '728E9B',
        'A200': '728E9B',
        'A400': '728E9B',
        'A700': '728E9B',
        'contrastDefaultColor': 'light',
        'contrastDarkColors': ['A100'],
        'contrastLightColors': undefined
    });
    $mdThemingProvider.definePalette('tintPalette', {
        '50': '728E9B',
        '100': '728E9B',
        '200': 'CCDADB', /* tint link-color 20% */
        '300': '728E9B',
        '400': 'AABBC3', /* tint base-color 40% */
        '500': '728E9B',
        '600': 'C7D2D7', /* tint base-color 60% */
        '700': '728E9B',
        '800': 'E3E8EB', /* tint base-color 80% */
        '900': '728E9B',
        'A100': '728E9B',
        'A200': '728E9B',
        'A400': '728E9B',
        'A700': '728E9B',
        'contrastDefaultColor': 'light',
        'contrastDarkColors': ['A100'],
        'contrastLightColors': undefined
    });
    $mdThemingProvider.definePalette('warnPalette', {
        '50': 'BA554A',
        '100': 'BA554A',
        '200': 'BA554A',
        '300': 'BA554A',
        '400': 'BA554A',
        '500': 'BA554A', /* warn-color */
        '600': 'BA554A',
        '700': 'BA554A',
        '800': 'BA554A',
        '900': 'BA554A',
        'A100': 'BA554A',
        'A200': 'BA554A',
        'A400': 'BA554A',
        'A700': 'BA554A',
        'contrastDefaultColor': 'light',
        'contrastDarkColors': ['A100'],
        'contrastLightColors': undefined
    });
    $mdThemingProvider.theme("default").primaryPalette("basePalette", {
        "default": "500",
        "hue-1": "200",
        "hue-2": "300",
        "hue-3": "900"
    }).accentPalette("tintPalette", {
        "default": "200",
        "hue-1": "400",
        "hue-2": "600",
        "hue-3": "800"
    }).warnPalette("warnPalette", {
        "default": "500"
    });

};

AppRun.$inject = ['$rootScope','$state','$http'];

AppConfig.$inject = ['$urlRouterProvider','$mdThemingProvider'];

angular.module('standardUI', ['ui.codemirror','ui.router','ngMaterial'])
    .run(AppRun)
    .config(AppConfig);