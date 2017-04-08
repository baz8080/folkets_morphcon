import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Makes a denormalised database with Saldo morphology data
 *
 * Created by barry on 24/03/2017.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        createMorphologyDatabase();
    }

    private static void createMorphologyDatabase() throws SAXException, IOException, SQLException {
        InputStream is = Main.class.getResourceAsStream("saldom.xml");

        if (is == null) {
            throw new IllegalStateException("Morphology data is missing.");
        }

        long time = System.currentTimeMillis();

        XMLReader reader = XMLReaderFactory.createXMLReader();
        MorphologyHandler morphologyHandler = new MorphologyHandler();
        reader.setContentHandler(morphologyHandler);
        reader.parse(new InputSource(is));

        System.out.println("Parsed document in " + (System.currentTimeMillis() - time));
        is.close();

        time = System.currentTimeMillis();

        Connection connection = DriverManager.getConnection("jdbc:sqlite:build/saldom.sqlite");
        connection.setAutoCommit(false);

        Statement statement = connection.createStatement();

        statement.executeUpdate("DROP TABLE IF EXISTS word_forms");
        statement.executeUpdate("DROP INDEX IF EXISTS word_form_index");
        statement.executeUpdate("CREATE TABLE word_forms (" +
                "id INTEGER PRIMARY KEY ASC," +
                "word_form TEXT, word_msd TEXT, word_base_form TEXT," +
                "word_lemgram TEXT, word_part_of_speech TEXT, word_paradigm TEXT" +
                ")");
        statement.executeUpdate("CREATE INDEX word_form_index on word_forms(word_form)");
        statement.close();
        connection.commit();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO word_forms(" +
                "word_form, word_msd, word_base_form, word_lemgram, word_part_of_speech, word_paradigm" +
                ") VALUES (?, ?, ?, ?, ?, ?)");

        for (LexicalEntry lexicalEntry : morphologyHandler.getLexicalEntries()) {

            for (WordForm wordForm : lexicalEntry.getWordForms()) {

                preparedStatement.setString(1, wordForm.getWrittenform());
                preparedStatement.setString(2, wordForm.getMsd());
                preparedStatement.setString(3, lexicalEntry.getBasicWordData().getBaseForm());
                preparedStatement.setString(4, lexicalEntry.getBasicWordData().getLemgram());
                preparedStatement.setString(5, lexicalEntry.getBasicWordData().getPartOfSpeech());
                preparedStatement.setString(6, lexicalEntry.getBasicWordData().getParadigm());

                preparedStatement.addBatch();
            }
        }

        preparedStatement.executeBatch();
        preparedStatement.close();
        connection.commit();

        connection.close();
        System.out.println("Created database in " + (System.currentTimeMillis() - time));
    }

    static class MorphologyHandler extends DefaultHandler {

        List<LexicalEntry> lexicalEntries = new ArrayList<>();
        LexicalEntry lexicalEntry;

        Stack<Object> elementStack = new Stack<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (localName.equals("LexicalEntry")) {
                lexicalEntry = new LexicalEntry();
            }

            if (localName.equals("Lemma")) {
                elementStack.push(new BasicWordData());
            }

            if (localName.equals("WordForm")) {
                elementStack.push(new WordForm());
            }

            if (localName.equals("feat") && elementStack.size() > 0 && elementStack.peek() instanceof BasicWordData) {
                if (attributes != null) {
                    if (attributes.getValue("att") != null && attributes.getValue("val") != null) {

                        BasicWordData basicWordData = (BasicWordData) elementStack.peek();

                        String att = attributes.getValue("att");
                        String value = attributes.getValue("val");

                        if ("writtenForm".equals(att)) {
                            basicWordData.setBaseForm(value);
                        } else if ("lemgram".equals(att)) {
                            basicWordData.setLemgram(value);
                        } else if ("partOfSpeech".equals(att)) {
                            basicWordData.setPartOfSpeech(value);
                        } else if ("paradigm".equals(att)) {
                            basicWordData.setParadigm(value);
                        }
                    }
                }
            }

            if (localName.equals("feat") && elementStack.size() > 0 && elementStack.peek() instanceof WordForm) {
                if (attributes != null) {
                    if (attributes.getValue("att") != null && attributes.getValue("val") != null) {

                        WordForm wordForm = (WordForm) elementStack.peek();

                        String att = attributes.getValue("att");
                        String value = attributes.getValue("val");

                        if ("writtenForm".equals(att)) {
                            wordForm.setWrittenform(value);
                        } else if ("msd".equals(att)) {
                            wordForm.setMsd(value);
                        }
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("Lemma")) {
                lexicalEntry.setBasicWordData((BasicWordData) elementStack.pop());
            }

            if (localName.equals("WordForm")) {
                lexicalEntry.getWordForms().add((WordForm) elementStack.pop());
            }

            if (localName.equals("LexicalEntry")) {
                lexicalEntries.add(lexicalEntry);
            }
        }

        List<LexicalEntry> getLexicalEntries() {
            return lexicalEntries;
        }
    }

}
