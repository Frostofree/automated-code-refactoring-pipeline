```java
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sismics.reader.core.model.OpmlOutline;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpmlReader {
    public List<OpmlOutline> parse(InputStream is) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        return new OpmlParser(reader).parse();
    }

    private static class OpmlParser {
        private final XMLStreamReader reader;
        private List<OpmlOutline> outlines;
        private OpmlOutline outline;

        public OpmlParser(XMLStreamReader reader) {
            this.reader = reader;
            outlines = new ArrayList<>();
        }

        public List<OpmlOutline> parse() throws XMLStreamException {
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String name = reader.getLocalName();
                        if ("outline".equals(name)) {
                            outline = new OpmlOutline();
                            outline.setText(reader.getAttributeValue(null, "title"));
                            outline.setXmlUrl(reader.getAttributeValue(null, "xmlUrl"));
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        name = reader.getLocalName();
                        if ("outline".equals(name)) {
                            outlines.add(outline);
                        }
                        break;
                }
            }

            return outlines;
        }
    }
}
```