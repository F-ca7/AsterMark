package team.aster.processor;

class PrimitiveLSBWatermark extends WatermarkProcessor{
    PrimitiveLSBWatermark(){
        encoder = new PrimLSBEncoder();
        decoder = new PrimLSBDecoder();
    }

    @Override
    public String toString() {
        return "PrimitiveLSBWatermark{}";
    }
}
