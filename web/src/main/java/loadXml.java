
//taken from: http://www.servletworld.com/servlet-tutorials/servlet3/multipartconfig-file-upload-example.html
// and http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html
import core.Settings;
import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import exceptions.XmlException;
import user.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
public class loadXml extends HttpServlet {

    private User user;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        StringBuilder content = loadXml(request, response);
        user = WebUtils.getSessionUser(request);
        try{
            user.getEngine().isXmlValid(content.toString());
            user.getEngine().loadRepositoryFromXML();
        }
        catch (UncommittedChangesError | InvalidBranchNameError | XmlException e){
            // TODO handle
            e.printStackTrace();
        }
//        new MainEngine().isXmlValid();
    }

    private StringBuilder loadXml(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{

        Collection<Part> parts = request.getParts();

        StringBuilder fileContent = new StringBuilder();

        for (Part part : parts) {
            // TODO(ALONA) valid by document requierments?
            //to write the content of the file to an actual file in the system (will be created at c:\samplefile)
            part.write(new File(Settings.baseLocation, "loaded.xml").getAbsolutePath()); // TODO support multople users

            //to write the content of the file to a string
            fileContent.append(readFromInputStream(part.getInputStream()));
        }

        return fileContent;
    }

    private String readFromInputStream(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\Z").next();
    }

}