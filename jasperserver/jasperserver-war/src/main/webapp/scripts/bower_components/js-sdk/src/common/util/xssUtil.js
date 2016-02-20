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

/*global xssUtil */

/**
 * @author: Borys Kolesnykov
 */

define(function (require) {
//    "use strict";

    /*
     This map is used to escape certain chars in the HTML output. This disables javascript in HTML context (XSS attacks).
     Note, it's important that applying this function > 1 times does not escape the string to the next level.
     That is the characters being replaced ( < > = ) should not appear in the substitution strings.  Otherwise,
     multiple escapes will break UI.
     E.g. < is replaced with &lt;. None of the chars (& l t or ;) are found as map values.
     */

    var htmlTagWhiteList = 'a,abbr,acronym,address,area,article,aside,b,bdi,bdo,big,blockquote,body,br,caption,center,' +
        'center,cite,code,col,colgroup,dd,details,dfn,div,dl,dt,em,fieldset,font,footer,form,h1,h2,h3,h4,h5,h6,head,' +
        'header,hr,html,i,iframe,img,input,label,legend,li,main,map,mark,menu,menuitem,meta,nav,ol,option,p,pre,' +
        'section,select,small,span,strike,strong,sub,summary,sup,table,tbody,td,textarea,th,thead,title,tr,u,ul,!--';

    var escapeMap = {
        '(': '&#40;',
        ')': '&#41;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    };

    var makeStringRegex = function(str){
        return str == null ? '' : str.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
    };

    // Need to escape chars like ) and ( in order to construct correct regex later /(?: \)|\( )/g
    var regexKeys = function(map) {
        var arr = [];
        for (var k in map) {
            if (Object.prototype.hasOwnProperty.call(map, k))
                arr.push(makeStringRegex(k));
        }
        return arr;
    };

    var unescapeMap = (function() {
        var map = {
            '&#111;': 'o',
            '&#110;': 'n',
            '&amp;': '&'
        }, k;
        for (k in escapeMap) {
            if (Object.prototype.hasOwnProperty.call(escapeMap, k))
                map[escapeMap[k]] = k;
        }

        return map;
    })();


    //'hard' escape
    var regexStr = '(?:' + regexKeys(escapeMap).join('|') + ')';
    var testRegexp = RegExp(regexStr);
    var replaceRegexp = RegExp(regexStr, 'g');

    //'soft' html escape
    var parenEscapeRegex = '(?:\\)|\\()';
    var testParenRegexp = RegExp(parenEscapeRegex);
    var replaceParenRegexp = RegExp(parenEscapeRegex, 'g');

    var htmlSingleQuoteEscapeRegex = "\\b='(.*?)'";
    var testSingleQuoteRegexp = RegExp(htmlSingleQuoteEscapeRegex);
    var replaceSingleQuoteRegexp = RegExp(htmlSingleQuoteEscapeRegex, 'g');

    var htmlDoubleQuoteEscapeRegex = '\\b="(.*?)"';
    var testDoubleQuoteRegexp = RegExp(htmlDoubleQuoteEscapeRegex);
    var replaceDoubleQuoteRegexp = RegExp(htmlDoubleQuoteEscapeRegex, 'g');

    var htmlLeftTagEscapeRegex = '<(?!/|' + htmlTagWhiteList.replace(/,/g,'|') + ')';
    var testLeftTagRegexp = RegExp(htmlLeftTagEscapeRegex, 'i');
    var replaceLeftTagRegexp = RegExp(htmlLeftTagEscapeRegex, 'ig');

    var htmlLeftTagEscapeRegex2 = '</(?!' + htmlTagWhiteList.replace(/,/g,'|') + ')';
    var testLeftTagRegexp2 = RegExp(htmlLeftTagEscapeRegex2, 'i');
    var replaceLeftTagRegexp2 = RegExp(htmlLeftTagEscapeRegex2, 'ig');

    //beef up javascript protection
    var jsRegex1 = '\\bjavascript:';
    var testJsRegex1 = RegExp(jsRegex1, 'i');
    var replaceJsRegex1 = RegExp(jsRegex1, 'ig');

    var jsRegex2 = '\\bon(\\w+?)=';
    var testJsRegex2 = RegExp(jsRegex2, 'i');
    var replaceJsRegex2 = RegExp(jsRegex2, 'ig');

    var _xssEscape = function(string, options) {
        string = string == null ? '' : string;
        options = options || { };
        if (!(typeof(string) === 'string' || string instanceof String))
            return string;

        if (!options.softHTMLEscape) {
            string = testRegexp.test(string) ? string.replace(replaceRegexp, function(match) { return escapeMap[match]; }) : string;
        }
        else {
            string = testParenRegexp.test(string) ? string.replace(replaceParenRegexp, function(match) { return escapeMap[match]; }) : string;

            string = testSingleQuoteRegexp.test(string) ? string.replace(replaceSingleQuoteRegexp, '=`$1`').replace(/'/g,'&#39;').replace(/\b=`(.*?)`/g,"='$1'") : string.replace(/'/g,'&#39;');
            string = testDoubleQuoteRegexp.test(string) ? string.replace(replaceDoubleQuoteRegexp, '=`$1`').replace(/"/g,'&quot;').replace(/\b=`(.*?)`/g,'="$1"') : string.replace(/"/g,'&quot;');

            if (options.whiteList && options.whiteList instanceof Array && options.whiteList.length > 0) {
                var rtHtmlLeftTagEscapeRegex = '<(?!/|' + options.whiteList.join('|') + ')';
                var rtTestLeftTagRegexp = RegExp(rtHtmlLeftTagEscapeRegex, 'i');
                var rtReplaceLeftTagRegexp = RegExp(rtHtmlLeftTagEscapeRegex, 'ig');
                string = rtTestLeftTagRegexp.test(string) ? string.replace(rtReplaceLeftTagRegexp, '&lt;') : string;

                var rtHtmlLeftTagEscapeRegex2 = '</(?!' + options.whiteList.join('|') + ')';
                var rtTestLeftTagRegexp2 = RegExp(rtHtmlLeftTagEscapeRegex2, 'i');
                var rtReplaceLeftTagRegexp2 = RegExp(rtHtmlLeftTagEscapeRegex2, 'ig');
                string = rtTestLeftTagRegexp2.test(string) ? string.replace(rtReplaceLeftTagRegexp2, '&lt;/') : string;
            }
            else {
                string = testLeftTagRegexp.test(string) ? string.replace(replaceLeftTagRegexp, '&lt;') : string;
                string = testLeftTagRegexp2.test(string) ? string.replace(replaceLeftTagRegexp2, '&lt;/') : string;
            }

            //beef up javascript protection
            string = testJsRegex1.test(string) ? string.replace(replaceJsRegex1, ''): string;
            string = testJsRegex2.test(string) ? string.replace(replaceJsRegex2, '&#111;&#110;$1='): string;
        }

        return string;
    };

    var regexUnescapeStr = '(?:' + regexKeys(unescapeMap).join('|') + ')';
    var testUnescapeRegexp = RegExp(regexUnescapeStr, 'i');
    var replaceUnescapeRegexp = RegExp(regexUnescapeStr, 'ig');
    var _xssUnescape = function(string) {
        string = string == null ? '' : string;
        if (!(typeof(string) === 'string' || string instanceof String)) {
            return string;
        }
        return testUnescapeRegexp.test(string) ? string.replace(replaceUnescapeRegexp, function(match) { return unescapeMap[match]; }) : string;
    };

    /* jshint ignore:start */
    //need to be exposed to global scope without 'window' object
    //because it causes failing of 'optimize' task
    xssUtil = {
        escape: _xssEscape,
        unescape: _xssUnescape,
        noConflict : function(){

        }
    };
    /* jshint ignore:end */

    return xssUtil;

});