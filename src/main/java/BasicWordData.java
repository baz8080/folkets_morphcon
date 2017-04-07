/**
 * Models data in the FormRepresentation element
 *
 * Created by barry on 07/04/2017.
 */
class BasicWordData {

    private String baseForm;
    private String partOfSpeech;
    private String lemgram;
    private String paradigm;

    String getBaseForm() {
        return baseForm;
    }

    void setBaseForm(String baseForm) {
        this.baseForm = baseForm;
    }

    String getPartOfSpeech() {
        return partOfSpeech;
    }

    void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    String getLemgram() {
        return lemgram;
    }

    void setLemgram(String lemgram) {
        this.lemgram = lemgram;
    }

    String getParadigm() {
        return paradigm;
    }

    void setParadigm(String paradigm) {
        this.paradigm = paradigm;
    }
}
