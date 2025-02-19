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
package org.vaadin.firitin;

import com.helger.commons.mutable.MutableInt;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.router.Route;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.upload.UploadFileHandler;

/**
 *
 * @author mstahv
 */
@Route
public class UploadFileHandlerExample extends VerticalLayout {

    private static final String DELIMITER = ";";

    public UploadFileHandlerExample() {

        Paragraph liveLogger = new Paragraph("...");
        UI ui = UI.getCurrent();
        UploadFileHandler uploadFileHandler = new UploadFileHandler(
                (InputStream content, String fileName, String mimeType) -> {
                    try {
                        long lastUpdate = System.currentTimeMillis();
                        int b = 0;
                        int count = 0;
                        while ((b = content.read()) != -1) {
                            if (b == "\n".getBytes()[0]) {
                                count++;
                                if((System.currentTimeMillis()-lastUpdate) > 200) {
                                    // Modifying UI during handling, to show this
                                    // is possible, see https://stackoverflow.com/questions/75165362/vaadin-flow-upload-component-streaming-upload
                                    int curcount = count;
                                    ui.access(() -> liveLogger.setText("counting... (%s)".formatted(curcount)));
                                    lastUpdate = System.currentTimeMillis();
                                }
                            }
                        }
                        String msg = "Counted " + count + "lines. Filename:" + fileName;
                        ui.access(() -> {
                            Notification.show(msg);
                            liveLogger.setText("...");
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(UploadFileHandlerExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

        MutableInt lineCount = new MutableInt(0);

        UploadFileHandler multiUploadFileHandler = new UploadFileHandler(
                (InputStream content, String fileName, String mimeType) -> {
                    try {
                        int b = 0;
                        int count = 0;
                        while ((b = content.read()) != -1) {
                            if (b == "\n".getBytes()[0]) {
                                count++;
                            }
                        }
                        lineCount.inc(count);
                        String msg = "Counted " + lineCount + "lines so far. Last file name " + fileName;
                        getUI().get().access(() -> Notification.show(msg));
                    } catch (IOException ex) {
                        Logger.getLogger(UploadFileHandlerExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).allowMultiple()
                .withUploadButton(new VButton(VaadinIcon.UPLOAD.create()));

        Grid<String[]> grid = new Grid<>();

        UploadFileHandler csvFileHandler = new UploadFileHandler(
                (InputStream content, String fileName, String mimeType) -> {
                    try {

                        BufferedReader br = new BufferedReader(new InputStreamReader(content));
                        String line = br.readLine();

                        getUI().get().access(() -> {

                        });
                        String[] headers = line.split(";");
                        getUI().get().access(() -> {
                            grid.removeAllColumns();
                            // create headers from first row
                            for (int i = 0; i < headers.length; i++) {
                                int index = i;
                                grid.addColumn(lineDataArray -> {
                                    return lineDataArray[index];
                                }).setHeader(headers[index]);
                            }

                        });

                        // Collect data
                        List<String[]> records = new ArrayList<>();
                        while ((line = br.readLine()) != null) {
                            String[] values = line.split(DELIMITER);
                            records.add(values);
                        }
                        getUI().get().access(() -> {
                            grid.setItems(records);
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(UploadFileHandlerExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                })
                .withAcceptedFileTypes("text/csv")
                .withDropLabelIcon(VaadinIcon.DROP.create())
                ;

        csvFileHandler.setI18n(new UploadExamplesI18N());
        csvFileHandler.getStyle().setBorder("2px dotted green");

        add(
                new Paragraph("Count lines"),
                liveLogger,
                uploadFileHandler,
                new Paragraph("Count lines (with multiple file support)"),
                multiUploadFileHandler,
                new Paragraph("Display CSV file"), csvFileHandler, grid);

    }


    /**
     * Provides a default I18N configuration for the Upload examples
     *
     * At the moment the Upload component requires a fully configured I18N instance,
     * even for use-cases where you only want to change individual texts.
     *
     * This I18N configuration is an adaption of the web components I18N defaults
     * and can be used as a basis for customizing individual texts.
     */
    public static class UploadExamplesI18N extends UploadI18N {
        public UploadExamplesI18N() {
            setDropFiles(new DropFiles().setOne("-> HERE <--")
                    .setMany("-> HERE <--"));
            setAddFiles(new AddFiles().setOne("Upload File...")
                    .setMany("Upload Files..."));
            setError(new Error().setTooManyFiles("Too Many Files.")
                    .setFileIsTooBig("File is Too Big.")
                    .setIncorrectFileType("Incorrect File Type."));
            setUploading(new Uploading()
                    .setStatus(new Uploading.Status().setConnecting("Connecting...")
                            .setStalled("Stalled")
                            .setProcessing("Processing File...").setHeld("Queued"))
                    .setRemainingTime(new Uploading.RemainingTime()
                            .setPrefix("remaining time: ")
                            .setUnknown("unknown remaining time"))
                    .setError(new Uploading.Error()
                            .setServerUnavailable(
                                    "Upload failed, please try again later")
                            .setUnexpectedServerError(
                                    "Upload failed due to server error")
                            .setForbidden("Upload forbidden")));
            setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB",
                    "PB", "EB", "ZB", "YB")));
        }
    }

}
