/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.html.json.impl;

import org.apidesign.html.json.spi.PropertyBinding;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.impl.PropertyBindingAccessor.FBData;
import org.netbeans.html.json.impl.PropertyBindingAccessor.PBData;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.Technology;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bindings<Data> {
    private Data data;
    private final Technology<Data> bp;

    private Bindings(Technology<Data> bp) {
        this.bp = bp;
    }
    
    public <M> PropertyBinding registerProperty(String propName, M model, SetAndGet<M> access, boolean readOnly) {
        return PropertyBindingAccessor.create(new PBData<M>(this, propName, model, access, readOnly));
    }

    public <M> FunctionBinding registerFunction(String name, M model, Callback<M> access) {
        return PropertyBindingAccessor.createFunction(new FBData<M>(name, model, access));
    }
    
    public static Bindings<?> apply(BrwsrCtx c, Object model) {
        Technology<?> bp = JSON.findTechnology(c);
        return apply(bp);
    }
    
    private static <Data> Bindings<Data> apply(Technology<Data> bp) {
        return new Bindings<Data>(bp);
    }
    
    public final void finish(Object model, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        assert data == null;
        if (bp instanceof Technology.BatchInit) {
            Technology.BatchInit<Data> bi = (Technology.BatchInit<Data>)bp;
            data = bi.wrapModel(model, propArr, funcArr);
        } else {
            data = bp.wrapModel(model);
            for (PropertyBinding b : propArr) {
                bp.bind(b, model, data);
            }
            for (FunctionBinding b : funcArr) {
                bp.expose(b, model, data);
            }
        }
    }
    
    
    public Data koData() {
        return data;
    }

    public void valueHasMutated(String firstName) {
        bp.valueHasMutated(data, firstName);
    }
    
    public void applyBindings() {
        bp.applyBindings(data);
    }

    Object wrapArray(Object[] arr) {
        return bp.wrapArray(arr);
    }
}
