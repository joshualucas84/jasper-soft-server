/*
 * Copyright (C) 2005 - 2014 Jaspersoft Corporation. All rights reserved.
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
 * @author: Zakhar Tomchenko, Kostiantyn Tsaregradskyi
 * @version: $Id: OptionContainer.js 1605 2015-09-23 17:55:32Z inestere $
 */

define(function (require) {
    "use strict";

    var Backbone = require("backbone"),
        _ = require("underscore"),
        OptionView = require("./OptionView"),
        arrSlice = Array.prototype.slice;

    return Backbone.View.extend(/** @lends OptionContainer.prototype */{
        el: function() {
            return _.template(this.template)();
        },

        /**
         * @constructor OptionContainer
         * @classdesc Generic container for options, buttons, etc.
         * @extends Backbone.View
         * @param {object} options
         * @param {object[]} options.options Option view descriptors.
         * @param {string} options.options[].action ID for option, should be defined and unique.
         * @param {string} options.mainTemplate Template for container.
         * @param {string} options.optionTemplate Template for options.
         * @param {string} [options.contextName="option"] Context of options, e.g. "button", "tab", "item" etc.
         * @param {string} [options.contentContainer="> .content > ul"] Selector for content container.
         * @param {boolean} [options.toggle=false] If options are togglable.
         * @param {string} [options.toggleClass="active"] Class name for active option.
         * @throws {Error} Option views descriptors must be specified
         * @throws {Error} Option container must have a template
         * @throws {Error} Option container must have an option template
         * @fires OptionContainer#mouseout
         * @fires OptionContainer#mouseover
         * @fires OptionContainer#CONTEXT_NAME:OPTION_ACTION
         */
        constructor: function(options) {
            options || (options = {});

            if (!options.options || !options.options.length) {
                throw new Error("Option views descriptors must be specified");
            }

            if (!options.mainTemplate && !options.el) {
                throw new Error("Option container must have a template");
            }

            if (!options.optionTemplate) {
                throw new Error("Option container must have an option template");
            }

            this.contextName = options.contextName || "option";
            this.contentContainer = options.el ? options.contentContainer : options.contentContainer || "> .content > ul";

            this.toggle = !!options.toggle;
            this.toggleClass = options.toggleClass || "active";

            this.options = [];

            this.collection = new Backbone.Collection(options.options);

            this.hasGroups = (this.collection.models[0] && this.collection.models[0].get("groupId")) ? true : false;

            this.defalutOptionView = this.hasGroups
                ? this.collection.where({ "default": true })
                : [this.collection.findWhere({ "default": true })];

            this.listenTo(this.collection, "select", this._selectOption);

            this.template = options.mainTemplate;
            this.optionTemplate = options.optionTemplate;

            Backbone.View.apply(this, arguments);
        },

        setElement: function(el){
            var res = Backbone.View.prototype.setElement.apply(this, arguments);
            this.$contentContainer = this.contentContainer ? this.$(this.contentContainer) : this.$el;
            return res;
        },

        initialize: function() {
            var self = this;

            this.collection.forEach(function(optionModel) {
                var view = new OptionView({
                    template: self.optionTemplate,
                    model: optionModel,
                    toggleClass: self.toggleClass
                });

                self.listenTo(view, "mouseover", function(option, ev) {
                    /**
                     * @event OptionContainer#mouseover
                     * @description This event is fired with information about mouseovered option.
                     */
                    self.trigger("mouseover", option, self, ev);
                });
                self.listenTo(view, "mouseout", function(option, ev) {
                    /**
                     * @event OptionContainer#mouseout
                     * @description This event is fired with information about mouseouted option.
                     */
                    self.trigger("mouseout", option, self, ev);
                });

                self.options.push(view);
                self.$contentContainer.append(view.$el);
            });

            if (this.toggle && this.defalutOptionView) {
                _.each(this.defalutOptionView, function (option) {
                   option && this.getOptionView(option.get("action")).addSelection();
                }, this);
            }
        },

        _onKeyDown: function(e){
            var optionView = this.getOptionView(e.keyCode, "triggerOnKeyCode");

            optionView && !optionView.isDisabled() && optionView.select();
        },

        _selectOption: function (optionView, optionModel) {
            if (this.toggle) {
                var group = optionModel.get("groupId"),
                    optionsToToggle = this.hasGroups ? _.where(this.options, function (option) { return option.model.get("groupId") === group; })
                        : this.options;

                _.each(optionsToToggle, function (optionView) {
                    optionView.removeSelection();
                }, this);

                optionView.addSelection();
            }

            /**
             * @event OptionContainer#CONTEXT_NAME:OPTION_ACTION
             */
            this.trigger(this.contextName + ":" + optionModel.get("action"), optionView, optionModel);
        },

        /**
         * @description Get option view by key.
         * @param {string} value
         * @param {string} [key="action"]
         * @returns {(OptionView|undefined)}
         */
        getOptionView: function(value, key) {
            return _.find(this.options, function(option) {
                return option.model.get(key || "action") === value;
            });
        },

        /**
         * @description Resets selection to initial state.
         * @param {array} [actions=[]] IDs of options
         * @param {boolean} [triggerEvent] If select events should be triggered
         */
        resetSelection: function (actions, triggerEvent) {
            actions = actions || [];

            if (this.toggle) {
                if (this.hasGroups) {
                    if (actions.length < this.defalutOptionView.length) {
                        _.each(this.defalutOptionView, function (model) {
                            var options = {groupId: model.get("groupId")},
                                groupOption = _.findWhere(actions, options);

                            !groupOption && model && actions.push(_.extend(options, {action: model.get("action")}));
                        });
                    }

                    actions = _.pluck(actions, "action");
                } else {
                    (!actions.length && this.defalutOptionView[0]) && actions.push(this.defalutOptionView[0].get("action"));
                }

                _.each(this.options, function (view) {
                    if (actions.length) {
                        if (_.contains(actions, view.model.get("action"))) {
                            !triggerEvent ? view.addSelection() : this._selectOption(view, view.model);
                        } else {
                            view.removeSelection();
                        }
                    }
                }, this);
            }
        },

        /**
         * @description Select options
         * @param {...string} action IDs of options to select
         */
        select: function(action){
            var actions = arrSlice.call(arguments);

            if(this.toggle){
                _.each(this.options, function(view){
                    ((!actions.length) || (_.contains(actions, view.model.get("action")))) ? view.addSelection() : view.removeSelection();
                }, this);
            }
        },

        /**
         * @description Deselect options
         * @param {...string} action IDs of options to deselect
         */
        deselect: function(action){
            var actions = arrSlice.call(arguments);

            if(this.toggle){
                _.each(this.options, function(view){
                    ((!actions.length) || (_.contains(actions, view.model.get("action")))) && view.removeSelection();
                }, this);
            }
        },

        /**
         * @description Show container and options.
         * @fires OptionContainer#show
         */
        show: function() {
            _.each(this.options, function(option) {
                var testFn = option.model.get("test");

                if (testFn && !testFn()) {
                    option.hide();
                } else {
                    option[option.model.get("hidden") ? "hide" : "show"]();
                }
            });

            this.$el.show();

            /** @event OptionContainer#show */
            this.trigger("show", this);

            return this;
        },

        /**
         * @description Hide container and options.
         * @fires OptionContainer#hide
         */
        hide: function() {
            this.$el.hide();

            /** @event OptionContainer#hide */
            this.trigger("hide", this);

            return this;
        },

        /**
         * @description Enable options
         * @param {...string} action IDs of options to enable
         */
        enable: function(action) {
            var args = arrSlice.call(arguments);

            _.each(this.options, function(view){
                ((!args.length) || (_.contains(args, view.model.get("action")))) && view.enable();
            });
        },

        /**
         * @description Disable options
         * @param {...string} action IDs of options to disable
         */
        disable: function(action) {
            var args = arrSlice.call(arguments);

            _.each(this.options, function(view){
                ((!args.length) || (_.contains(args, view.model.get("action")))) && view.disable();
            });
        },

        /**
         * @description Remove container from DOM
         */
        remove: function() {
            _.invoke(this.options, "remove");

            Backbone.View.prototype.remove.apply(this, arguments);
        }
    });
});