/*
 * Copyright 2018 Viritin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.firitin.components.upload;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.vaadin.firitin.fluency.ui.FluentComponent;
import org.vaadin.firitin.fluency.ui.FluentHasSize;
import org.vaadin.firitin.fluency.ui.FluentHasStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * An upload implementation that just pass the input stream (and name and mime
 * type) of the uploaded file for developer to handle.
 * <p>
 * Note, then FileHandler you pass in is not executed in the UI thread. If you
 * want to modify the UI from it, by sure to use UI.access to handle locking
 * properly.
 * <p>
 * Note, all Upload features are not supported (but the lazy developer is not
 * throwing exceptions on all those methods).
 *
 * @author mstahv
 */
@Tag("vaadin-upload")
public class UploadFileHandler extends Component implements FluentComponent<UploadFileHandler>, FluentHasStyle<UploadFileHandler>, FluentHasSize<UploadFileHandler> {

    public UploadFileHandler allowMultiple() {
        return withAllowMultiple(true);
    }

    @FunctionalInterface
    public interface FileHandler {

        /**
         * This method is called by the framework when a new file is being
         * received.
         * <p>
         * You can read the file contents from the given InputStream.
         * <p>
         * Note, that this method is not executed in the UI thread. If you want
         * to modify the UI from it, by sure to use UI.access (and possibly Push
         * annotation) to handle locking properly.
         *
         * @param content the file content
         * @param fileName the name of the file in users device
         * @param mimeType the mime type parsed from the file name
         */
        public void handleFile(InputStream content, String fileName, String mimeType);
    }


    protected final FileHandler fileHandler;
    private FileRequestHandler frh;
    private boolean clearAutomatically = true;
    private UI ui;

    public UploadFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        withAllowMultiple(false);
        // dummy listener, makes component visit the server after upload,
        // in case no push configured
        addUploadSucceededListener(e -> {
            if(clearAutomatically) {
                frh.files.remove(e.getFileName());
                if(frh.files.isEmpty()) {
                    clearFiles();
                }
            }
        });
        
        //getElement().setProperty("noAuto", true);
        
    }

    public void clearFiles() {
        getElement().executeJs("this.files = [];");
    }

    public UploadFileHandler withAllowMultiple(boolean allowMultiple) {
        if (allowMultiple) {
            withMaxFiles(Integer.MAX_VALUE);
        } else {
            withMaxFiles(1);
        }
        return this;
    }

    public UploadFileHandler withDragAndDrop(boolean enableDragAndDrop) {
        if(enableDragAndDrop) {
            getElement().removeAttribute("nodrop");
        } else {
            getElement().setAttribute("nodrop", true);
        }
        return this;
    }

    public UploadFileHandler withClearAutomatically(boolean clear) {
        this.clearAutomatically = clear;
        return this;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.frh = new FileRequestHandler();
        getElement().setAttribute("target", "./" + frh.id);
        attachEvent.getSession().addRequestHandler(frh);
        
        getElement().executeJs("""
            this.addEventListener("upload-request", e => {
                e.preventDefault(true); // I'll send this instead!!
                const xhr = event.detail.xhr;
                const file = event.detail.file;
                xhr.setRequestHeader('Content-Type', file.type);
                xhr.setRequestHeader('Content-Disposition', 'attachment; filename="' + file.name + '"');
                xhr.send(file);
            });
        """);
        
        this.ui = attachEvent.getUI();
        super.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        detachEvent.getSession().removeRequestHandler(frh);
        ui = null;
        super.onDetach(detachEvent);
    }

    public UploadFileHandler withMaxFiles(int maxFiles) {
        this.getElement().setProperty("maxFiles", (double) maxFiles);
        return this;
    }

    public class FileRequestHandler implements RequestHandler {

        private final String id;

        private List<String> files = new LinkedList<>();

        public FileRequestHandler() {
            this.id = UUID.randomUUID().toString();
        }


        @Override
        public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
            String contextPath = request.getContextPath();
            String pathInfo = request.getPathInfo();
            if (pathInfo.endsWith(id) ) {
                // Vaadin's StreamReceiver & friends has this odd
                // inversion of streams, thus handle here
                // TODO figure out if content type or name needs some sanitation
                String contentType = request.getHeader("Content-Type");
                String cd = request.getHeader("Content-Disposition");
                String name = cd.split(";")[1].split("=")[1].substring(1);
                name = name.substring(0, name.length() - 1);
                fileHandler.handleFile(request.getInputStream(), name, contentType);

                response.setStatus(200);
                response.getWriter().println("OK");  // Viritin approves

                return true;
            }
            return false;
        }

    }

    public Registration addUploadSucceededListener(ComponentEventListener<UploadSucceededEvent> listener) {
        return addListener(UploadSucceededEvent.class, listener);
    }

    @DomEvent("upload-success")
    public static class UploadSucceededEvent
            extends ComponentEvent<UploadFileHandler> {
        private final String fileName;

        public UploadSucceededEvent(UploadFileHandler source,
                                    boolean fromClient, @EventData("event.detail.file.name") String fileName) {
            super(source, fromClient);
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

}
