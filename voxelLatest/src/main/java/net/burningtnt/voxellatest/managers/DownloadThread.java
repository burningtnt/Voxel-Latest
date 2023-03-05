package net.burningtnt.voxellatest.managers;

import java.io.File;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DownloadThread extends Thread {
    private enum DownloadTaskStatus {
        PREPARING, DOWNLOADING, CANCELED, DONE;
    }

    private static class DownLoadTask {
        private static int RETRY_COUNTER_MAX = 3;

        private final URL url;
        private final Path path;

        private final UUID uuid;
        private DownloadTaskStatus downloadTaskStatus;
        private CompletableFuture<HttpResponse<Path>> connection = null;

        private int retryCounter = 0;

        private final boolean shouldCacheAsFile;

        private DownLoadTask(URL url, UUID uuid, boolean shouldCacheAsFile) {
            this.url = url;
            this.path = new File(ConfigFileManager.downloadRootDirectory, uuid.toString()).toPath();
            this.uuid = uuid;
            this.shouldCacheAsFile = shouldCacheAsFile;
            this.downloadTaskStatus = DownloadTaskStatus.PREPARING;
        }
    }

    private static final Map<String, DownLoadTask> inputDownloadTasksStream = new HashMap<>();
    private static final Set<DownLoadTask> downloadingTasks = new HashSet<>();
    private static final Map<String, DownLoadTask> outputDownloadTasksStream = new HashMap<>();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .proxy(ProxySelector.getDefault())
            .authenticator(Authenticator.getDefault())
            .build();

    public static UUID pushTask(URL url, boolean shouldCacheAsFile) {
        UUID res = null;
        synchronized (inputDownloadTasksStream) {
            if (inputDownloadTasksStream.containsKey(url.toString())) {
                res = inputDownloadTasksStream.get(url.toString()).uuid;
            } else {
                res = UUID.randomUUID();
                DownLoadTask downLoadTask = new DownLoadTask(url, res, shouldCacheAsFile);
                inputDownloadTasksStream.put(url.toString(), downLoadTask);
                outputDownloadTasksStream.put(url.toString(), downLoadTask);
            }
        }
        return res;
    }

    public DownloadThread() {
        super("VoxelLatest-Net-Download-Thread");
    }

    public void run() {
        while (true) {
            submitInputTask();
            doDownloadTask();
            submitOutputTask();
        }
    }

    private void submitOutputTask() {
        downloadingTasks.removeIf((DownLoadTask downLoadTask) -> {
            if (downLoadTask.downloadTaskStatus == DownloadTaskStatus.DONE) {
                return true;
            }
            if (downLoadTask.downloadTaskStatus == DownloadTaskStatus.CANCELED) {
                synchronized (outputDownloadTasksStream) {
                    outputDownloadTasksStream.remove(downLoadTask.url.toString());
                }
                return true;
            }
            return false;
        });
    }

    private void doDownloadTask() {
        for (DownLoadTask downLoadTask : downloadingTasks) {
            switch (downLoadTask.downloadTaskStatus) {
                case PREPARING -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(downLoadTask.url.toURI())
                                .timeout(Duration.ofSeconds(20))
                                .GET()
                                .build();
                        downLoadTask.connection = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofFile(downLoadTask.path));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    downLoadTask.downloadTaskStatus = DownloadTaskStatus.DOWNLOADING;
                }
                case DOWNLOADING -> {
                    if (downLoadTask.connection.isCompletedExceptionally()) {
                        if (downLoadTask.retryCounter < DownLoadTask.RETRY_COUNTER_MAX) {
                            try {
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(downLoadTask.url.toURI())
                                        .timeout(Duration.ofSeconds(20))
                                        .GET()
                                        .build();
                                downLoadTask.connection = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofFileDownload(downLoadTask.path));
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                            downLoadTask.retryCounter++;
                        }
                    } else if (downLoadTask.connection.isCancelled()) {
                        downLoadTask.downloadTaskStatus = DownloadTaskStatus.CANCELED;
                    } else if (downLoadTask.connection.isDone()) {
                        try {
                            HttpResponse<Path> response = downLoadTask.connection.get();
                            if (response.statusCode() == 200) {
                                downLoadTask.downloadTaskStatus = DownloadTaskStatus.DONE;
                            } else {
                                try {
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(downLoadTask.url.toURI())
                                            .timeout(Duration.ofSeconds(20))
                                            .GET()
                                            .build();
                                    downLoadTask.connection = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofFileDownload(downLoadTask.path));
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                                downLoadTask.retryCounter++;
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                case DONE, CANCELED -> {
                }
                default -> {
                    throw new EnumConstantNotPresentException(DownloadTaskStatus.class, downLoadTask.downloadTaskStatus.name());
                }
            }
        }
    }

    private void submitInputTask() {
        synchronized (inputDownloadTasksStream) {
            for (DownLoadTask downLoadTask : inputDownloadTasksStream.values()) {
                downloadingTasks.add(downLoadTask);
                downLoadTask.downloadTaskStatus = DownloadTaskStatus.PREPARING;
            }
        }
    }
}
