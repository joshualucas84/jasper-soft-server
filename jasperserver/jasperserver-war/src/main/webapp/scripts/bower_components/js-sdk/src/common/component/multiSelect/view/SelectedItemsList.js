/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * @author Sergey Prilukin
 * @version: $Id: SelectedItemsList.js 380 2014-11-09 15:04:25Z ktsaregradskyi $
 */

/**
 * Selecteditems list. Part of MultiSelect.
 */

define(function (require) {
    'use strict';

    var $ = require("jquery"),
        Backbone = require("backbone"),
        _ = require("underscore"),
        KeyboardManager = require("common/component/singleSelect/manager/KeyboardManager"),
        ListWithNavigation = require("common/component/multiSelect/view/ListViewForSelectedItemsList"),
        ListWithSelectionModel = require("common/component/list/model/ListWithSelectionModel"),
        listWithNavigationModelTrait = require("common/component/list/model/listWithNavigationModelTrait"),
        scalableListItemHeightCalculationTrait = require("common/component/multiSelect/mixin/scalableListItemHeightCalculationTrait"),
        selectedItemsListTemplate = require("text!common/component/multiSelect/templates/selectedItemsTemplate.htm"),
        itemsTemplate = require("text!common/component/multiSelect/templates/selectedItemsListTemplate.htm"),
        listTemplate = require("text!common/component/multiSelect/templates/listTemplate.htm"),
        browserDetection = require("common/util/browserDetection"),
        i18n = require("bundle!js-sdk/ScalableInputControlsBundle"),
        xssUtil = require("common/util/xssUtil");


    var SelectedItemsList = Backbone.View.extend({

        className: "m-Multiselect-listContainer j-toggle-panel j-selected j-inactive jrs",

        attributes: {
            "style": "position: absolute; left: -9999px; top: 0"
        },

        events: function() {
            return {
                "keydown input": this.keyboardManager.onKeydown,
                "focus input": "onFocus",
                "blur input": "onBlur",
                "mouseup li .j-remove-selection": "onMouseupOnDeleteButton",
                "mousedown": "onMousedown",
                "mouseup": "onMouseup"
            };
        },

        keydownHandlers: _.extend({
            "65": "onAKey",
            "8": "onDeleteKey",
            "46": "onDeleteKey"
        }, KeyboardManager.prototype.keydownHandlers),

        initialize: function(options) {
            if (!this.model) {
                this.model = new Backbone.Model();
            }

            this.model.set("focused", false, {silent: true});

            this.template = _.template(selectedItemsListTemplate);

            this.keyboardManager = new KeyboardManager({
                keydownHandlers: this.keydownHandlers,
                keydownTimeout: options.keydownTimeout,
                context: this,
                deferredKeydownHandler: this.processKeydown,
                immediateHandleCondition: this.immediateHandleCondition,
                immediateKeydownHandler: this.immediateKeydownHandler,
                stopPropagation: true
            });

            var ListWithNavigationModel = ListWithSelectionModel.extend(listWithNavigationModelTrait);
            this.listViewModel = options.listViewModel || new ListWithNavigationModel({
                getData: options.getData,
                bufferSize: options.bufferSize,
                loadFactor: options.loadFactor
            });

            this.listView = options.listView || _.extend(new ListWithNavigation({
                el: options.listElement || $(listTemplate),
                model: this.listViewModel,
                chunksTemplate: options.chunksTemplate,
                itemsTemplate: options.itemsTemplate || itemsTemplate,
                scrollTimeout: options.scrollTimeout,
                lazy: true,
                selectedClass: "is-selected",
                selection: {
                    allowed: true,
                    multiple: false
                }
            }), scalableListItemHeightCalculationTrait);

            this.initListeners();
            this.render().resize();
        },

        initListeners: function() {
            this.listenTo(this.listView, "active:changed", this.activeChange, this);
            this.listenTo(this.model, "change:focused", this.focusStateChanged, this);
            this.listenTo(this.model, "change:disabled", this.changeDisabled, this);
            this.listenTo(this.listViewModel, "change", this.onModelChange, this);
        },

        render: function() {
            this.listView.undelegateEvents();

            var selectedItemsList = $(this.template({
                disabled: this.model.get("disabled"),
                i18n:     i18n
            }));

            this.$el
                .empty()
                .append(selectedItemsList)
                .append(this.listView.el);

            this.listView.render();

            this.listView.delegateEvents();

            return this;
        },

        renderData: function() {
            this.listView.renderData();
            return this;
        },

        /* Event Handlers */

        activeChange: function(active) {
            if (active) {
                this.listViewModel.select(active.value, active.index);
                this.listView.scrollTo(active.index);
            } else {
                this.listViewModel.clearSelection();
            }
        },

        changeDisabled: function() {
            var disabled = this.model.get("disabled");

            if (disabled) {
                this.$el.addClass("disabled");
                this.$el.find("input[type='text']").attr("disabled", "disabled");
                this.listView.setValue({});
                this.listView.activate(undefined);
            } else {
                this.$el.find("input[type='text']").removeAttr("disabled");
                this.$el.removeClass("disabled");
            }

            this.listView.setDisabled(disabled);
        },

        focusStateChanged: function() {
            if (this.model.get("focused")) {
                this.$el.find(".mSelect-svListPlaceholder").addClass("focused");
            } else {
                this.$el.find(".mSelect-svListPlaceholder").removeClass("focused");
            }
        },

        onModelChange: function() {
            if (!this.listViewModel.get("total")) {
                this.listView.$el.hide();
            } else {
                this.listView.$el.show();
            }
        },

        onFocus: function() {
            this.model.set("focused", true);
        },

        onBlur: function() {
            if (!this.preventBlur) {
                this.model.set("focused", false);
            }
        },

        onMouseupOnDeleteButton: function(event) {
            this.onDeleteKey(event);
        },

        onMousedown: function() {
            if (!browserDetection.isIPad()) {
                this.preventBlur = true;
                if (!this.model.get("focused")) {
                    this.$el.find("input").focus();
                }
            }
        },

        onMouseup: function() {
            if (this.preventBlur) {
                this.$el.find("input").focus();

                delete this.preventBlur;
            }
        },

        /* Key handlers for KeyboardManager */

        onUpKey: function(event) {
            this.listView.activatePrevious();
        },

        onDownKey: function(event) {
            var active = this.listView.getActiveValue();

            if (active) {
                this.listView.activateNext();
            } else {
                this.listView.activateFirst();
            }
        },

        onEnterKey: function(event) {
            /* do nothing */
        },

        onHomeKey: function(event) {
            this.listView.activateFirst();
        },

        onEndKey: function(event) {
            this.listView.activateLast();
        },

        onPageUpKey: function(event) {
            this.listView.pageUp();
        },

        onPageDownKey: function(event) {
            this.listView.pageDown();
        },

        onTabKey: function() {
            /* do nothing */
        },

        onAKey: function(event) {
            /* do nothing */
        },

        onDeleteKey: function(event) {
            var value = this.listView.getValue();


            if (value && value.length > 0) {
                var emptySelection = true;

                for (var index in value) {
                    if (value.hasOwnProperty(index) && value[index] !== undefined) {
                        emptySelection = false;
                        break;
                    }
                }

                if (!emptySelection) {
                    this.trigger("selection:remove", value);
                }
            }
        },

        /* API */

        fetch: function(callback, options) {
            this.listView.setValue({});
            this.listView.activate(undefined);
            this.listView.fetch(callback, options);
        },

        resize: function() {
            this.listView.resize();
        },

        setDisabled: function(disabled) {
            this.model.set("disabled", disabled);
        },

        getDisabled: function() {
            return this.model.get("disabled");
        },

        remove: function() {
            this.listView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

    return SelectedItemsList;
});

