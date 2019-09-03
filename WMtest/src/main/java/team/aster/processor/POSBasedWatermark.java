package team.aster.processor;

public class POSBasedWatermark extends WatermarkProcessor{
    POSBasedWatermark(){
        encoder = new POSEncoder();
        decoder = new POSDecoder();
    }

    @Override
    public String toString() {
        return "POSBasedWatermark{}";
    }
}
