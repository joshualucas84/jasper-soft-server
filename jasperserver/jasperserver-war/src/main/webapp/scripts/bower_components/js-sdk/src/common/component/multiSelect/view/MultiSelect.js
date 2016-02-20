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
 * @author Sergey Prilukin; modified by: Ken Penn
 * @version: $Id: MultiSelect.js 812 2015-01-27 11:01:30Z psavushchik $
 */

/**
 * tabbed MultiSelect
 * notes: the Available Items list and Selected Items list are interdependent;
 *        the Available Items list uses ListWithSelectionAsObjectHashModel
 *        the Selected Items list uses CacheableDataProvider,
 *          which is dependent on ListWithSelectionAsObjectHashModel
 */

define(function (require) {
    'use strict';

    var $ = require("jquery"),
        Backbone = require("backbone"),
        _ = require("underscore"),
        AvailableItemsList = require("common/component/multiSelect/view/AvailableItemsList"),
        SelectedItemsList = require("common/component/multiSelect/view/SelectedItemsList"),
        SelectedItemsDataProvider = require("common/component/multiSelect/dataprovider/SelectedItemsDataProvider"),
        NumberUtils = require("common/util/parse/NumberUtils"),
        numberUtils = new NumberUtils(),
        browserDetection = require("common/util/browserDetection"),
        multiSelectTemplate = require("text!common/component/multiSelect/templates/multiSelectTemplate.htm"),
        i18n = require("bundle!js-sdk/ScalableInputControlsBundle"),
        xssUtil = require("common/util/xssUtil"),
        doCalcOnVisibleNodeClone = require("common/component/list/util/domAndCssUtil").doCalcOnVisibleNodeClone;

    //css
    require("css!common/js_reset");
    require("css!common/base");
    require("css!common/layout");
    require("css!common/modules");
    require("css!common/state");
    require("css!common/utility");

    var SELECTION_CHANGE_TIMEOUT = 100;

    var MultiSelect = Backbone.View.extend({

        className: "jrs",

        events: function() {
            return {
                "click  .j-toggle-ctrl": "toggleLists"
            };
        },

        initialize: function(options) {
            this.template = _.template(multiSelectTemplate);
            this.visibleItemsCount = options.visibleItemsCount;

            this.i18n = {
                selected  : i18n["sic.multiselect.toggle.selected"],
                available : i18n["sic.multiselect.toggle.available"]
            };

            this.availableItemsListModel = this._createAvailableItemsListModel(options);
            this.availableItemsList = this._createAvailableItemsList(options);
            this.selectedItemsDataProvider = this._createSelectedItemsListDataProvider(options);
            this.selectedItemsList = this._createSelectedItemsList(options);

            this.initListeners();

            // Do not trigger selection changed first time
            if (typeof options.value !== "undefined") {
                this.silent = true;
                this.availableItemsList.setValue(options.value);
            }

            this.render();
        },

        _createAvailableItemsListModel: function(options) {
            return new Backbone.Model();
        },

        _createAvailableItemsList: function(options) {
            return options.availableItemsList || new AvailableItemsList({
                model: this.availableItemsListModel,
                getData: options.getData,
                bufferSize: options.bufferSize,
                loadFactor: options.loadFactor,
                chunksTemplate: options.chunksTemplate,
                scrollTimeout: options.scrollTimeout
            });
        },

        _createSelectedItemsListDataProvider: function(options) {
            return new SelectedItemsDataProvider(options.selectedListOptions);
        },

        _createSelectedItemsList: function(options) {
            this.formatValue = options.formatValue;

            return new SelectedItemsList({
                getData: this.selectedItemsDataProvider.getData,
                bufferSize: options.bufferSize,
                loadFactor: options.loadFactor,
                chunksTemplate: options.chunksTemplate,
                scrollTimeout: options.scrollTimeout
            });
        },

        initListeners: function() {
            this.listenTo(this.availableItemsList, "selection:change", this.selectionChange, this);
            this.listenTo(this.availableItemsListModel, "change:totalValues", this._updateAvailableItemsCountLabel, this);

            this.listenTo(this.selectedItemsList, "selection:remove", this.selectionRemoved, this);
        },

        render: function() {
            var $multiSelect = $(this.template({
                isIPad: browserDetection.isIPad(),
                i18n : i18n
            }));

            this.availableItemsList.undelegateEvents();
            this.selectedItemsList.undelegateEvents();

            this.selectedItemsList.$el.insertAfter($multiSelect.find('.m-Multiselect-toggleContainer'));
            this.availableItemsList.$el.insertAfter($multiSelect.find('.m-Multiselect-toggleContainer'));

            this.$el.empty();
            this.$el.append($multiSelect[0]);

            this.availableItemsList.render();
            this.selectedItemsList.render();

            this._updateAvailableItemsCountLabel();

            this.availableItemsList.delegateEvents();
            this.selectedItemsList.delegateEvents();

            this._tuneCSS();

            return this;
        },

        _tuneCSS: function() {
            var self = this;

            // only need to do this once
            if (!this._cssDepententSizesSet) {
                doCalcOnVisibleNodeClone({
                    el: this.$el,
                    css: {"width": "500px"},
                    alwaysClone: true, //should be true since we modify cloned element
                    callback: function ($el) {
                        self.toggleContainerHeight = $el.find(".m-Multiselect-toggleContainer").outerHeight();
                        //need to fix height of an element copy before measuring it's total height
                        self._tuneCSSInternal($el);
                        $el.find(".j-scalable-list").css({height: "0"});
                        self.emptyContainerHeight = $el.outerHeight();
                    }
                });

                this._cssDepententSizesSet = true;
            }

            this._tuneCSSInternal(this.$el);
        },

        _tuneCSSInternal: function($el) {
            var toggleContainerHeight = this.toggleContainerHeight;

            $el.find('.j-toggle-panel.j-available').css("padding-top", toggleContainerHeight);
            $el.find('.j-toggle-panel.j-selected').css("padding-top", toggleContainerHeight);
            $el.css("height", "100%");
        },


        /* Event Handlers */

        toggleLists : function (evt) {
            evt.stopPropagation();

            if ($(evt.currentTarget).hasClass('is-active')) {
                //tab is already selected. nothing to do here
                return;
            }

            this.$el.find('.j-toggle-ctrl').toggleClass('is-active is-inactive');
            this.$el.find('.j-toggle-panel').toggleClass('j-active j-inactive');

            var inactive = this.$el.find('.j-toggle-panel.j-inactive');
            inactive.css({
                "position": "absolute",
                "left": "-9999px",
                "top": "0",
                "width": inactive.width() + "px"
            });
            this.$el.find('.j-toggle-panel.j-active').css({
                "position": "relative",
                "left": "",
                "top": "",
                "width": ""
            });

            if (!browserDetection.isIPad()) {
                //focus input after toggle so keyboard could be used immediately
                //to navigate through visible list
                this.$el.find('.j-toggle-panel.j-active input').focus();
            }
        },

        selectionChange: function(selection) {
            clearTimeout(this.selectionChangeTimeout);

            this.selectionChangeTimeout = setTimeout(
                _.bind(this.selectionChangeInternal, this, selection), SELECTION_CHANGE_TIMEOUT);
        },

        selectionRemoved: function (selection) {
            // for performance reasons we broke encapsulation here
            // and get raw selection.
            // we even did not make copy of it, since it will be immediately reset
            var currentRawSelection = this.availableItemsList.model.get("value"),
                seletedIndex,
                selectedLength = selection.length;

            for (seletedIndex = 0; seletedIndex < selectedLength; seletedIndex += 1) {
                delete currentRawSelection[selection[seletedIndex]];
            }

            this.availableItemsList.setValue(_.keys(currentRawSelection));
        },

        /* Internal helper methods */

        selectionChangeInternal: function(selection) {
            var self = this,
                activeValue = this.selectedItemsList.listView.getActiveValue(),
                scrollTop = this.selectedItemsList.listView.$el.scrollTop();

            this.selectedItemsDataProvider.setData(selection);
            this.selectedItemsList.fetch(function () {
                self._updateSelectedItemsCountLabel();
                self.selectedItemsList.resize();

                self.selectedItemsList.listView.$el.scrollTop(scrollTop);

                //if selected items list is still visible: preserve it's active element
                if (activeValue && self.selectedItemsList.$el.hasClass("j-active")) {
                    var total = self.selectedItemsList.listView.model.get('total');
                    if (total && total > activeValue.index) {
                        self.selectedItemsList.listView.activate(activeValue.index);
                    } else if (total) {
                        self.selectedItemsList.listView.activate(activeValue.index - 1);
                    }
                }
            });


            if (!this.silent) {
                this.triggerSelectionChange();
            } else {
                delete this.silent;
            }
        },

            // sets label appropriately on tabs
        _setToggleLabel: function (target, count, text) {
            var labelCount = numberUtils.formatNumber(count),
                $labelEl = this.$el.find(target + ' .m-Multiselect-toggleLabel'),
                labelText = text + ': ' +  labelCount;

            $labelEl.text(labelText)
                .attr('title', xssUtil.escape(labelText));
        },

        _updateAvailableItemsCountLabel: function() {
            var total = this.availableItemsList.model.get('totalValues') || 0;
            this._setToggleLabel('.j-available', total || 0, this.i18n.available);
        },

        _updateSelectedItemsCountLabel: function() {
            var $noSelection = this.$el.find('.j-no-selection'),
                count = this.selectedItemsList.listView.model.get('total') || 0;

            this._setToggleLabel('.j-selected', count, this.i18n.selected);

            if (count === 0) {
                $noSelection.show();
            } else {
                $noSelection.hide();
            }
        },

        triggerSelectionChange: function() {
            this.trigger("selection:change", this.getValue());
        },

        /* API */

        renderData: function() {
            this.availableItemsList.renderData();
            this.selectedItemsList.renderData();

            return this;
        },

        fetch: function(callback, options) {
            this.availableItemsList.fetch(callback, options);
        },

        reset: function(options) {
            this.availableItemsList.reset(options);
        },

        resize: function() {
            this.availableItemsList.resize();
            this.selectedItemsList.resize();
        },

        setValue: function(value, options) {
            if (options && options.silent) {
                this.silent = true;
            }

            delete options.silent;
            this.availableItemsList.setValue(value, options);
        },

        getValue: function() {
            var value = this.availableItemsList.getValue();

            //We have to compact result
            var result = [];
            var i = 0;
            for (var index in value) {
                if (value.hasOwnProperty(index) && value[index] !== undefined) {
                    result[i++] = value[index];
                }
            }

            return result;
        },

        setDisabled: function(disabled) {
            this.availableItemsList.setDisabled(disabled);
            this.selectedItemsList.setDisabled(disabled);
            return this;
        },

        getDisabled: function() {
            return this.availableItemsList.getDisabled();
        },

        remove: function() {
            this.availableItemsList.remove();
            this.selectedItemsList.remove();

            Backbone.View.prototype.remove.call(this);
        }
    });

    return MultiSelect;
});

