package team.aster.processor;

public class PunctuationBasedWatermark extends WatermarkProcessor {
    PunctuationBasedWatermark(){
        encoder = new PunctuationEncoder();
        decoder = new PunctuationDecoder();
    }

    @Override
    public String toString() {
        return "PunctuationBasedWatermark{}";
    }
}
