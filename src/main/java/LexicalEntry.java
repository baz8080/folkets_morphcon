import java.util.ArrayList;
import java.util.List;

/**
 * Models a LexicalEntry element
 *
 * Created by barry on 07/04/2017.
 */
class LexicalEntry {
    private BasicWordData basicWordData;
    private List<WordForm> wordForms;

    LexicalEntry() {
        wordForms = new ArrayList<>();
    }

    BasicWordData getBasicWordData() {
        return basicWordData;
    }

    void setBasicWordData(BasicWordData basicWordData) {
        this.basicWordData = basicWordData;
    }

    List<WordForm> getWordForms() {
        return wordForms;
    }
}
