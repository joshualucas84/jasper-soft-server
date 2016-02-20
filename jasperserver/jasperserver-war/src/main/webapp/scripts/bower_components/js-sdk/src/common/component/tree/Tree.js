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
 * @author: Zakhar Tomchenko
 * @version: $Id: Tree.js 1760 2015-10-27 18:45:31Z yplakosh $
 */

define(function(require){
    "use strict";

    require("jquery.ui");

    require("css!lists");

    var _ = require('underscore'),
        Backbone = require('backbone'),
        TreeLevel = require("./TreeLevel"),
        TreeDataLayer = require("./TreeDataLayer"),

        listTemplate = require("text!./template/treeLevelListTemplate.htm"),
        levelTemplate = require("text!./template/treeLevelTemplate.htm"),

        ITEM_ACTIONS = {
            ADD: "add",
            UPDATE: "update"
        },

    Tree = Backbone.View.extend({
        template: _.template(levelTemplate),
        _plugins: [],

        el: function() {
            return this.template();
        },

        constructor: function(options) {
            options || (options = {});

            this.defaultDataLayer = new TreeDataLayer(options);
            this.customDataLayers = options.customDataLayers;

            options.collapserSelector || (options.collapserSelector = "b.icon:eq(0)");

            if (options.predefinedData){
                this.defaultDataLayer.predefinedData = options.predefinedData;
                options.predefinedData = undefined;
            } else {
                this.defaultDataLayer.predefinedData = {};
            }

            if (options.rootless){
                this.rootless = options.rootless;
                options.rootless = undefined;
            }

            if (options.additionalCssClasses){
                this.additionalCssClasses = options.additionalCssClasses;
                options.additionalCssClasses = undefined;
            }

            if (options.getDataLayer){
                this.getDataLayer = options.getDataLayer;
                options.getDataLayer = undefined;
            }

            this._options = _.extend({
                itemsTemplate: listTemplate,
                plugins: this._plugins,
                owner: this
            }, options);

            this.context = options.context || {};

            for (var i = 0, l = this._plugins.length; i<l; i++){
                this._plugins[i].processors && (this.processors = this.processors.concat(this._plugins[i].processors));
            }

            Backbone.View.apply(this, arguments);
        },

        initialize: function(options){
            this.rootLevel = new TreeLevel(_.extend({},this._options, {levelHeight: options.rootLevelHeight}, {el: this.$("li")[0]}, {item: { id:"/", value: {id: "/", resourceType: "folder"}}}));

            this.rootless && this.$el.find("ul:first").addClass("hideRoot");
            this.additionalCssClasses && this.$el.find("ul:first").addClass(this.additionalCssClasses);

            this.listenTo(this.rootLevel, "ready", _.bind(addLevels, this));
            this.listenTo(this.rootLevel, "selection:change", _.bind(function(selection, level, visitedLevels){
                if(level && visitedLevels && _.isArray(visitedLevels)){
                    this.rootLevel.selection.multiple || this.rootLevel.resetSelection({silent: true, exclude: [visitedLevels[0]]});
                }
                 this.trigger("selection:change", selection);
            }, this));
            this.listenTo(this.rootLevel, "item:dblclick", _.bind(function(selection){
                this.trigger("item:dblclick", selection);
            }, this));

            for (var i = 0, l = this._plugins.length; i<l; i++){
                this._plugins[i].constr.treeInitialized.call(this, this._plugins[i].options);
            }

            this.rootLevel.render();

            if (options.collapsed && options.lazyLoad && this.rootless){
                this.expand("/");
            }
        },

        remove: function(){
            for (var i = 0, l = this._plugins.length; i<l; i++){
                this._plugins[i].constr.treeRemoved.call(this);
            }

            this.rootLevel.remove();
            Backbone.View.prototype.remove.apply(this, arguments);
        },

        expand: function(nodeId, options){
            var level = this.getLevel(nodeId);
            level && level.open(options);
            return this;
        },

        collapse: function(nodeId, options){
            var level = this.getLevel(nodeId);
            level && level.close(options);
            return this;
        },

        select: function(nodeId) {
            var level = this.getLevel(nodeId);

            if (level) {
                var list = level.parent.list,
                    item = level.item;

                list.model.clearSelection().addValueToSelection(item.value, item.index);

                this.trigger("selection:change", list.getValue());
            }
        },

        deselect: function(nodeId){
            // TODO
        },

        // TODO: this should be more generic
        addItem: function(parentId, itemData) {
            var level = this.getLevel(parentId),
                action = ITEM_ACTIONS.ADD;

            level && this._getNodeItems(parentId, level, itemData, action);

            return this;
        },

        updateItem: function(itemData, parentId) {
            var level = parentId ? this.getLevel(parentId) : this.rootLevel;

            level && this._getNodeItems(level.id, level, itemData);

            return this;
        },

        resetSelection: function(options){
            this.rootLevel.list.clearSelection();
            this.rootLevel.list.model.selection = [];
            this.rootLevel.resetSelection(options);

            return this;
        },

        refresh: function(context){
            context && (this.context = context);
            this.rootLevel.refresh();
        },

        recalcConstraints: function(){
            this.rootLevel.recalcConstraints();
        },

        fetchVisibleData: function() {
            this.rootLevel.fetchVisibleData();
        },

        clearCache: function() {
            this.rootLevel.clearCache();
        },

        getLevel: function(levelId){
            return this.rootLevel.id === levelId ? this.rootLevel : this.rootLevel.getLevel(levelId);
        },

        getDataLayer: function(level){
            return this.customDataLayers && this.customDataLayers[level.id] ?
                this.customDataLayers[level.id] : this.defaultDataLayer;
        },

        _addItem: function(childItems, parentId, itemData) {
            !_.findWhere(childItems, {id: itemData.id}) && childItems.push(itemData);

            return childItems;
        },

        _updateItem: function(childItems, itemData) {
            _.each(childItems, function(item) {
                item.id === itemData.id && (_.extend(item, itemData));
            }, this);

            return childItems;
        },

        _getNodeItems: function(parentId, level, itemData, action) {
            var dataLayer = this.getDataLayer(level),
                childItems = level.list.model.get("items") ? level.list.model.get("items") : [],
                updatedChildItems;

            dataLayer.predefinedData = dataLayer.predefinedData || {};

            updatedChildItems = (action === ITEM_ACTIONS.ADD
                ? this._addItem(childItems, parentId, itemData)
                : this._updateItem(childItems, itemData));

            dataLayer.predefinedData[parentId] = _.sortBy(updatedChildItems, 'label');

            level.list.lazy = false;
            level.list.fetch();
        }
    },{
        instance: function(options){
            return new this(options);
        }
    });

    return {
        use: function(plugin, options){
            return (function(constructor){
               return {
                   use: function(plugin, options){
                       constructor.prototype._plugins.push({constr: plugin, options:options});
                       return this;
                   },
                   create: function(){
                       return constructor;
                   }
               }
            }(Tree.extend({_plugins: [{constr: plugin, options:options}]})));
        },
        create: function(){
            return Tree;
        }
    };

    function addLevels(parentLevel){
        var items = parentLevel.list.model.get("items"), self = this;

        items = _.where(items, {_node: true});

        parentLevel.$(".node").each(function(index, element) {
            var levelName = items[index].id;

            if (parentLevel.items[levelName]){
                parentLevel.items[levelName].setElement(element);
            } else {
                parentLevel.items[levelName] = new TreeLevel(
                    _.extend({}, self._options, {
                            el: element,
                            item: items[index],
                            parent: parentLevel
                        }));

                self.listenTo(parentLevel.items[levelName], 'listRenderError', _.bind(function(responseStatus, error, level) {
                    this.trigger('levelRenderError', responseStatus, error, level);
                }, self));

                parentLevel.listenTo(parentLevel.items[levelName], "ready", _.bind(addLevels, self));

                parentLevel.listenTo(parentLevel.items[levelName], "selection:change", function(selection, level, visitedLevels){
                    level && visitedLevels.push(level);
                    parentLevel.list.model.clearSelection();
                    parentLevel.trigger("selection:change", selection, parentLevel, visitedLevels);
                });
                parentLevel.listenTo(parentLevel.items[levelName], "item:dblclick", function(selection){
                    parentLevel.list.model.clearSelection();
                    parentLevel.trigger("item:dblclick", selection);
                });

                parentLevel.items[levelName].render();
            }
        }).length &&  parentLevel.$(".j-view-port-chunk").css({height: "auto"});
    }
});

