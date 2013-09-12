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
package org.apidesign.html.boot.spi;

import java.io.Reader;
import java.net.URL;
import org.apidesign.html.boot.impl.FnUtils;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public abstract class Fn {
    private final Presenter presenter;
    
    protected Fn(Presenter presenter) {
        this.presenter = presenter;
    }
    
    public final boolean isValid() {
        return FnUtils.currentPresenter() == presenter;
    }
    
    public final Object invoke(Object thiz, Object... args) throws Exception {
        return handleInvoke(thiz, args);
    }
    
    protected abstract Object handleInvoke(Object thiz, Object... args) throws Exception;
    
    public interface Presenter {
        public Fn defineFn(String code, String... names);
        public void displayPage(URL page, Runnable onPageLoad);
        public void loadScript(Reader code) throws Exception;
    }
}
