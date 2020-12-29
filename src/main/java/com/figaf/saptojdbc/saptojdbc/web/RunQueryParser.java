package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.RunQueryInputDto;
import com.figaf.saptojdbc.saptojdbc.dto.RunSqlRequest;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidInputDataException;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidTableNameException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.w3c.dom.Node.ELEMENT_NODE;

@Slf4j
public class RunQueryParser {

    public static final String SQL_QUERY_MODE = "SQL_QUERY";
    public static final String SELECT_MODE = "SELECT";
    public static final String SQL_QUERY_DELIMITER = ";";

    public RunQueryInputDto parse(String input) throws ParserConfigurationException, SAXException, IOException {
        Node rootElement = parseDocumentAndReturnTopLevelNode(input);
        RunQueryInputDto runQueryInputDto = new RunQueryInputDto();
        fillTopLevelTags(rootElement, runQueryInputDto);
        List<Node> children = findAllNodesOfType(rootElement, ELEMENT_NODE);
        for (Node firstLevelNode: children) {
            parseSqlStatementSection(runQueryInputDto, firstLevelNode);
        }
        return runQueryInputDto;
    }

    private void parseSqlStatementSection(RunQueryInputDto runQueryInputDto, Node firstLevelNode) {
        RunSqlRequest runSqlRequest = new RunSqlRequest();
        runSqlRequest.setFirstLevelTag(firstLevelNode.getLocalName());
        log.debug("first level tag name is : " + firstLevelNode.getLocalName());
        findQueryNode(firstLevelNode, runSqlRequest);
        runQueryInputDto.add(runSqlRequest);
    }

    private Node parseDocumentAndReturnTopLevelNode(String input) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(input)));
        return doc.getChildNodes().item(0);
    }

    private void findQueryNode(Node firstLevelTag, RunSqlRequest request) {
        Node queryNode = findFirstElementNodeAmongChildrenOrThrowException(firstLevelTag);
        Node selectedQueryMode = queryNode.getAttributes().getNamedItem("action");
        if (null == selectedQueryMode) {
            throw new InvalidInputDataException("missing mandatory attribute 'action'. " +
                "Please set attribute to the 'SELECT' or 'SQL_QUERY' depending on flow");
        } else if (SQL_QUERY_MODE.equalsIgnoreCase(selectedQueryMode.getNodeValue())) {
            handleRunSqlQueryCase(request, queryNode);
        } else if(SELECT_MODE.equalsIgnoreCase(selectedQueryMode.getNodeValue())) {
            handleSelectCase(request, queryNode);
        } else {
            throw new InvalidInputDataException("Unknown select mode: only select or SQL_QUERY are supported");
        }
    }

    private void handleSelectCase(RunSqlRequest request, Node queryNode) {
        List<Node> queryNodeChildren = findAllNodesOfType(queryNode, ELEMENT_NODE);
        if (queryNodeChildren.size() < 2) {
            throw new InvalidInputDataException(
                format("Node %s should have 'table' and 'access' children tags and optionally keys 'tag'", 
                    queryNode.getLocalName()));
        }
        String tableName = determineTableName(queryNodeChildren.get(0));
        List<String> selectFields = determineSelectedProperties(queryNodeChildren.get(1));
        String sqlStatement = prepareSqlStatement(tableName, selectFields);
        validateAccessStatementOrThrowAnException(sqlStatement);
        request.setAccessStatement(sqlStatement);
        if (queryNodeChildren.size() == 3) {
            Node keys = queryNodeChildren.get(2);
            processKeysNodes(keys, request);
        }
        log.info("input request was parsed successfully, flow type:  {}, table: {} ", SELECT_MODE, tableName);
    }

    private String prepareSqlStatement(String tableName, List<String> selectFields) {
        StringBuilder sqlStatement = new StringBuilder()
            .append("SELECT ");
        int lastItemInList = selectFields.size() - 1;
        for (int i = 0; i< lastItemInList; i++) {
            String fieldName = selectFields.get(i);
            sqlStatement.append(fieldName).append(", ");
        }
        sqlStatement.append(selectFields.get(lastItemInList)).append(" ");
        sqlStatement.append("from ");
        ensureTableNameContainsValidChars(tableName);
        sqlStatement.append(tableName);
        return sqlStatement.toString();
    }

    private void ensureTableNameContainsValidChars(String tableName) {
        String regex = "^[a-zA-Z0-9\\._]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (!matcher.matches()) {
            throw new InvalidTableNameException();
        }
    }

    private List<String> determineSelectedProperties(Node node) {
        List<String> results = new ArrayList<>();
        for (Node child : findAllNodesOfType(node, ELEMENT_NODE)) {
            results.add(child.getLocalName());
        }
        return results;
    }

    private String determineTableName(Node node) {
        return node.getFirstChild().getTextContent();
    }

    private void handleRunSqlQueryCase(RunSqlRequest request, Node queryNode) {
        List<Node> queryNodeChildren = findAllNodesOfType(queryNode, ELEMENT_NODE);
        if (queryNodeChildren.isEmpty()) {
            throw new InvalidInputDataException("Invalid format of input document: " +
                "Document should contain query node; and query node should contain at least access node " +
                "with sql query");
        } else {
            extractAccessData(request, queryNodeChildren);
            extractKeysData(request, queryNodeChildren);
            log.info("input request was parsed successfully, flow type:  {}, sql-query: {} ", 
                SQL_QUERY_MODE, request.getAccessStatement());
        }
    }

    private void extractKeysData(RunSqlRequest request, List<Node> children) {
        if (children.size() > 1) { //means that there is need po process select statement without where clause
            Node keys = children.get(1);
            processKeysNodes(keys, request);
        }
    }

    private void extractAccessData(RunSqlRequest rq, List<Node> children) {
        Node access = children.get(0);
        processAccessNode(access, rq);
    }

    private void processKeysNodes(Node keys, RunSqlRequest request) {
        List<Node> children = findAllNodesOfType(keys, ELEMENT_NODE);
        for (Node child : children) {
            String name = child.getLocalName();
            Node searchCriteriaValue = child.getChildNodes().item(0);
            if (null != searchCriteriaValue) {
                request.addEntry(name, searchCriteriaValue.getNodeValue());
            } else {
                throw new IllegalArgumentException(format("Bad input. Query parameter %s doesn't contain valid value", name));
            }
        }
    }

    private void processAccessNode(Node access, RunSqlRequest request) {
        Node statementNode = access.getChildNodes().item(0);
        String accessStatement = statementNode.getNodeValue().trim();
        validateAccessStatementOrThrowAnException(accessStatement);
        request.setAccessStatement(accessStatement);
    }

    private Node findFirstElementNodeAmongChildrenOrThrowException(Node firstLevelTag) {
        List<Node> children = findAllNodesOfType(firstLevelTag, ELEMENT_NODE);
        if (children.size() != 1) {
            throw new InvalidInputDataException("Invalid structure of input document, please check with reference guide");
        }
        return children.get(0);
    }

    private List<Node> findAllNodesOfType(Node searchBase, short searchedNodeType) {
        ArrayList<Node> result = new ArrayList<>();
        for (int i = 0; i < searchBase.getChildNodes().getLength(); i++) {
            Node current = searchBase.getChildNodes().item(i);
            log.debug( "element idx: {}, nodeType: {}, nodeName: {} ",
                i, current.getNodeType(), current.getNodeName());
            if (current.getNodeType() == searchedNodeType) {
                result.add(current);
            }
        }
        return result;
    }

    private void fillTopLevelTags(Node rootElement, RunQueryInputDto runQueryInputDto) {
        runQueryInputDto.setTopLevelTag(rootElement.getLocalName());
        runQueryInputDto.setTopLevelPrefix(rootElement.getPrefix());
        NamedNodeMap attributes = rootElement.getAttributes();
        String schemaAttribute = "xmlns:" + runQueryInputDto.getTopLevelPrefix();
        log.debug("computed schema attribute is : {}", schemaAttribute);
        runQueryInputDto.setTopLevelSchema(attributes.getNamedItem(schemaAttribute).getNodeValue());
    } 
    
    public void validateAccessStatementOrThrowAnException(String input) {
        if (input.contains(SQL_QUERY_DELIMITER)) {
            throw new IllegalArgumentException("Character ';' is prohibited for usage in queries");
        }
    }
}
