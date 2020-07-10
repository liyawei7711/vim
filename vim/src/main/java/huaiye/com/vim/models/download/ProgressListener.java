package huaiye.com.vim.models.download;

public interface ProgressListener {
    void onProgress(long totalBytes, long remainingBytes, boolean done);
}
