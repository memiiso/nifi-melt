'use strict';

var MERGEController = function ($scope, $state, $q, $mdDialog, $timeout, ProcessorService, details, databases) {

    $scope.meltEditor = {};
    $scope.databaseError = false;

    $scope.disableCSS = '';
    $scope.infoMessage = '';
    $scope.errorMessage = '';

    $scope.schemas = [];
    $scope.tables = [];
    $scope.columns = [];
    $scope.selectedSchema = '';
    $scope.selectedTable = '';

    $scope.processor = {};

    // Details:
    console.log(details);
    // SB-Infos:
    console.log(databases);

    $scope.getSourceSelect = function (details) {
        if (details['properties']['melt_dbcp_service'] == null) {
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
        if (details['properties']['melt_source'] != null && details['properties']['melt_source'] != "") {
            return details['properties']['melt_source'];
        } else {
            return '';
        }
    };

    $scope.populateScopeWithDetails = function (detailsData) {
        console.log("populateScopeWithDetails");
        $scope.SourceSelect = $scope.getSourceSelect(detailsData);
        $scope.processor = detailsData;
    };

    $scope.populateScopeWithDetails(details.data);

    $scope.populateScopeWithDBInfos = function (databases) {
    console.log("populateScopeWithDBInfos");
        if (databases.status != 200) {
            $scope.databaseError = true;
            $mdDialog.show(
                $mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title('Database Error')
                    .textContent('A fatal error occurred. Cannot load the database informations.\n'+databases.statusText)
                    .ariaLabel('Database Error')
                    .ok('OK')
                );
        } else {
            var data = databases['data'];
            $scope.schemas = data.schemaList;
            if ($scope.schemas != null && data.schemasAsList.length > 0) {
                $scope.selectedSchema = $scope.schemas[data.schemasAsList[0]];
                console.log($scope.schemas[data.schemasAsList[0]]);
            }
        }
    };
    $scope.$watch('selectedSchema', function (newSchema, oldSchema) {
        var tables = newSchema.tableList;
        $scope.selectedTable = '';
        if (tables != null) {
            $scope.tables = tables;
            $scope.columns = [];
        }
    });
    $scope.selectTable = function (selTable) {
        $scope.selectedTable = selTable.tableName;
        var columns = selTable.columnList;
        if (columns != null) {
            $scope.columns = columns;
        }
    };

    $scope.populateScopeWithDBInfos(databases);

    $scope.clearMessages = function () {
        $scope.infoMessage = '';
        $scope.errorMessage = '';
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

    $scope.initMeltEditor = function (_editor) {
        $scope.initEditors(_editor);
        $scope.meltEditor = _editor;

        _editor.on('change', function (cm, changeObj) {
            $scope.clearMessages();
        });

        _editor.clearGutter('CodeMirror-lint-markers');
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
        value: $scope.SourceSelect,
        onLoad: $scope.initMeltEditor
    };

    $scope.formatEditor = function (editor) {
        var sqlValue = sqlFormatter.format(editor.getDoc().getValue());
        editor.getDoc().setValue(sqlValue);
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
        editor.setOption("readOnly", false);
        $scope.disableCSS = "";
        $scope.toggleEditorErrors(editor, 'show');
        $scope.specUpdated = specUpdated;
        $scope.clearMessages();
    };

    $scope.validateSelect = function (selectStatement) {
        var p = ProcessorService.getDetails();
        var myselectStatement = $scope.getProperties(selectStatement);
        ProcessorService.validateSelect(myselectStatement)
            .then(function (response) {
                $scope.infoMessage = response.data.message;
                $scope.specUpdated = false;
            })
            .catch(function (response) {
                $scope.errorMessage = "Error occurred during Validation";
                console.log(response.statusText);
            });
    };

    $scope.runSelect = function (selectStatement) {
        var myselectStatement = $scope.getProperties(selectStatement);
        ProcessorService.validateSelect(myselectStatement)
            .then(function (response) {
                $scope.infoMessage = response.data.message;
                $scope.specUpdated = false;
            })
            .catch(function (response) {
                $scope.errorMessage = "Error occurred during Validation";
                console.log(response.statusText);
            });
    };

    $scope.showELT = function(ev) {
        angular.copy($scope.variables);
        $scope.editorDialogProperties = {
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            extraKeys: {"Ctrl-Space": "autocomplete"},
            gutters: ['CodeMirror-lint-markers'],
            mode: 'text/x-sql',
            lint: true,
            value: $scope.processor.properties.melt_elt_statement,
            readOnly: true
        };
        $mdDialog.show({
            locals: {parent: $scope,
                editorDialogProperties: $scope.editorDialogProperties,
                melt_elt_statement : $scope.processor.properties.melt_elt_statement
            },
            controller: angular.noop,
            controllerAs: 'dialogCtl',
            bindToController: true,
            templateUrl: 'app/processor/dialog-show-melt-elt-statement.html',
            targetEvent: ev,
            clickOutsideToClose: false
        });
    };

    // Set values for PropertyDescriptors that are defined in processor
    $scope.getProperties = function (meltExpress) {
        return {
            "melt_source": meltExpress != "" ? meltExpress : null
        };
    };

    $scope.saveSourceSelect = function (SourceSelect) {
        var properties = $scope.getProperties(SourceSelect);
        ProcessorService.setProperties(properties)
            .then(function (response) {
                var details = response.data;
                $scope.populateScopeWithDetails(details);
                $scope.infoMessage = "Changes saved successfully";
                $scope.specUpdated = false;
            })
            .catch(function (response) {
                $scope.errorMessage = "Error occurred during save";
                console.log(response.statusText);
            });
    };

    $scope.cancelDialog = function () {
        $mdDialog.cancel();
    }

    $scope.$watch("meltEditor", function (newValue, oldValue) {
        $scope.toggleEditor(newValue, false);
    });

};

MERGEController.$inject = ['$scope', '$state', '$q', '$mdDialog', '$timeout', 'ProcessorService', 'details', 'databases'];
angular.module('meltStandardUI').controller('MERGEController', MERGEController);
