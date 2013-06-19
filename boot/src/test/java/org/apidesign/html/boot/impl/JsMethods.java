/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.boot.impl;

import net.java.html.js.JavaScriptBody;


/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class JsMethods {
    @JavaScriptBody(args = {}, body = "return 42;")
    public static Object fortyTwo() {
        return -42;
    }
    
    @JavaScriptBody(args = {"x", "y" }, body = "return x + y;")
    public static native int plus(int x, int y);
    
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public static native int plus(int x);
    
    @JavaScriptBody(args = {}, body = "return this;")
    public static native Object staticThis();
    
    @JavaScriptBody(args = {}, body = "return this;")
    public native Object getThis();
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public native int plusInst(int x);
}