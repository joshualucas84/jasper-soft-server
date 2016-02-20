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
 * Basic option view for buttons, menu options etc.
 *
 * @author: Kostiantyn Tsaregradskyi, Zakhar Tomchenko
 * @version: $Id: OptionView.js 270 2014-10-13 19:58:03Z agodovanets $
 */

define(function (require) {
    "use strict";

    var Backbone = require("backbone"),
        _ = require("underscore");

    return Backbone.View.extend({
        events: {
            "click": "select",
            "mouseover": "mouseover",
            "mouseout": "mouseout"
        },

        /**
         * @constructor OptionView
         * @classdesc Basic option view for buttons, menu options etc.
         * @param {object} options
         * @param {string} options.template - HTML template for option.
         * @param {Backbone.Model} options.model - model representing option.
         * @param {string} [options.disabledClass=[]] - CSS class to add to disabled option.
         * @param {string} [options.hiddenClass=[]] - CSS class to add to hidden option.
         * @throws "Option should have defined template" exception if options.template is not defined.
         * @throws "Option should have associated Backbone.Model" exception if options.model is not defined or is not a Backbone.Model instance.
         * @fires OptionView#mouseover
         * @fires OptionView#mouseout
         */
        constructor: function(options) {
            if (!options || !options.template) {
                throw new Error ("Option should have defined template");
            }

            if (!options.model || !(options.model instanceof Backbone.Model)) {
                throw new Error ("Option should have associated Backbone.Model");
            }

            this.template = _.template(options.template);

            this.disabledClass = options.disabledClass;
            this.hiddenClass = options.hiddenClass;
            this.toggleClass = options.toggleClass;

            Backbone.View.apply(this, arguments);
        },

        initialize: function() {
            Backbone.View.prototype.initialize.apply(this, arguments);

            if (this.model.get("disabled") === true) {
                this.disable();
            }

            if (this.model.get("hidden") === true) {
                this.hide();
            }
        },

        el: function() {
            return this.template(this.model.toJSON());
        },

        /*
         * @method enable
         * @description Enables option.
         */
        enable: function() {
            this.$el.removeAttr("disabled").removeClass(this.disabledClass);
        },

        /*
         * @method disable
         * @description Disables option.
         */
        disable: function() {
            this.$el.attr("disabled", "disabled").addClass(this.disabledClass);
        },

        /*
         * @method show
         * @description Shows option.
         */
        show: function() {
            this.$el.show().removeClass(this.hiddenClass);
        },

        /*
         * @method hide
         * @description Hides option.
         */
        hide: function() {
            this.$el.hide().addClass(this.hiddenClass);
        },

        /*
         * @method isVisible
         * @description Check if option is visible.
         * @returns {boolean}
         */
        isVisible: function() {
            return this.$el.is(":visible");
        },

        /*
         * @method isDisabled
         * @description Check if option is disabled.
         * @returns {boolean}
         */
        isDisabled: function() {
            return this.$el.is(":disabled");
        },

        /*
         * @method select
         * @param {jQuery.Event}
         * @description Click on option event handler.
         * @fires Backbone.Model#select
         */
        select: function(ev) {
            this.model.trigger("select", this, this.model, ev);
        },

        /**
         * @method addSelection
         * @description Adds class to option element
         */
        addSelection: function() {
            this.$el.addClass(this.toggleClass);
        },

        /**
         * @method removeSelection
         * @description Removes class from option element
         */
        removeSelection: function() {
            this.$el.removeClass(this.toggleClass);
        },

        /*
         * @method mouseover
         * @param {jQuery.Event}
         * @description Mouseover on option event handler.
         * @fires OptionView#mouseover
         */
        mouseover: function(ev) {
            this.trigger("mouseover", this, this.model, ev);
        },

        /*
         * @method mouseout
         * @param {jQuery.Event}
         * @description Mouseout on option event handler.
         * @fires OptionView#mouseout
         */
        mouseout: function(ev) {
            this.trigger("mouseout", this, this.model, ev);
        }
    });
});