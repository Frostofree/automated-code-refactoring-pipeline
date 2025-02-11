```java
public class OpmlParser {

    public List<Outline> parse(InputStream is) throws Exception {
        OpmlHandler handler = new OpmlHandler();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(is, handler);

        return handler.getOutline();
    }

    private static class OpmlHandler extends DefaultHandler {
        private Outline outline;
        private StringBuilder content;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("opml".equalsIgnoreCase(localName)) {
                outline = new Outline();
            } else if ("body".equalsIgnoreCase(localName)) {
                // Do nothing
            } else if ("outline".equalsIgnoreCase(localName)) {
                processOutlineElement(attributes);
            } else {
                // Do nothing
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (content != null) {
                content.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("outline".equalsIgnoreCase(localName)) {
                if (content != null) {
                    outline.setText(StringUtils.trim(content.toString()));
                    content = null;
                }
                outline = outline.getParent();
            }
        }

        private void processOutlineElement(Attributes attributes) {
            Outline childOutline = new Outline();
            childOutline.setTitle(StringUtils.trim(attributes.getValue("title")));
            childOutline.setXmlUrl(StringUtils.trim(attributes.getValue("xmlUrl")));
            childOutline.setHtmlUrl(StringUtils.trim(attributes.getValue("htmlUrl")));
            childOutline.setType(StringUtils.trim(attributes.getValue("type")));

            if (StringUtils.isBlank(childOutline.getType()) || "folder".equals(childOutline.getType()) || "rss".equals(childOutline.getType())) {
                if (outline != null) {
                    outline.getOutlineList().add(childOutline);
                }
            } else {
                log.warn(MessageFormat.format("Ignoring unknown outline of type {0}", childOutline.getType()));
            }

            outline = childOutline;
        }

        public Outline getOutline() {
            return outline;
        }
    }
}

public class Outline {

    // ... Code omitted for brevity ...
}
```