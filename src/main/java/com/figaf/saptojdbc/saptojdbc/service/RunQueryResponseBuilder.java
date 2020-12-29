package com.figaf.saptojdbc.saptojdbc.service;

import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.dto.RunSqlRequest;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Slf4j
public class RunQueryResponseBuilder {
    
    private Document doc;
    private Element rootElement;

    public RunQueryResponseBuilder(RunQueryInputDto inputDto) throws Exception {
        this.doc = createDocumentBuilderFactory();
        this.rootElement = createRootNode(inputDto);
    }

    private Element createRootNode(RunQueryInputDto inputDto) {
        return doc.createElementNS(
            inputDto.getTopLevelSchema(), // namespace
            createRootTag(inputDto) // node name including prefix
        );
    }

    public void addSection(RunSqlRequest request,
                           List<Map<String, String>> queryResults) {
        Node firstLevelTag = createNestedFirstLevelTag(doc, request, rootElement);
        createDataRow(doc, firstLevelTag, queryResults);
    }

    private Document createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    private void createDataRow(Document doc, Node firstLevelTag, List<Map<String, String>> queryResults) {
        for(Map<String, String> rowEntry: queryResults) {
            try {
                Element rowElement = doc.createElement("row");
                for(Map.Entry<String, String> propertyValuePair: rowEntry.entrySet()) {
                    String propertyValue = propertyValuePair.getValue();
                    Element propertyEl = doc.createElement(propertyValuePair.getKey());
                    propertyEl.setTextContent(propertyValue);
                    rowElement.appendChild(propertyEl);
                }
                firstLevelTag.appendChild(rowElement);
            } catch (DOMException e) {
                log.error("exception happened during creation xml response ", e);
                String rowContent = convertRowEntryToString(rowEntry);
                log.error(" row that caused troubles is: {}", rowContent);
            }
        }
    }

    private String convertRowEntryToString(Map<String, String> rowEntry) {
        StringBuilder acc = new StringBuilder();
        for(Map.Entry<String, String> propertyValuePair: rowEntry.entrySet()) {
            acc.append("propertyName:")
                    .append(propertyValuePair.getKey())
                .append(", value=")
                .append(propertyValuePair.getValue())
                .append("; ");
        }
        return acc.toString();
    }

    private Node createNestedFirstLevelTag(Document doc,
                                           RunSqlRequest inputDto, Element rootElement) {
        Element element = doc.createElement(inputDto.getFirstLevelTag() + "_response");
        rootElement.appendChild(element);
        return element;
    }

    private String createRootTag(RunQueryInputDto inputDto) {
        return inputDto.getTopLevelPrefix() 
            + ":" 
            + inputDto.getTopLevelTag()
            + "_response";
    }

    public String transformDocumentToString() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(rootElement);
        ByteArrayOutputStream bout  = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bout);
        transformer.transform(source, result);
        return new String(bout.toByteArray());
    }
}
