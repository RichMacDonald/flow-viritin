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
package org.vaadin.firitin.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTargetValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.flow.shared.Registration;
import jakarta.servlet.http.Cookie;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.fluency.ui.FluentComponent;
import org.vaadin.firitin.fluency.ui.FluentHasEnabled;
import org.vaadin.firitin.fluency.ui.FluentHasTooltip;

/**
 * An anchor which links to a file whose content is produced dynamically.
 *
 * @author mstahv
 * @see #setFileName(java.lang.String)
 * @see #setFileHandler(com.vaadin.flow.function.SerializableConsumer)
 */
public class DynamicFileDownloader extends Anchor implements
        FluentComponent<DynamicFileDownloader>, FluentHasEnabled<DynamicFileDownloader>,
        FluentHasTooltip<DynamicFileDownloader> {

    private static final int POLLING_INTERVAL = 999;
    /**
     * The request handler that handles the download request.
     */
    protected RequestHandler requestHandler;
    StreamResource resource = new StreamResource("dummy", (InputStreamFactory) () -> new ByteArrayInputStream(new byte[0]));
    FileNameGenerator fileNameGenerator = (r) -> "downloadedFile";
    private Button button;
    private DomListenerRegistration disableOnclick;
    private ContentTypeGenerator contentTypeGenerator = () -> "application/octet-stream";
    private SerializableConsumer<OutputStream> contentWriter;
    private Integer originalPollingInterval;
    private boolean newWindow;
    private UI ui;
    private boolean hasFinishedListeners;
    private boolean hasStartedListeners;

    /**
     * Constructs a basic download link with DOWNLOAD icon from
     * {@link VaadinIcon} as the "text" and default file name.
     *
     * @param writer the callback to generate the contents of the file
     */
    public DynamicFileDownloader(ContentWriter writer) {
        add(new VButton(VaadinIcon.DOWNLOAD.create()));
        setWriter(writer);
    }

    /**
     * Constructs a new download link with given text, static file name and
     * writer.
     *
     * @param linkText      the text inside the link
     * @param contentWriter the content writer that generates the actual
     *                      content.
     */
    public DynamicFileDownloader(String linkText, ContentWriter contentWriter) {
        this();
        setText(linkText);
        setWriter(contentWriter);
    }
    /**
     * Constructs a new download link with given text, static file name and
     * writer.
     *
     * @param linkText      the text inside the link
     * @param fileName      the file name of produced files
     * @param contentWriter the content writer that generates the actual
     *                      content.
     */
    public DynamicFileDownloader(String linkText, String fileName, ContentWriter contentWriter) {
        this();
        setText(linkText);
        this.fileNameGenerator = r -> fileName;
        setWriter(contentWriter);
    }
    /**
     * Constructs a download link with given component as the content that
     * ignites the download.
     *
     * @param downloadComponent the component to be clicked by the user to start
     *                          the download
     * @param fileName          the filename of the generated files
     * @param contentWriter     the content writer of the generated file
     */
    public DynamicFileDownloader(Component downloadComponent, String fileName, ContentWriter contentWriter) {
        this();
        add(downloadComponent);
        fileNameGenerator = r -> fileName;
        setWriter(contentWriter);
    }
    /**
     * Constructs a download link with given component as the content that
     * ignites the download.
     *
     * @param downloadComponent the component to be clicked by the user to start
     *                          the download
     * @param contentWriter     the content writer of the generated file
     */
    public DynamicFileDownloader(Component downloadComponent, ContentWriter contentWriter) {
        this();
        add(downloadComponent);
        setWriter(contentWriter);
    }
    /**
     * Empty constructor file downloader. Be sure to call setFileHandler
     * before the component is attached.
     */
    public DynamicFileDownloader() {
    }

    /**
     * Makes the download link to be disabled after the first click.
     *
     * @param disableOnClick true to disable the link after the first click
     */
    public void setDisableOnClick(boolean disableOnClick) {
        if (disableOnclick != null) {
            disableOnclick.remove();
        }
        if (disableOnClick) {
            getElement().executeJs("""
                    const el = this;
                    this.addEventListener("click", e => {
                        setTimeout(() => el.removeAttribute("href"), 0);
                    });
                    """);
            disableOnclick = getElement().addEventListener("click", e -> {
                setEnabled(false);
            });
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(isAttached()) {
            adjustHref();
        }
    }

    @ClientCallable
    private void ping() {
        // Nothing to do here, possible errors & listener invocations should now be synced
        // if Push is not enabled
    }

    private void setWriter(ContentWriter contentWriter) {
        this.contentWriter = out -> {
            try {
                contentWriter.writeContent(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = attachEvent.getUI();
        prepareRequestHandler(attachEvent);
    }

    private void prepareRequestHandler(AttachEvent attachEvent) {
        ensurePollingOrPush(attachEvent);
        getElement().setAttribute("fakesr", resource);
        String identifier = adjustHref();

        runBeforeClientResponse(ui -> {
            requestHandler = new RequestHandler() {
                @Override
                public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
                    if(hasStartedListeners) {
                        ui.access(() -> {
                            DynamicFileDownloader.this.getEventBus().fireEvent(new DownloadStartedEvent(DynamicFileDownloader.this, false));
                        });
                    }
                    String id = request.getParameter("id");
                    if (id != null && id.equals(identifier)) {
                        response.setStatus(200);
                        String filename = getFileName(session, request);
                        if (filename == null) {
                            filename = fileNameGenerator.getFileName(request);
                        }
                        response.setHeader("Content-Disposition", (newWindow ? "" : "attachment;") + "filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
                        response.setHeader("Content-Type", contentTypeGenerator.getContentType());
                        // Set a cookie to indicate that the file has been downloaded, browser registers after the
                        // download is complete, and we can then hit the server (from the client) to check for possible
                        // errors and UI modifications
                        Cookie marker = new Cookie("filedownloadmarker-" + id, "filewritten");
                        marker.setPath("/");
                        // Client side ought to clear this (if attached), but set a reasonable max age anyways...
                        marker.setMaxAge(60*60);
                        response.addCookie(marker);
                        try {
                            contentWriter.accept(response.getOutputStream());
                        } catch (Exception e) {
                            try {
                                response.setStatus(500);
                            } catch (Exception e2) {
                                // most likely header already sent
                            }
                            getUI().ifPresent(ui -> ui.access(() -> {
                                DynamicFileDownloader.this.getEventBus().fireEvent(new DownloadFailedEvent(DynamicFileDownloader.this, e));
                            }));
                            e.printStackTrace();
                            return true;
                        }
                        if(hasFinishedListeners) {
                            ui.access(() -> {
                                DynamicFileDownloader.this.getEventBus().fireEvent(new DownloadFinishedEvent(DynamicFileDownloader.this, false));
                            });
                        }
                        return true;
                    }
                    return false;
                }
            };

            ui.getSession().addRequestHandler(requestHandler);

            if(!newWindow) {
                getElement().setAttribute("download", "");
            } else {
                setRouterIgnore(true);
                setTarget("_blank");
            }
        });
    }

    private String adjustHref() {
        String identifier = resource.getId();
        VaadinSession session = getUI().get().getSession();
        if(isEnabled()) {
            if(requestHandler != null && !session.getRequestHandlers().contains(requestHandler)) {
                // re-enabling disabled component
                session.addRequestHandler(requestHandler);
            }
            getElement().executeJs("""
                const id = '%s';
                this.setAttribute("href",
                        this.getAttribute("fakesr").substring(0, this.getAttribute("fakesr").indexOf("VAADIN"))
                                + "?v-r=dfd&id=" + id);
                
                this.onclick = e=> {

                    if(this.downloadStartedListener) {
                        setTimeout(() => {
                            this.$server.ping();
                        }, 100);
                    }
                    
                    // start an interval that checks if a cookie with identifier has been set,
                    // if so, stop interval, hit server for possible errors & UI modifications
                    this.interval = setInterval(() => {
                        if(document.cookie.indexOf(id + '=filewritten') > -1) {
                            var d = new Date();
                            d.setDate(d.getDate() - 1);
                            var expires = ";expires=" + d;
                            document.cookie = "filedownloadmarker-"+ id + "=registered" + expires + "; path=/";
                            clearInterval(this.interval);
                            this.$server.ping();
                        }
                    }, 1000);
                }
                """.formatted(identifier));
        } else {
            getElement().executeJs("this.removeAttribute('href');");
            // make sure the request handler can't be access by hacking if disabled
            if(requestHandler != null) {
                session.removeRequestHandler(requestHandler);
            }
        }
        return identifier;
    }

    protected void ensurePollingOrPush(AttachEvent attachEvent) {
        try {
            UI ui = attachEvent.getUI();
            if (ui.getPushConfiguration().getPushMode().isEnabled()) {
                return;
            }
            if (false && ui.getPollInterval() < POLLING_INTERVAL) {
                Logger.getLogger(DynamicFileDownloader.class.getName()).log(Level.INFO, "The UI don't have push enabled, DynamicFileDownloader setting polling interval to " + POLLING_INTERVAL + "ms to make listeners work as expected. Consider enabling push.");
                originalPollingInterval = ui.getPollInterval();
                ui.setPollInterval(POLLING_INTERVAL);
            }
        } catch (Exception e) {
            Logger.getLogger(DynamicFileDownloader.class.getName()).log(Level.WARNING, "Failed to set polling interval", e);
            // ignore, not supported
        }
    }


    @Override
    protected void onDetach(DetachEvent detachEvent) {
        cleanupRequestHandler(detachEvent);
        super.onDetach(detachEvent);
    }

    private void cleanupRequestHandler(DetachEvent detachEvent) {
        try {
            if (originalPollingInterval != null && detachEvent.getUI().getPollInterval() == POLLING_INTERVAL && !detachEvent.getUI().isClosing()) {
                detachEvent.getUI().setPollInterval(originalPollingInterval);
            }
        } catch (Exception e) {
            Logger.getLogger(DynamicFileDownloader.class.getName()).log(Level.WARNING, "Failed to reset polling interval", e);
        }
        detachEvent.getSession().removeRequestHandler(requestHandler);
    }

    /**
     * Adds a listener that is executed when the file content has been streamed.
     *
     * @param listener the listener
     * @return the {@link Registration} you can use to remove this listener.
     */
    public Registration addDownloadFinishedListener(ComponentEventListener<DownloadFinishedEvent> listener) {
        hasFinishedListeners = true;
        return addListener(DownloadFinishedEvent.class, listener);
    }

    /**
     * Adds a listener that is executed when the file content streaming has started.
     *
     * @param listener the listener
     * @return the {@link Registration} you can use to remove this listener.
     */
    public Registration addDownloadStartedListener(ComponentEventListener<DownloadStartedEvent> listener) {
        hasStartedListeners = true;
        getElement().setProperty("downloadStartedListener", true);
        return addListener(DownloadStartedEvent.class, listener);
    }

    /**
     * Adds a listener that is executed when the file content streaming has
     * failed due to an exception. Note that the UI changes done in the listener
     * don't necessarily happen live if you don't have
     * {@link com.vaadin.flow.component.page.Push} in use or use
     * {@link UI#setPollInterval(int)} method.
     *
     * @param listener the listener
     * @return the {@link Registration} you can use to remove this listener.
     */
    public Registration addDownloadFailedListener(ComponentEventListener<DownloadFailedEvent> listener) {
        return addListener(DownloadFailedEvent.class, listener);
    }

    /**
     * Sets the file handler that generates the file content.
     *
     * @param contentWriter the file handler
     */
    public void setFileHandler(SerializableConsumer<OutputStream> contentWriter) {
        this.contentWriter = contentWriter;
    }

    /**
     * Sets the file name of downloaded file.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileNameGenerator = r -> fileName;
    }

    /**
     * Gets the filename of downloaded file. Override if you want to generate
     * the name dynamically.
     *
     * @param session the vaadin session
     * @param request the vaadin request
     * @return the file name
     * @deprecated provide FileNameGenerator instead
     */
    @Deprecated
    protected String getFileName(VaadinSession session, VaadinRequest request) {
        return null;
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Makes the download look like a button instead of a normal link.
     *
     * @return the same instance, fluent method
     */
    public DynamicFileDownloader asButton() {
        String text = getText();
        setText(null);
        this.button = new Button(text);
        add(button);
        return this;
    }

    /**
     * @return a Button component wrapped inside the file downloader, if
     * configured as a Button
     */
    public Button getButton() {
        if (button == null) {
            throw new IllegalStateException("asButton() is not called!");
        }
        return button;
    }

    /**
     * Sets the strategy to creates the name of the downloaded file.
     *
     * @param fileNameGenerator the generator
     */
    public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
        this.fileNameGenerator = fileNameGenerator;
    }

    /**
     * Fluent method to set the strategy to creates the name of the downloaded.
     *
     * @param fileNameGenerator the generator
     * @return the same instance, fluent method
     */
    public DynamicFileDownloader withFileNameGenerator(FileNameGenerator fileNameGenerator) {
        setFileNameGenerator(fileNameGenerator);
        return this;
    }

    public void setContentTypeGenerator(ContentTypeGenerator contentTypeGenerator) {
        this.contentTypeGenerator = contentTypeGenerator;
    }

    public DynamicFileDownloader withContentTypeGenerator(ContentTypeGenerator contentTypeGenerator) {
        setContentTypeGenerator(contentTypeGenerator);
        return this;
    }

    /**
     * @param text the tooltip text
     * @see HasTooltip#setTooltipText(String)
     * <p>
     * Note, that tooltips are only supported if the content of the link
     * supports them. For example, tooltips are supported if the
     * {@link #asButton()} method is called.
     */
    @Override
    public Tooltip setTooltipText(String text) {
        // Anchor does not implement HasTooltip, hack to content
        // (often a Button) -> works
        HasTooltip component = (HasTooltip) getChildren().findFirst().get();
        return component.setTooltipText(text);
    }

    @Override
    public Tooltip getTooltip() {
        HasTooltip component = (HasTooltip) getChildren().findFirst().get();
        return component.getTooltip();
    }

    /**
     * Configures the download to open in a new window and removes the download attribute and
     * content disposition headers instructing to download the target as a file. So essentially
     * we are giving the browser a chance to make a choise what should be done. This allows the component
     * to be used to generated e.g. PDF files on the fly and show them in browser by default.
     *
     * @return the same instance, fluent method
     */
    public DynamicFileDownloader inNewWindow() {
        this.newWindow = true;
        return this;
    }

    /**
     * Writes the content of the downloaded file to the given output stream.
     */
    @FunctionalInterface
    public interface ContentWriter extends Serializable {

        /**
         * Writes the content of the downloaded file to the given output
         * stream (~ output stream of there response).
         *
         * @param out the output stream to write to
         * @throws IOException if an IO error occurs
         */
        void writeContent(OutputStream out) throws IOException;

    }

    /**
     * Generates name dynamically per request. Override for example to add
     * timestamps to the names of the downloaded files or to configure response
     * headers (executed during download, but before writing the actual response
     * body).
     */
    @FunctionalInterface
    public interface FileNameGenerator extends Serializable {

        /**
         * Creates the filename for the downloaded files.
         * <p>
         * Called by the framework when download is requested by browser, just
         * before the file body is generated.
         *
         * @param request the request object
         * @return the file name to be used in the Content-Disposition header
         */
        String getFileName(VaadinRequest request);
    }

    /**
     * Generates the content of HTTP response header 'Content-Type'.
     * If known, should be set to the MIME type of the content.
     * Otherwise, the 'Content-Type' defaults to 'application/octet-stream'.
     * This indicates content as "arbitrary binary data".
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc2046#section-4.5.1">RFC2046</a>
     */
    @FunctionalInterface
    public interface ContentTypeGenerator extends Serializable {

        /**
         * Used as 'Content-Type' HTTP response header.
         *
         * @return MIME type of the content.
         */
        String getContentType();
    }

    /**
     * Event fired when the file download has been streamed to the client.
     */
    public static class DownloadFinishedEvent extends ComponentEvent<DynamicFileDownloader> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the
         *                   client
         */
        public DownloadFinishedEvent(DynamicFileDownloader source, boolean fromClient) {
            super(source, fromClient);
        }

    }

    /**
     * Event fired when the file download has been streamed to the client.
     */
    public static class DownloadStartedEvent extends ComponentEvent<DynamicFileDownloader> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the
         *                   client
         */
        public DownloadStartedEvent(DynamicFileDownloader source, boolean fromClient) {
            super(source, fromClient);
        }

    }

    /**
     * Event fired when the file download fails.
     */
    public static class DownloadFailedEvent extends ComponentEvent<DynamicFileDownloader> {

        private final Exception exception;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source the source component
         * @param e      the exception
         */
        public DownloadFailedEvent(DynamicFileDownloader source, Exception e) {
            super(source, false);
            this.exception = e;
        }

        /**
         * Gets the exception that caused the download to fail.
         *
         * @return the exception
         */
        public Exception getException() {
            return exception;
        }

    }
}
