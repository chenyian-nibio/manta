package jp.go.nibiohn.bioinfo.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import jp.go.nibiohn.bioinfo.server.UploadProcessService;

@SuppressWarnings("serial")
public class UploadDataServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, String> paraMap = new HashMap<String, String>();
		// deal with the upload file
		response.setContentType("text/html");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		try {
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setFileSizeMax(45000000);
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					paraMap.put(item.getFieldName(), item.getString());
				} else {
					InputStream inputStream = item.getInputStream();
					String type = paraMap.get("type");
					if (type == null) {
						// unexpected 
					}
					UploadProcessService service = new UploadProcessService();
					boolean res = service.processAndSaveUploadData(type, inputStream);

					PrintWriter writer = response.getWriter();
					if (res) {
						writer.write("OK");
					} else {
						writer.write("Upload failed.");
					}
				}
			}
		} catch (FileSizeLimitExceededException e) {
			response.getWriter().write("Upload file exceeds the size limitation. Try a smaller file.");
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
	}
}
