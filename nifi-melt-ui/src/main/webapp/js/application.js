$(document).ready(function() {
    ua.editable = $('#attribute-updater-editable').text() === 'true';
    ua.init();
});

/**
 * Determine if an `element` has content overflow and adds a colored bottom border if it does.
 *
 * @param {HTMLElement} element The DOM element to toggle .scrollable upon.
 */
var toggleScrollable = function(element) {
    if ($(element).is(':visible')) {
        if (element.offsetHeight < element.scrollHeight ||
            element.offsetWidth < element.scrollWidth) {
            // your element has overflow
            $(element).css({
                'border-bottom': '1px solid #d0dbe0'
            });
        } else {
            $(element).css({
                'border-bottom': '1px solid #ffffff'
            });
        }
    }
};

var ua = {
    newRuleIndex: 0,
    editable: false,

    /**
     * Initializes this web application.
     *
     * @returns {undefined}
     */
    init: function() {
        // configure the dialogs
        ua.initOkDialog();
        ua.initYesNoDialog();


        var mime = 'text/x-mariadb';
        var editor = CodeMirror.fromTextArea(document.getElementById("sqlEditor"), {
                mode: mime,
                indentWithTabs: true,
                smartIndent: true,
                lineNumbers: true,
                matchBrackets: true,
                autofocus: true,
                extraKeys: {
                    "Ctrl-Space": "autocomplete"
                }
        });

        // enable grid resizing
        $(window).resize(function(e) {
            if (e.target === window) {

                // toggle .scrollable when appropriate
                toggleScrollable($('#ctas-expression-panel').get(0));
            }
        });

        toggleScrollable($('#ctas-expression-panel').get(0));

        // initialize the schemas and tables
        ua.initDatabaseList();

        // button click for new conditions/actions
        $('#expression-save').on('click', function() {
            //destroyEditors();
            var ruleId = 1;

            if (ruleId === '') {
                $('#ok-dialog-content').text('No rule is selected.');
                $('#ok-dialog').modal('setHeaderText', 'Configuration Error').modal('show');
            } else {
                // clear the current content
                $('#new-sql-editor').nfeditor('setValue', '');
            }
        });

        // handle rule name changes
        $('#selected-rule-name').on('blur', function() {
            var ruleName = $(this).val();
            var ruleItem = $('#rule-list').children('li.selected');
            var rule = ruleItem.data('rule');
            if (rule.name !== ruleName) {
                ruleItem.addClass('unsaved');
            }
        }).on('focus', function() {
            if (ua.editable) {
                // commit any condition edits
                var conditionsEditController = conditionsGrid.getEditController();
                conditionsEditController.commitCurrentEdit();

            } else {
                ua.removeAllDetailDialogs();
            }
        });

        // show the save button if appropriate
        if (ua.editable) {
            $('#selected-rule-save').show();
            $('#new-rule').show();
            $('#new-condition').show();
            $('#new-action').show();

            // make the combo for the flow file policy
            $('#flowfile-policy').combo({
                options: [{
                    text: 'use clone',
                    value: 'USE_CLONE',
                    description: 'Matching rules are executed with a copy of the original flowfile.'
                }, {
                    text: 'use original',
                    value: 'USE_ORIGINAL',
                    description: 'Matching rules are executed with the original flowfile in the order specified below.'
                }],
                select: function(selectedOption) {
                    var selectedFlowFilePolicy = $('#selected-flowfile-policy').text();

                    // only consider saving the new flowfile policy when appropriate
                    if (selectedFlowFilePolicy !== '' && selectedFlowFilePolicy !== selectedOption.value) {
                        var entity = {
                            processorId: ua.getProcessorId(),
                            revision: ua.getRevision(),
                            clientId: ua.getClientId(),
                            flowFilePolicy: selectedOption.value
                        };

                        $.ajax({
                            type: 'PUT',
                            url: 'api/criteria/evaluation-context',
                            data: JSON.stringify(entity),
                            processData: false,
                            contentType: 'application/json'
                        }).then(function(evaluationContext) {
                            ua.showMessage('FlowFile Policy saved as "' + selectedOption.text + '".');

                            // record the newly selected value
                            $('#selected-flowfile-policy').text(evaluationContext.flowFilePolicy);
                        }, function(xhr, status, error) {
                            // show an error message
                            $('#ok-dialog-content').text('Unable to save new FlowFile Policy due to:' + xhr.responseText);
                            $('#ok-dialog').modal('setHeaderText', 'Error').modal('show');
                        });
                    }
                }
            });
        } else {
            // make the rule name read only when not editable
            $('#selected-rule-name').attr('readonly', 'readonly');

            /*
                        // make the combo for the flow file policy
                        $('#flowfile-policy').combo({
                            options: [{
                                    text: 'use clone',
                                    value: 'USE_CLONE',
                                    description: 'Matching rules are executed with a copy of the original flowfile.',
                                    disabled: true
                                }, {
                                    text: 'use original',
                                    value: 'USE_ORIGINAL',
                                    description: 'Matching rules are executed with the original flowfile in the order specified below.',
                                    disabled: true
                                }]
                        }); */
        }

        // load the schemas and tables
        ua.initDatabaseList

        // initialize the tooltips
        $('.info').qtip({
            style: {
                classes: 'ui-tooltip-tipped ui-tooltip-shadow nifi-tooltip'
            },
            show: {
                solo: true,
                effect: false
            },
            hide: {
                effect: false
            },
            position: {
                at: 'bottom right',
                my: 'top left'
            }
        });
    },



    /**
     * Configure the ok dialog.
     *
     * @returns {undefined}
     */
    initOkDialog: function() {
        $('#ok-dialog').modal({
            overlayBackground: false,
            scrollableContentStyle: 'scrollable',
            buttons: [{
                buttonText: 'Ok',
                color: {
                    base: '#728E9B',
                    hover: '#004849',
                    text: '#ffffff'
                },
                handler: {
                    click: function() {
                        // close the dialog
                        $('#ok-dialog').modal('hide');
                    }
                }
            }],
            handler: {
                close: function() {
                    // clear the content
                    $('#ok-dialog-content').empty();
                }
            }
        });
    },

    /**
     * Configure the yes no dialog.
     *
     * @returns {undefined}
     */
    initYesNoDialog: function() {
        $('#yes-no-dialog').modal({
            scrollableContentStyle: 'scrollable',
            overlayBackground: false
        });
    },

    /**
     * Initializes the rule list.
     *
     * @returns {undefined}
     */
    initDatabaseList: function() {
        console.log('calling init databases!!');

        // handle rule clicks
        var databaseList = $('#table-list');

        // conditionally support reordering
        if (ua.editable) {
            // make the list sortable
            ruleList.sortable({
                helper: 'clone',
                cancel: 'li.unsaved',
                update: function(event, ui) {
                    // attempt save the rule order
                    ua.saveRuleOrder().fail(function() {
                        // and if it fails, revert
                        ruleList.sortable('cancel');
                    });
                }
            });
        }

        databaseList.on('click', 'li', function() {
            var li = $(this);
            var rule = li.data('rule');

            // get the currently selected rule to see if a change is necessary
            var currentlySelectedRuleItem = ruleList.children('li.selected');

            // dont do anything if this is already selected
            if (li.attr('id') === currentlySelectedRuleItem.attr('id')) {
                return;
            }

            // handle saving if necessary
            var saveModifiedRule = $.Deferred(function(deferred) {
                if (currentlySelectedRuleItem.length) {
                    // if we're editable, ensure any active edits are committed before continuing
                    if (ua.editable) {
                        // commit any condition edits
                        var conditionsEditController = conditionsGrid.getEditController();
                        conditionsEditController.commitCurrentEdit();

                        // commit any action edits
                        var actionsEditController = actionsGrid.getEditController();
                        actionsEditController.commitCurrentEdit();
                    }

                    // get the currently selected rule
                    var currentlySelectedRule = currentlySelectedRuleItem.data('rule');

                    // determine if the currently selected rule has been modified
                    if (currentlySelectedRuleItem.hasClass('unsaved')) {
                        $('#yes-no-dialog-content').text('Rule \'' + currentlySelectedRule.name + '\' has unsaved changes. Do you want to save?');
                        $('#yes-no-dialog').modal('setHeaderText', 'Save Changes').modal('setButtonModel', [{
                            buttonText: 'Yes',
                            color: {
                                base: '#728E9B',
                                hover: '#004849',
                                text: '#ffffff'
                            },
                            handler: {
                                click: function() {
                                    // close the dialog
                                    $('#yes-no-dialog').modal('hide');

                                    // save the previous rule
                                    ua.saveSelectedRule().then(function() {
                                        deferred.resolve();
                                    }, function() {
                                        deferred.reject();
                                    });
                                }
                            }
                        }, {
                            buttonText: 'No',
                            color: {
                                base: '#E3E8EB',
                                hover: '#C7D2D7',
                                text: '#004849'
                            },
                            handler: {
                                click: function() {
                                    // close the dialog
                                    $('#yes-no-dialog').modal('hide');

                                    // since changes are being discarded, remove the modified indicator when the rule has been previously saved
                                    if (currentlySelectedRuleItem.attr('id').indexOf('unsaved-rule') === -1) {
                                        currentlySelectedRuleItem.removeClass('unsaved');
                                    }

                                    // no save selected... resolve
                                    deferred.resolve();
                                }
                            }
                        }]).modal('show');
                    } else {
                        deferred.resolve();
                    }
                } else {
                    deferred.resolve();
                }
            }).promise();

            // ensure any modified rule is saved before continuing
            saveModifiedRule.done(function() {
                // select the specified rule
                ua.selectRule(rule).done(function(rule) {
                    // update the selection
                    li.data('rule', rule).addClass('selected').siblings().removeClass('selected');
                });
            });
        }).on('click', 'div.remove-rule', function(e) {
            var li = $(this).closest('li');
            var rule = li.data('rule');

            // remove the rule
            ua.deleteRule(rule).done(function() {
                li.remove();

                // attempt to get the first visible rule
                var firstVisibleRule = ruleList.children('li:visible:first');
                if (firstVisibleRule.length === 1) {
                    firstVisibleRule.click();
                } else {
                    // only hide the rule list when we know there are no rules (could be filtered out)
                    if (ruleList.is(':empty')) {
                        // update the rule list visibility
                        ua.hideRuleList();
                        // clear the selected rule id
                        $('#selected-rule-id').text('');
                    }

                    // clear the rule details
                    ua.clearRuleDetails();
                }

                // re-apply the rule filter
                ua.applyRuleFilter();
            });

            // stop event propagation;
            e.stopPropagation();
        }).children(':first').click();
    },

    /**
     * Saves the current rule order
     */
    saveRuleOrder: function() {
        var ruleList = $('#rule-list');

        // get the proper order
        var ruleOrder = ruleList.sortable('toArray');

        // only include existing rules
        var existingRuleOrder = $.grep(ruleOrder, function(ruleId) {
            return ruleId.indexOf('unsaved-rule-') === -1;
        });

        var entity = {
            processorId: ua.getProcessorId(),
            revision: ua.getRevision(),
            clientId: ua.getClientId(),
            ruleOrder: existingRuleOrder
        };

        return $.ajax({
            type: 'PUT',
            url: 'api/criteria/evaluation-context',
            data: JSON.stringify(entity),
            processData: false,
            contentType: 'application/json'
        }).then(function() {
            ua.showMessage('New rule ordering saved.');
        }, function(xhr, status, error) {
            // show an error message
            $('#ok-dialog-content').text('Unable to reorder the rules due to:' + xhr.responseText);
            $('#ok-dialog').modal('setHeaderText', 'Error').modal('show');
        });
    },


    /**
     * Loads the rule list.
     *
     * @returns {undefined}
     */
    loadTableList: function() {
        var ruleList = $.ajax({
            type: 'GET',
            url: 'api/criteria/rules?' + $.param({
                processorId: ua.getProcessorId(),
                verbose: true
            })
        }).done(function(response) {
            var rules = response.rules;

            // populate the rules
            if (rules && rules.length > 0) {
                // add each rules
                $.each(rules, function(_, rule) {
                    ua.createNewRuleItem(rule);
                });

                // show the listing
                ua.showRuleList();

                // select the first rule
                $('#rule-list').children('li:visible:first').click();
            } else {
                ua.hideRuleList();
            }
        });

        var evaluationContext = $.ajax({
            type: 'GET',
            url: 'api/criteria/evaluation-context?' + $.param({
                processorId: ua.getProcessorId()
            })
        }).done(function(evaluationContext) {
            // record the currently selected value
            $('#selected-flowfile-policy').text(evaluationContext.flowFilePolicy);

            // populate the control
            $('#flowfile-policy').combo('setSelectedOption', {
                value: evaluationContext.flowFilePolicy
            });
        });

        // allow the rule list and evaluation context to load
        return $.Deferred(function(deferred) {
            $.when(ruleList, evaluationContext).then(function() {
                deferred.resolve();
            }, function() {
                $('#ok-dialog-content').text('Unable to load the table list and databases.');
                $('#ok-dialog').modal('setHeaderText', 'Error').modal('show');

                deferred.reject();
            });
        }).promise();
    },

    /**
     * Selects the specified rule and populates its details.
     *
     * @param {object} rule
     * @returns
     */
    selectRule: function(rule) {
        var ruleId = rule.id;

        var conditionsGrid = $('#selected-rule-conditions').data('gridInstance');
        var conditionsData = conditionsGrid.getData();

        var actionsGrid = $('#selected-rule-actions').data('gridInstance');
        var actionsData = actionsGrid.getData();

        return $.Deferred(function(deferred) {
            // if this is an existing rule, load it
            if (ruleId.indexOf('unsaved-rule-') === -1) {
                $.ajax({
                    type: 'GET',
                    url: 'api/criteria/rules/' + encodeURIComponent(ruleId) + '?' + $.param({
                        processorId: ua.getProcessorId(),
                        verbose: true
                    })
                }).then(function(response) {
                    deferred.resolveWith(this, [response.rule]);
                }, function() {
                    // show an error message
                    $('#ok-dialog-content').text('Unable to load details for rule \'' + rule.name + '\'.');
                    $('#ok-dialog').modal('setHeaderText', 'Error').modal('show');

                    // reject the deferred
                    deferred.reject();
                });
            } else {
                deferred.resolveWith(this, [$.extend({
                    conditions: [],
                    actions: []
                }, rule)]);
            }
        }).done(function(selectedRule) {
            if (!ua.editable) {
                // if we are in read only mode, ensure there are no lingering pop ups
                ua.removeAllDetailDialogs();
            }

            // populate the rule details
            $('#selected-rule-id').text(selectedRule.id);
            $('#selected-rule-name').val(selectedRule.name).show();
            $('#no-rule-selected-label').hide();

            // populate the rule conditions
            conditionsGrid.setSortColumn('expression', true);
            conditionsData.setItems(selectedRule.conditions);
            conditionsGrid.invalidate();

            // populate the rule actions
            actionsGrid.setSortColumn('attribute', true);
            actionsData.setItems(selectedRule.actions);
            actionsGrid.invalidate();
        }).promise();
    },



    /**
     * Saves the currently selected rule.
     *
     * @returns {unresolved}
     */
    saveSelectedRule: function() {
        var ruleId = $('#selected-rule-id').text();

        var conditionsGrid = $('#selected-rule-conditions').data('gridInstance');
        var conditionsData = conditionsGrid.getData();

        var actionsGrid = $('#selected-rule-actions').data('gridInstance');
        var actionsData = actionsGrid.getData();

        // ensure a rule was populated
        if (ruleId === '') {
            $('#ok-dialog-content').text('No rule is selected.');
            $('#ok-dialog').modal('setHeaderText', 'Configuration Error').modal('show');
            return;
        }

        // commit any condition edits
        var conditionsEditController = conditionsGrid.getEditController();
        conditionsEditController.commitCurrentEdit();

        // commit any action edits
        var actionsEditController = actionsGrid.getEditController();
        actionsEditController.commitCurrentEdit();

        // marshal the rule
        var rule = {
            name: $('#selected-rule-name').val(),
            conditions: conditionsData.getItems(),
            actions: actionsData.getItems()
        };

        // marshal the entity
        var entity = {
            processorId: ua.getProcessorId(),
            clientId: ua.getClientId(),
            revision: ua.getRevision(),
            rule: rule
        };

        // determine the type of request to make
        var url = 'api/criteria/rules';
        var httpMethod = 'POST';
        if (ruleId.indexOf('unsaved-rule-') === -1) {
            rule['id'] = ruleId;
            httpMethod = 'PUT';
            url = url + '/' + encodeURIComponent(ruleId);
        }

        // create/update the rule
        return $.ajax({
            type: httpMethod,
            url: url,
            data: JSON.stringify(entity),
            processData: false,
            contentType: 'application/json'
        }).then(function(response) {
            var rule = response.rule;

            // update the id of the rule
            $('#selected-rule-id').text(rule.id);

            // update the rule item
            var selectedRuleItem = $('#rule-list').children('li.selected');
            selectedRuleItem.data('rule', rule).attr('id', rule.id).removeClass('unsaved').children('div.rule-label').text(rule.name);

            // indicate that that message was saved
            ua.showMessage('Rule \'' + rule.name + '\' was saved successfully.');
        }, function(xhr, status, error) {
            $('#ok-dialog-content').text(xhr.responseText);
            $('#ok-dialog').modal('setHeaderText', 'Error').modal('show');
        });
    },

    /**
     * Hides the rule list.
     *
     * @returns {undefined}
     */
    hideRuleList: function() {
        $('#table-list-container').hide();
        $('#no-rules').show();
        $('#rule-filter-controls').hide();
    },

    /**
     * Adds a hover effects to the specified selector.
     *
     * @param {type} selector
     * @param {type} normalStyle
     * @param {type} overStyle
     */
    addHoverEffect: function(selector, normalStyle, overStyle) {
        $(document).on('mouseenter', selector, function() {
            $(this).removeClass(normalStyle).addClass(overStyle);
        }).on('mouseleave', selector, function() {
            $(this).removeClass(overStyle).addClass(normalStyle);
        });
        return $(selector).addClass(normalStyle);
    },

    /**
     * Shows the specified text and clears it after 10 seconds.
     *
     * @param {type} text
     * @returns {undefined}
     */
    showMessage: function(text) {
        toggleScrollable($('#message').text(text).get(0));
        setTimeout(function() {
            toggleScrollable($('#message').text('').get(0));
        }, 10000);
    },

    /**
     * Custom validator for required fields.
     *
     * @param {type} value
     */
    requiredFieldValidator: function(value) {
        if (value === null || value === undefined || !value.length) {
            return { valid: false, msg: "This is a required field" };
        } else {
            return { valid: true, msg: null };
        }
    },

    /**
     * Removes all currently open process property detail dialogs.
     */
    removeAllDetailDialogs: function() {
        nf.UniversalCapture.removeAllPropertyDetailDialogs();
    },

    /**
     * Gets the client id.
     *
     * @returns
     */
    getClientId: function() {
        return $('#attribute-updater-client-id').text();
    },

    /**
     * Gets the revision.
     *
     * @returns
     */
    getRevision: function() {
        return $('#attribute-updater-revision').text();
    },

    /**
     * Gets the processor id.
     *
     * @returns
     */
    getProcessorId: function() {
        return $('#attribute-updater-processor-id').text();
    }
};