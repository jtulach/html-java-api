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
package org.apidesign.html.boot.fx;

import com.sun.javafx.scene.web.Debugger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class FXInspect implements Runnable {
    private static final Logger LOG = Logger.getLogger(FXInspect.class.getName());
    
    
    private final WebEngine engine;
    private final ObjectInputStream input;
    
    private FXInspect(WebEngine engine, int port) throws IOException {
        this.engine = engine;
        
        Socket socket = new Socket(InetAddress.getByName(null), port);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
        initializeDebugger(output);
    }
    
    static void initialize(WebEngine engine) {
        final int inspectPort = Integer.getInteger("netbeans.inspect.port", -1); // NOI18N
        if (inspectPort != -1) {
            try {
                FXInspect inspector = new FXInspect(engine, inspectPort);
                Thread t = new Thread(inspector, "FX<->NetBeans Inspector");
                t.start();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot connect to NetBeans IDE to port " + inspectPort, ex); // NOI18N
            }
        }
    }
    
    private void initializeDebugger(final ObjectOutputStream output) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Debugger debugger = engine.impl_getDebugger();
                debugger.setEnabled(true);
                debugger.setMessageCallback(new Callback<String,Void>() {
                    @Override
                    public Void call(String message) {
                        try {
                            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                            output.writeInt(bytes.length);
                            output.write(bytes);
                            output.flush();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                        return null;
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        try {
            while (true) {
                int length = input.readInt();
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                final String message = new String(bytes, StandardCharsets.UTF_8);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        engine.impl_getDebugger().sendMessage(message);
                    }
                });
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
}