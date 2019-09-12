package jp.go.nibiohn.bioinfo.server.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@SuppressWarnings("serial")
public class UploadDataServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// deal with the upload file
		response.setContentType("text/html");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		try {
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setFileSizeMax(1000000);
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
//					String type = item.getString();
//					String fieldName = item.getFieldName();
//					System.out.println(fieldName + ":" + type);
				} else {
					InputStream inputStream = item.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line = reader.readLine();
//					PrintWriter writer = response.getWriter();
					while (line != null) {
						// TODO process the upload data and store in the database  
//						System.out.println(line);
//						writer.write(line + "\n");
						line = reader.readLine();
					}
				}
			}
		} catch (FileSizeLimitExceededException e) {
			response.getWriter().write("Upload file exceeds the size limitation. Try a smaller file.");
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
