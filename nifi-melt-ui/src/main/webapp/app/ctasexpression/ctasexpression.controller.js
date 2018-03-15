'use strict';

var CtasExpressionController = function ($scope, $state, $q, $mdDialog, $timeout, CtasExpressionService, ProcessorService, details, databases) {

    $scope.processorId = '';
    $scope.clientId = '';
    $scope.revisionId = '';
    $scope.editable = false;
    $scope.ctasEditor = {};
    $scope.databaseError = false;

    $scope.inputEditor = {};
    $scope.sortOutput = false;
    $scope.validObj = {};
    $scope.error = '';
    $scope.disableCSS = '';
    $scope.saveStatus = '';
    $scope.expressUpdated = false;
    $scope.variables = {};
    $scope.joltVariables = {};


    $scope.schemas = [];
    $scope.tables = [];
    $scope.columns = [];
    $scope.selectedSchema = '';
    $scope.selectedTable = '';

    $scope.ten = Array.apply(null, {length: 10}).map(Number.call, Number);
    $scope.fifteen = Array.apply(null, {length: 15}).map(Number.call, Number);

    console.log($scope.ten);

    $scope.convertToArray = function (map) {
        var labelValueArray = [];
        angular.forEach(map, function (value, key) {
            labelValueArray.push({'label': value, 'value': key});
        });
        return labelValueArray;
    };

    // Details:
    console.log(details);

    // SB-Infos:
    console.log(databases);

    $scope.getCtasExpression = function (details) {

        if (details['properties']['dbcp-connection-pool'] == null) {
            if (!$scope.databaseError) {
                $scope.databaseError = true;
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Database Error')
                        .textContent('Could not connect to database: please configure the DBCPConnectionPool to obtain a connection to the database')
                        .ariaLabel('Database Error')
                        .ok('OK')
                );
            }
        }
        if (details['properties']['ctas-expression'] != null && details['properties']['ctas-expression'] != "") {
            return details['properties']['ctas-expression'];
        } else {
            return '';
        }
    };

    $scope.populateScopeWithDetails = function (detailsData) {
        $scope.ctasExpression = $scope.getCtasExpression(detailsData);
    };

    $scope.populateScopeWithDetails(details.data);

    $scope.populateScopeWithDBInfos = function (dbDetails) {
        console.log(dbDetails);
        if (dbDetails.status != 200) {
            console.log('foo');
            if (!$scope.databaseError) {
                $scope.databaseError = true;
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Database Error')
                        .textContent('A fatal error occurred. Cannot load the database informations.')
                        .ariaLabel('Database Error')
                        .ok('OK')
                );
            }
        } else {
            console.log('bar');
            var data = dbDetails['data'];
            $scope.schemas = data.schemas;
            if ($scope.schemas != null && $scope.schemas.length > 0) {
                $scope.selectedSchema = $scope.schemas[0];
            }
        }
    };

    $scope.populateScopeWithDBInfos(databases);

    $scope.clearValidation = function () {
        $scope.validObj = {};
    };

    $scope.clearError = function () {
        $scope.error = '';
    };

    $scope.clearSave = function () {
        if ($scope.saveStatus != '') {
            $scope.saveStatus = '';
        }
    };

    $scope.clearMessages = function () {
        $scope.clearSave();
        $scope.clearError();
        $scope.clearValidation();
    };

    $scope.showError = function (message, detail) {
        $scope.error = message;
        console.log('Error received:', detail);
    };

    $scope.initEditors = function (_editor) {
        _editor.setOption('extraKeys', {
            'Shift-F': function (cm) {
                var jsonValue = js_beautify(cm.getDoc().getValue(), {
                    'indent_size': 1,
                    'indent_char': '\t'
                });
                cm.getDoc().setValue(jsonValue)
            }
        });
    };

    $scope.initCtasEditor = function (_editor) {
        $scope.initEditors(_editor);
        $scope.ctasEditor = _editor;

        _editor.on('change', function (cm, changeObj) {
            $scope.clearMessages();
        });

        _editor.clearGutter('CodeMirror-lint-markers');
        /*
                _editor.on('update', function (cm) {
                    if ($scope.transform == 'jolt-transform-sort') {
                        $scope.toggleEditorErrors(_editor, 'hide');
                    }
                });

                _editor.on('change', function (cm, changeObj) {
                    if (!($scope.transform == 'jolt-transform-sort' && changeObj.text.toString() == "")) {
                        $scope.clearMessages();
                        if (changeObj.text.toString() != changeObj.removed.toString()) {
                            $scope.specUpdated = true;
                        }
                    }
                }); */
    };

    $scope.editorProperties = {
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        extraKeys: {"Ctrl-Space": "autocomplete"},
        gutters: ['CodeMirror-lint-markers'],
        mode: 'text/x-sql',
        lint: true,
        value: $scope.ctasExpression,
        onLoad: $scope.initCtasEditor
    };

    $scope.formatEditor = function (editor) {
        var sqlValue = sqlFormatter.format(editor.getDoc().getValue());
        editor.getDoc().setValue(sqlValue);
    };

    $scope.hasJsonErrors = function (input, transform) {
        try {
            jsonlint.parse(input);
        } catch (e) {
            return true;
        }
        return false;
    };

    $scope.toggleEditorErrors = function (editor, toggle) {
        var display = editor.display.wrapper;
        var errors = display.getElementsByClassName("CodeMirror-lint-marker-error");

        if (toggle == 'hide') {
            angular.forEach(errors, function (error) {
                var element = angular.element(error);
                element.addClass('hide');
            });

            var markErrors = display.getElementsByClassName("CodeMirror-lint-mark-error");
            angular.forEach(markErrors, function (error) {
                var element = angular.element(error);
                element.addClass('CodeMirror-lint-mark-error-hide');
                element.removeClass('CodeMirror-lint-mark-error');
            });
        } else {
            angular.forEach(errors, function (error) {
                var element = angular.element(error);
                element.removeClass('hide');
            });

            var markErrors = display.getElementsByClassName("CodeMirror-lint-mark-error-hide");
            angular.forEach(markErrors, function (error) {
                var element = angular.element(error);
                element.addClass('CodeMirror-lint-mark-error');
                element.removeClass('CodeMirror-lint-mark-error-hide');
            });
        }
    };

    $scope.toggleEditor = function (editor, specUpdated) {
        /*
                if (transform == 'jolt-transform-sort') {
                    editor.setOption("readOnly", "nocursor");
                    $scope.disableCSS = "trans";
                    $scope.toggleEditorErrors(editor, 'hide');
                } else { */
        editor.setOption("readOnly", false);
        $scope.disableCSS = "";
        $scope.toggleEditorErrors(editor, 'show');
        // }

        $scope.specUpdated = specUpdated;
        $scope.clearMessages();
    };

    $scope.getJoltSpec = function (transform, jsonSpec, jsonInput) {

        return {
            "transform": transform,
            "specification": jsonSpec,
            "input": jsonInput,
            "customClass": $scope.customClass,
            "modules": $scope.modules,
            "expressionLanguageAttributes": $scope.joltVariables
        };
    };

    $scope.validateJson = function (jsonInput, jsonSpec, transform) {
        var deferred = $q.defer();
        $scope.clearError();
        if (transform == 'jolt-transform-sort' || !$scope.hasJsonErrors(jsonSpec, transform)) {
            var joltSpec = $scope.getJoltSpec(transform, jsonSpec, jsonInput);
            CtasExpressionService.validate(joltSpec).then(function (response) {
                $scope.validObj = response.data;
                deferred.resolve($scope.validObj);
            }).catch(function (response) {
                $scope.showError("Error occurred during validation", response.statusText)
                deferred.reject($scope.error);
            });
        } else {
            $scope.validObj = {"valid": false, "message": "JSON Spec provided is not valid JSON format"};
            deferred.resolve($scope.validObj);
        }

        return deferred.promise;
    };


    // Set values for PropertyDescriptors that are defined in processor
    $scope.getProperties = function (ctasExpress) {

        return {
            "ctas-expression": ctasExpress != "" ? ctasExpress : null
        };
    };

    $scope.saveCtasExpression = function (ctasExpression, processorId, clientId, revisionId) {
        console.log(ctasExpression, processorId, clientId, revisionId);
        $scope.clearError();

        var properties = $scope.getProperties(ctasExpression);

        ProcessorService.setProperties(processorId, revisionId, clientId, properties)
            .then(function (response) {
                var details = response.data;
                $scope.populateScopeWithDetails(details);
                $scope.saveStatus = "Changes saved successfully";
                $scope.specUpdated = false;
            })
            .catch(function (response) {
                $scope.showError("Error occurred during save properties", response.statusText);
            });
    };

    $scope.addVariable = function (variables, key, value) {
        if (key != '' && value != '') {
            variables[key] = value;
        }

        $timeout(function () {
            var scroller = document.getElementById("variableList");
            scroller.scrollTop = scroller.scrollHeight;
        }, 0, false);
    }

    $scope.deleteVariable = function (variables, key) {
        delete variables[key];
    }

    $scope.saveVariables = function (variables) {
        angular.copy($scope.variables, $scope.joltVariables);
        $scope.cancelDialog();
    }

    $scope.cancelDialog = function () {
        $mdDialog.cancel();
    }

    $scope.initController = function (params) {
        console.log('init pararms: ', params);
        $scope.processorId = params.id;
        $scope.clientId = params.clientId;
        $scope.revisionId = params.revision;
        $scope.editable = eval(params.editable);

        /*
                var jsonSpec = $scope.getSpec($scope.transform, $scope.jsonSpec);

                if (jsonSpec != null && jsonSpec != "") {
                    setTimeout(function () {
                        $scope.$apply(function () {
                            $scope.validateJson($scope.jsonInput, jsonSpec, $scope.transform);
                        });
                    });
                } */
    };

    $scope.$watch("ctasEditor", function (newValue, oldValue) {
        $scope.toggleEditor(newValue, false);
    });

    $scope.$watch('selectedSchema', function (newSchema, oldSchema) {
        var tables = newSchema.tables;
        $scope.selectedTable = '';

        if (tables != null) {
            $scope.tables = tables;
            $scope.columns = [];
        }
    });

    // ---------------
    $scope.selectTable = function (selTable) {
        $scope.selectedTable = selTable.name;
        var columns = selTable.columns;
        if (columns != null) {
            $scope.columns = columns;
        }
    };

    $scope.initController($state.params);
};

CtasExpressionController.$inject = ['$scope', '$state', '$q', '$mdDialog', '$timeout', 'CtasExpressionService', 'ProcessorService', 'details', 'databases'];
angular.module('standardUI').controller('CtasExpressionController', CtasExpressionController);
