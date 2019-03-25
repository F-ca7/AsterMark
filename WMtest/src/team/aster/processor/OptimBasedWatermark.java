package team.aster.processor;

class OptimBasedWatermark extends WatermarkProcessor {

    OptimBasedWatermark(){
        encoder = new OptimEncoder();
        decoder = new OptimDecoder();

    }

    @Override
    public String toString() {
        return "Optimization Based team.aster.processor";
    }
}
