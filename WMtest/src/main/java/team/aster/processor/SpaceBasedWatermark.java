package team.aster.processor;

public class SpaceBasedWatermark extends WatermarkProcessor {
    SpaceBasedWatermark(){
        encoder = new SpaceEncoder();
        decoder = new SpaceDecoder();
    }

    @Override
    public String toString() {
        return "SpaceBasedWatermark{}";
    }
}
