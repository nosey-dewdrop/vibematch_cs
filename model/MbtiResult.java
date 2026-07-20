package model;

/*
 * The result of the vibe test. type is the 4 letter code like "ENFP".
 * The four percentages are how strongly the person leans to the FIRST letter
 * of each axis:
 *   eiPercent -> toward E (so 30 means more I)
 *   snPercent -> toward S (so 30 means more N)
 *   tfPercent -> toward T (so 30 means more F)
 *   jpPercent -> toward J (so 30 means more P)
 * We use these for the little bars on the result screen.
 */
public class MbtiResult {

    private String type;
    private int eiPercent;
    private int snPercent;
    private int tfPercent;
    private int jpPercent;

    public MbtiResult(String type, int eiPercent, int snPercent, int tfPercent, int jpPercent) {
        this.type = type;
        this.eiPercent = eiPercent;
        this.snPercent = snPercent;
        this.tfPercent = tfPercent;
        this.jpPercent = jpPercent;
    }

    public String getType() { return type; }

    public int getEiPercent() { return eiPercent; }
    public int getSnPercent() { return snPercent; }
    public int getTfPercent() { return tfPercent; }
    public int getJpPercent() { return jpPercent; }
}
