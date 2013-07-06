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
package net.java.html.geo;

import org.testng.annotations.Test;

/**
 */
public class OnLocationTest {
    
    static @OnLocation void onLocation(Position p) {
    }

    @Test public void createOneTimeQueryStatic() {
        GeoHandle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        h.start();
        h.stop();
    }

    @Test public void createRepeatableWatchStatic() {
        GeoHandle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        h.start();
        h.stop();
    }
    
    @OnLocation void instance(Position p) {
    }
    
    @Test public void createOneTimeQueryInstance() {
        OnLocationTest t = new OnLocationTest();
        
        GeoHandle h = InstanceHandle.createQuery(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        h.start();
        h.stop();
    }

    @Test public void createRepeatableWatch() {
        OnLocationTest t = new OnLocationTest();
        
        GeoHandle h = InstanceHandle.createWatch(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        h.start();
        h.stop();
    }
}
