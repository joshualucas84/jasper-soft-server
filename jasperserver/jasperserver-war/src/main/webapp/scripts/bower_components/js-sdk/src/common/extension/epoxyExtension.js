define(function(require){

    var Epoxy = require("backbone.epoxy.original"),
        AttachableColorPicker = require("common/component/colorPicker/SimpleAttachableColorPicker"),
        _ = require("underscore");


    function getAttrName($el, handlerName) {
        var selector = $el.data("bind"),
            pattern = new RegExp(handlerName +  ":(\\w*)\\(?(\\w*)?\\)?");

        return _.last(_.compact(selector.match(pattern)));
    }

    Epoxy.binding.addHandler("validationErrorClass", {
        init: function( $element, value, bindings, context ) {
            this.attr = getAttrName($element, "validationErrorClass");
            var model = this.view.model;

            this._onAttrValidated = function(model, attr, error) {
                $element[error ? "addClass" : "removeClass"]("error");
            };

            model.on("validate:" + this.attr, this._onAttrValidated);
        },
        get: function( $element, value, event ) {
            // Get data from the bound element...
            return $element.val();
        },
        set: function( $element, value ) {
            // Set data into the bound element...
            $element.val( value );
        },
        clean: function() {
            this.view.model.off("validate:" + this.attr, this._onAttrValidated);
        }
    });

    Epoxy.binding.addHandler("validationErrorText", {
        init: function( $element, value, bindings, context ) {
            this.attr = getAttrName($element, "validationErrorText");
            var model = this.view.model;

            this._onAttrValidated = function(model, attr, error) {
                $element.text(error || "");
            };

            model.on("validate:" + this.attr, this._onAttrValidated);
        },
        get: function( $element, value, event ) {
            // Get data from the bound element...
            return $element.val();
        },
        set: function( $element, value ) {
            // Set data into the bound element...
            $element.val( value );
        },
        clean: function() {
            this.view.model.off("validate:" + this.attr, this._onAttrValidated);
        }
    });

    Epoxy.binding.addFilter("escapeCharacters", {
        get: function( value ) {
            // model -> view
            return _.escape(value);
        },
        set: function( value ) {
            // view -> model
            return _.unescape(value);
        }
    });

    Epoxy.binding.addHandler("colorpicker", {
        init: function( $element, value, bindings, context ) {
            var showTransparentInput = !!$element.data("showTransparentInput"),
                label = $element.data("label"),
                attr = getAttrName($element, "colorpicker");

            this.attachableColorPicker = new AttachableColorPicker($element, {top: 5, left: 5}, {label: label, showTransparentInput: showTransparentInput});

            this.callback = function(color){
                bindings[attr](color);
            };

            this.attachableColorPicker.on("color:selected", _.bind(this.callback, this));
        },
        get: function( $element, value, event ) {
            // Get data from the bound element...
            return $element.val();
        },
        set: function( $element, value ) {
            var colorIndicator = $element.find(".colorIndicator");
            this.attachableColorPicker.highlightColor(value);
            colorIndicator.css("background-color", value);
        },
        clean: function() {
            this.attachableColorPicker.off("color:selected", _.bind(this.callback, this));
            this.attachableColorPicker.remove();
        }
    });

    Epoxy.binding.addHandler("radioDiv", {
        init: function( $element, value, bindings, context ) {
            var attr = getAttrName($element, "radioDiv");

            this.callback = function(){
                var value = $element.data("value");
                bindings[attr](value);
            };

            $element.on('click', _.bind(this.callback, this));
        },
        get: function( $element, value, event ) {
            // Get data from the bound element...
            return $element.data("value");
        },
        set: function( $element, value ) {
            var radioDivs = $element.siblings("div[data-bind*='radioDiv:']");

            if($element.data("value") === value){
                $element.addClass('checked');
                $element.children(".radioChild").addClass('checked');
                radioDivs.removeClass('checked');
                radioDivs.children(".radioChild").removeClass('checked');
            }
        }
    });

    Epoxy.binding.addHandler("checkboxDiv", {
        init: function( $element, value, bindings, context ) {
            var attr = getAttrName($element, "checkboxDiv");

            this.callback = function(){
                bindings[attr](!bindings[attr]());
            };

            $element.on('click', _.bind(this.callback, this));
        },
        get: function( $element, value, event ) {
            // Get data from the bound element...
            return $element.data("value");
        },
        set: function( $element, value ) {
            if(value){
                $element.addClass('checked');
                $element.children(".checkboxChild").addClass('checked');
            }else{
                $element.removeClass('checked');
                $element.children(".checkboxChild").removeClass('checked');
            }
        }
    });

    Epoxy.binding.addHandler("slide", function($element, value) {
        $element[value ? "slideDown" : "slideUp"]({
            complete: function() {
                !value && $element.hide();
            }
        });
    });

    /**
     * @type {function}
     * @description Epoxy filter to prepend some text before binding. To pass multi-word text weap it in single quotes.
     * @example
     *  <select data-bind="text:prependText(myAttr, 'multi-word text to prepend')></select>
     *  <select data-bind="text:prependText(myAttr, 'single-word')"></select>
     */

    Epoxy.binding.addFilter("prependText", function(value, text) {
        if (text.charAt(0) === "'" && text.charAt(text.length - 1) === "'") {
            text = text.slice(1, text.length - 1);
        }

        return text + " " + (_.isUndefined(value) ? "" : value);
    });

    return Epoxy;
});