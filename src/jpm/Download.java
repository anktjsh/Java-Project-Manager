/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

/**
 *
 * @author Aniket
 */
public class Download extends Task<File> {

    private final static Logger log = Logger.getLogger(Download.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final String remoteUrl;
    private final File localFile;
    private final int bufferSize;

    public Download(String remoteUrl, File localFile) {
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = DEFAULT_BUFFER_SIZE;

        stateProperty().addListener((source, oldState, newState) -> {
            if (newState.equals(Worker.State.SUCCEEDED)) {
                onSuccess();
            } else if (newState.equals(Worker.State.FAILED)) {
                onFailed();
            }
        });
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public File getLocalFile() {
        return localFile;
    }

    private InputStream getStream(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            int responseCode = httpConn.getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return (httpConn.getInputStream());
            } else if (responseCode==302) {
                return getStream(httpConn.getHeaderField("Location"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected File call() throws Exception {
        try {
            log.info(String.format("Downloading file %s to %s", remoteUrl, localFile.getAbsolutePath()));
            InputStream stream = getStream(remoteUrl);
            if (stream != null) {
                File dir = localFile.getParentFile();
                dir.mkdirs();
                copy(stream, new FileOutputStream(localFile));
                log.info(String.format("Downloading of file %s to %s completed successfully", remoteUrl, localFile.getAbsolutePath()));
                return localFile;
            } else {
                throw new RuntimeException("NULL HTTP CONNECTION");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public static void copy(InputStream source, OutputStream sink)
            throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
        }
        sink.close();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Download) {
            Download d = (Download) o;
            if (d.getLocalFile().equals(getLocalFile())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.remoteUrl);
        hash = 37 * hash + Objects.hashCode(this.localFile);
        hash = 37 * hash + this.bufferSize;
        return hash;
    }

    private void onFailed() {
        updateProgress(-1, 1);
    }

    private void onSuccess() {
        updateProgress(1, 1);
    }
}
