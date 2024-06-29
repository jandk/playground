package be.twofold.playground;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.nio.file.*;
import java.util.*;

public class SaxTest {
    public static void main(String[] args) {
        Path path = Paths.get("C:\\Temp\\math.stackexchange.com\\Badges.xml.gz");

    }

    private static final class SaxLocalNameCount extends DefaultHandler {
        private Map<String, Integer> tagCount;

        @Override
        public void startDocument() {
            tagCount = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            Integer value = tagCount.get(localName);

            if (value == null) {
                tagCount.put(localName, 1);
            } else {
                int count = value;
                count++;
                tagCount.put(localName, count);
            }
        }

        @Override
        public void endDocument() {
            for (Map.Entry<String, Integer> entry : tagCount.entrySet()) {
                System.out.println("Local Name '" + entry.getKey() + "' occurs " + entry.getValue() + " times");
            }
        }

    }
}
