/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleListTest {

    public SimpleListTest() {
    }

    @DataProvider(name = "lists")
    public static Object[][] bothLists() {
        return new Object[][] {
            new Object[] { new ArrayList<Object>() },
            new Object[] { SimpleList.asList() },
        };
    }

    @Test(dataProvider = "lists")
    public void testListIterator(List<String> list) {
        list.add("Hi");
        list.add("Ahoj");
        list.add("Ciao");

        list.sort(String.CASE_INSENSITIVE_ORDER);

        ListIterator<String> it = list.listIterator(3);
        assertEquals(it.previous(), "Hi");
        assertEquals(it.previous(), "Ciao");
        it.remove();
        assertEquals(it.next(), "Hi");
        assertEquals(it.previous(), "Hi");
        assertEquals(it.previous(), "Ahoj");
        assertEquals(list.size(), 2);
    }

    @Test(dataProvider = "lists")
    public void toStringHashTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);
        assertEquals(list.toString(), "[3, 3.3, 4, 4.4]");
        assertEquals(list.hashCode(), 1374332816);
    }

    @Test(dataProvider = "lists")
    public void toStringHashSubListTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        list = list.subList(0, 4);

        assertEquals(list.toString(), "[3, 3.3, 4, 4.4]");
        assertEquals(list.hashCode(), 1374332816);
    }

    @Test(dataProvider = "lists")
    public void subListEqualsTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        assertEquals(list, list.subList(0, 4));
    }

    @Test(dataProvider = "lists")
    public void retainAll(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        list.retainAll(Collections.singleton(4L));

        assertEquals(list.size(), 1);
        assertEquals(list.get(0), 4L);
    }

    @Test(dataProvider = "lists")
    public void retainAllOnSubList(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        List<Number> subList = list.subList(1, 4);

        subList.retainAll(Collections.singleton(4L));

        assertEquals(subList.size(), 1);
        assertEquals(subList.get(0), 4L);

        assertEquals(list.size(), 2);
    }

}
