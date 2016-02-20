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


define(function (require) {
    "use strict";

    var $ = require("jquery"),
        _ = require("underscore"),
        ClassUtil = require('common/util/classUtil');

    return ClassUtil.extend(
        /** @lends AttachableComponent.prototype */
        {

        /**
         * @constructor AttachableComponent
         * @classdesc Component that can be attached to any DOM elemenet
         * @param {(HTMLElement|jQuery)} attachTo - DOM element to attach to
         * @param {object} [padding={top: 5, left: 5}] Padding for component
         * @throw {Error} AttachableComponent should be attached to an element
         */
        constructor: function(attachTo, padding){

            this.padding = padding ? padding : {top: 5, left: 5};

            if (!attachTo || $(attachTo).length === 0) {
                throw new Error("AttachableComponent should be attached to an element");
            }

            this.$attachTo = $(attachTo);
        },

        /**
         * @description Shows component near element.
         */
        show: function(){
            var attachOffset = this.$attachTo.offset(),
                attachHeight = this.$attachTo.height(),
                attachWidth  = this.$attachTo.width();

            var body = $("body"),
                bodyHeight = body.height(),
                bodyWidth = body.width(),
                colorPickerWidth = this.$el.innerWidth(),
                colorPickerHeight = this.$el.innerHeight(),
                fitByHeight = attachOffset.top + attachHeight + this.padding.top,
                fitByWidth =  attachOffset.left;

            var top = attachOffset.top + attachHeight + this.padding.top;
            var left = attachOffset.left;

            if(bodyHeight < colorPickerHeight+fitByHeight){
                top = attachOffset.top - colorPickerHeight - this.padding.top;
            }
            if(bodyWidth < colorPickerWidth+fitByWidth){
                left = attachOffset.left - colorPickerWidth + attachWidth;
            }

            _.extend(this, {top: top, left: left});

            this.$el.css({ top: this.top, left: this.left });

            this.$el.show();

            this.trigger("show", this);
        },

        /**
         * @description hides component
         * @returns {AttachableComponent}
         */
        hide: function() {
            this.$el.hide();

            this.trigger("hide", this);

            return this;
        }
    });
});