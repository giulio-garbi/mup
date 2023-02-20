package org.sysma.schedulerExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

public class MultiPartStringParser implements org.apache.commons.fileupload.UploadContext {
    
    private String postBody;
    private String boundary;
    private byte[] data;
    private Map<String, String> parameters = new HashMap<String, String>();
            
    public MultiPartStringParser(String postBody, byte[] data) throws Exception {
        this.postBody = postBody;
        this.data = data;
        // Sniff out the multpart boundary.
        int idx = postBody.indexOf('\n');
        if (idx == -1)
        	idx = postBody.length();
        if(idx == 0)
        	return;
        this.boundary = postBody.substring(Math.min(2,idx), idx).trim();
        // Parse out the parameters.
        final FileItemFactory factory = new DiskFileItemFactory();
        FileUpload upload = new FileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(this);
        for (FileItem fileItem: fileItems) {
            if (fileItem.getName() != null){
            	var fc = fileItem.get();
                parameters.put(fileItem.getFieldName(), fileItem.getName() + "|" + Base64.getEncoder().encodeToString(fc));
            } else {
            	parameters.put(fileItem.getFieldName(), fileItem.getString());
            }
        }
    }
    
    public Map<String,String> getParameters() {
        return parameters;
    }

    // The methods below here are to implement the UploadContext interface.
    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; // You should know the actual encoding.
    }
    
    // This is the deprecated method from RequestContext that unnecessarily
    // limits the length of the content to ~2GB by returning an int. 
    @Override
    public int getContentLength() {
        return -1; // Don't use this
    }

    @Override
    public String getContentType() {
        // Use the boundary that was sniffed out above.
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long contentLength() {
        return postBody.length();
    }
}