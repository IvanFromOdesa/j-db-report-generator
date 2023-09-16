package org.ivank.gen;

import org.ivank.Logger;
import org.ivank.db.data.DbData;
import org.ivank.db.data.FkKeyData;
import org.ivank.db.data.RowData;
import org.ivank.db.data.TableData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public class ReportGenerator {

    public static void writeToXML(String catalog, DbData dbData, String outputPath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(catalog);
            doc.appendChild(rootElement);
            for (TableData tableData : dbData.getTableData()) {
                Element tableName = doc.createElement(tableData.getTableName());
                rootElement.appendChild(tableName);
                for (RowData rowData : tableData.getRows()) {
                    Element record = doc.createElement("record");
                    String pkKey = tableData.getPkKey();
                    if (pkKey != null && !"".equals(pkKey)) {
                        record.setAttribute(pkKey, rowData.row().get(pkKey));
                    }
                    tableName.appendChild(record);
                    proccessRow(doc, rowData, record);
                    for (FkKeyData fkKeyData : tableData.getFkKeyData()) {
                        String table = fkKeyData.fkKeyTable();
                        String fkKeyValue = rowData.row().get(pkKey);
                        Optional<RowData> rd = DbData.byFkKey(dbData, fkKeyValue, fkKeyData);
                        if (rd.isPresent()) {
                            String key = fkKeyData.fkKey();
                            Element nested = doc.createElement(table);
                            record.appendChild(nested);
                            nested.setAttribute(key, fkKeyValue);
                            proccessRow(doc, rd.get(), nested);
                        }
                    }
                }
                if (tableData.getRows().isEmpty()) {
                    Logger.warning(String.format("Empty table: %s", tableData.getTableName()));
                }
                Logger.info(String.format("Finished writing table: %s", tableData.getTableName()));
            }
            FileOutputStream output = new FileOutputStream(outputPath);
            writeXml(doc, output);
            output.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private static void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // pretty print XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
    }

    private static void proccessRow(Document doc, RowData rowData, Element record) {
        for (Map.Entry<String, String> entry : rowData.row().entrySet()) {
            Element key = doc.createElement(entry.getKey());
            key.setTextContent(entry.getValue());
            record.appendChild(key);
        }
    }
}
