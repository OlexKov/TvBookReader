package com.example.bookreader.utility.bookutils.fb2parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class FictionBook {

    protected Xmlns[] xmlns;
    protected Description description;
    protected List<Body> bodies = new ArrayList<>();
    protected Map<String, Binary> binaries;

    public String encoding = "utf-8";

    public FictionBook() {}

    public FictionBook(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException, OutOfMemoryError{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        // Безпосередньо парсимо вхідний потік — парсер сам визначить кодування з XML prolog
        Document doc = db.parse(inputStream);

        initXmlns(doc);
        description = new Description(doc);

        NodeList bodyNodes = doc.getElementsByTagName("body");
        for (int i = 0; i < bodyNodes.getLength(); i++) {
            bodies.add(new Body(bodyNodes.item(i)));
        }

        NodeList binaryNodes = doc.getElementsByTagName("binary");
        if (binaryNodes.getLength() > 0) {
            binaries = new HashMap<>();
            for (int i = 0; i < binaryNodes.getLength(); i++) {
                Binary binary = new Binary(binaryNodes.item(i));
                binaries.put(binary.getId().replace("#", ""), binary);
            }
        }
    }


    public FictionBook(File file) throws ParserConfigurationException, IOException, SAXException, OutOfMemoryError {
        this(new FileInputStream(file));
    }

    protected void setXmlns(ArrayList<Node> nodeList) {
        xmlns = new Xmlns[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            Node node = nodeList.get(index);
            xmlns[index] = new Xmlns(node);
        }
    }

    protected void initXmlns(Document doc) {
        NodeList fictionBook = doc.getElementsByTagName("FictionBook");
        ArrayList<Node> xmlns = new ArrayList<>();
        for (int item = 0; item < fictionBook.getLength(); item++) {
            NamedNodeMap map = fictionBook.item(item).getAttributes();
            for (int index = 0; index < map.getLength(); index++) {
                Node node = map.item(index);
                xmlns.add(node);
            }
        }
        setXmlns(xmlns);
    }

    public ArrayList<Person> getAuthors() {
        return description.getDocumentInfo().getAuthors();
    }

    public Xmlns[] getXmlns() {
        return xmlns;
    }

    public Description getDescription() {
        return description;
    }

    public @Nullable Body getBody() {
        return getBody(null);
    }

    public @Nullable Body getNotes() {
        return getBody("notes");
    }

    public @Nullable Body getComments() {
        return getBody("comments");
    }

    private @NotNull Body getBody(String name) {
        for (Body body : bodies) {
            if ((name + "").equals(body.getName() + "")) {
                return body;
            }
        }
        return bodies.get(0);
    }

    @NotNull
    public Map<String, Binary> getBinaries() {
        return binaries == null ? new HashMap<String, Binary>() : binaries;
    }

    public String getTitle() {
        return description.getTitleInfo().getBookTitle();
    }

    public String getLang() {
        return description.getTitleInfo().getLang();
    }

    public @Nullable Annotation getAnnotation() {
        return description.getTitleInfo().getAnnotation();
    }
}

