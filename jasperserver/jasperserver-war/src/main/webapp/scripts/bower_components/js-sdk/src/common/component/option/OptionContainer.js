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
 * @version: $Id: OptionContainer.js 1309 2015-06-18 19:03:13Z dgorbenk $
 */

define(function (require) {
    "use strict";

    var Backbone = require("backbone"),
        _ = require("underscore"),
        OptionView = require("./OptionView"),
        arrSlice = Array.prototype.slice;

    return Backbone.View.extend({
        el: function() {
            return _.template(this.template)();
        },

        constructor: function(options) {
            validateInput(options);

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

	            // listen to synthetic events from OptionView and trigger the same event with other parameters
                self.listenTo(view, "mouseover", function(optionView, optionViewModel, ev) {
                    self.trigger("mouseover", optionView, self, ev);
                });
                self.listenTo(view, "mouseout", function(optionView, optionViewModel, ev) {
                    self.trigger("mouseout", optionView, self, ev);
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

	        this.trigger("selectionMade");
            this.trigger(this.contextName + ":" + optionModel.get("action"), optionView, optionModel);
        },

        getOptionView: function(value, key) {
            return _.find(this.options, function(option) {
                return option.model.get(key || "action") === value;
            });
        },

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

        select: function(){
            var actions = Array.prototype.slice.call(arguments, 0);

            if(this.toggle){
                _.each(this.options, function(view){
                    ((!actions.length) || (_.contains(actions, view.model.get("action")))) ? view.addSelection() : view.removeSelection();
                }, this);
            }
        },

        deselect: function(){
            var actions = Array.prototype.slice.call(arguments, 0);

            if(this.toggle){
                _.each(this.options, function(view){
                    ((!actions.length) || (_.contains(actions, view.model.get("action")))) && view.removeSelection();
                }, this);
            }
        },

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

            this.trigger("show", this);

            return this;
        },

        hide: function() {
            this.$el.hide();

            this.trigger("hide", this);

            return this;
        },

        enable: function(){
            var args = arrSlice.call(arguments);
            _.each(this.options, function(view){
                ((!args.length) || (_.contains(args, view.model.get("action")))) && view.enable();
            });
        },

        disable: function(){
            var args = arrSlice.call(arguments);
            _.each(this.options, function(view){
                ((!args.length) || (_.contains(args, view.model.get("action")))) && view.disable();
            });
        },

        remove: function() {
            _.invoke(this.options, "remove");

            Backbone.View.prototype.remove.apply(this, arguments);
        }
    });

    function validateInput(options){
        if (!options){
            throw new Error("Init options must be specified");
        }

        if (!options.options || !options.options.length){
            throw new Error("Option views descriptors must be specified");
        }

        if (!options.mainTemplate && !options.el){
            throw new Error("Option container must have a template");
        }

        if (!options.optionTemplate){
            throw new Error("Option container must have an option template");
        }
    }
});